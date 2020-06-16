package xyz.joonhyung.tetris

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.sync.*
import kotlinx.serialization.json.*
import java.util.concurrent.*

class TetrisBattleController(private val coroutineScope: CoroutineScope, private val server: TetrisServer,
                             val countDownMilis: Long = 3000) {
    var homeBoard = TetrisBoard(coroutineScope, this)
    var awayBoard = TetrisBoard(coroutineScope, this)

    private val globalChannel = Channel<TetrisMessage>(Channel.UNLIMITED)
    private val messageStream = ConcurrentHashMap<String, Channel<String>>()

    enum class BattleState {
        UNREADY, READY, RUNNING, STOPPED, FINISHED, CLOSED, TERMINATED
    }

    // This set of properties manage the state of the battle.
    private val mutex = Mutex()
    private val tetrisUsers = HashSet<String>()
    private var homeUser: String? = null
    private var awayUser: String? = null
    private var homeIsReady = false
    private var awayIsReady = false
    private var state = BattleState.UNREADY

    init {
        coroutineScope.launch {
            for (message in globalChannel) {
                sendMessage(message)
            }
        }
        state = BattleState.READY
    }

    private fun sendMessage(message: TetrisMessage) {
        val json = Json(JsonConfiguration.Stable)
        val jsonString = json.stringify(TetrisMessage.serializer(), message)
        val recipient = message.recipient
        if (recipient == null) {
            messageStream.forEach {
                it.value.offer(jsonString)
            }
        }
        else messageStream[recipient]?.offer(jsonString)
    }

    private suspend fun reset() {
        mutex.withLock {
            if (state == BattleState.FINISHED) {
                homeIsReady = false
                awayIsReady = false
                homeUser = null
                awayUser = null
                state = BattleState.CLOSED
            } else return
        }
        // We want to avoid holding more than one lock.
        homeBoard.close()
        awayBoard.close()
        homeBoard = TetrisBoard(coroutineScope, this)
        awayBoard = TetrisBoard(coroutineScope, this)
        sendMessage(TetrisMessage.Reset())
        mutex.withLock {
            state = BattleState.READY
        }
    }

    private fun TetrisBoard.getName(): String {
        return when (this) {
            homeBoard -> "home"
            awayBoard -> "away"
            else -> "unknown"
        }
    }

    private suspend fun streamBoardMessage(board: TetrisBoard) {
        for (message in board.messageChannel) {
            message.boardName = board.getName()
            sendMessage(message)
        }
    }

    suspend fun start() = mutex.withLock {
        if (state == BattleState.UNREADY) {
            coroutineScope.launch {
                for (message in globalChannel) {
                    sendMessage(message)
                }
            }
            state = BattleState.READY
        }
    }

    suspend fun close() = mutex.withLock {
        if (state == BattleState.FINISHED) {
            state = BattleState.TERMINATED
            globalChannel.close()
        }
    }

    private fun setReadyState(board: TetrisBoard, ready: Boolean) {
        when (board) {
            homeBoard -> homeIsReady = ready
            awayBoard -> awayIsReady = ready
        }
        sendBoardUserMessage(null)
        if (homeIsReady && awayIsReady) {
            state = BattleState.RUNNING
            readyAndStart()
        }
    }

    private suspend fun handleAction(member: String, action: String) {
        val board = mutex.withLock {
            val board = when (member) {
                homeUser -> homeBoard
                awayUser -> awayBoard
                else -> return
            }
            if (action == "ready" || action == "unready") {
                if (state == BattleState.READY) setReadyState(board, action == "ready")
                return
            }
            board
        }
        when (action) {
            "left" -> board.moveLeft()
            "right" -> board.moveRight()
            "rotateLeft" -> board.rotateLeft()
            "rotateRight" -> board.rotateRight()
            "hold" -> board.holdBlock()
            "softDrop" -> board.softDrop()
            "hardDrop" -> board.hardDrop()
            "surrender" -> board.surrender()
        }
    }

    suspend fun addUser(member: String) = mutex.withLock {
        tetrisUsers.add(member)
    }

    suspend fun removeUser(member: String) = mutex.withLock {
        tetrisUsers.remove(member)
    }

    private fun getBoardUserMessage(): TetrisMessage.BoardUser {
        return TetrisMessage.BoardUser(server.getMemberName(homeUser), homeIsReady,
                                       server.getMemberName(awayUser), awayIsReady)
    }

    private fun sendBoardUserMessage(recipient: String?) {
        val message = getBoardUserMessage()
        message.recipient = recipient
        globalChannel.offer(message)
    }

    suspend fun memberJoined(member: String) {
        messageStream.computeIfAbsent(member) {
            val stream = Channel<String>(Channel.UNLIMITED)
            coroutineScope.launch {
                // This coroutine terminates if the member leaves
                for (message in stream) {
                    server.send(member, Frame.Text(message))
                }
            }
            stream
        }
        addUser(member)
        homeBoard.sendGameIfStarted(member)
        awayBoard.sendGameIfStarted(member)
        mutex.withLock {
            sendBoardUserMessage(member)
        }
    }

    suspend fun memberLeft(member: String) {
        setUserType(member, "spectator")
        removeUser(member)
        messageStream[member]?.close()
        messageStream.remove(member)
    }

    private suspend fun setUserType(member: String, type: String) = mutex.withLock {
        val board = when (member) {
            homeUser -> {
                homeUser = null
                homeIsReady = false
                homeBoard
            }
            awayUser -> {
                awayUser = null
                awayIsReady = false
                awayBoard
            }
            else -> null
        }
        if (state == BattleState.RUNNING) {
            coroutineScope.launch {
                board?.surrender()
            }
        }
        when (type) {
            "home" -> if (homeUser == null) {
                homeUser = member
            }
            "away" -> if (awayUser == null) {
                awayUser = member
            }
        }
        sendBoardUserMessage(null)
    }

    suspend fun receiveFromMember(member: String, action: String) {
        when (action) {
            "home", "away", "spectator" -> setUserType(member, action)
            "reset" -> reset()
            else -> handleAction(member, action)
        }
    }

    private fun readyAndStart() {
        coroutineScope.launch {
            streamBoardMessage(homeBoard)
        }
        coroutineScope.launch {
            streamBoardMessage(awayBoard)
        }
        coroutineScope.launch {
            homeBoard.readyAndStart(countDownMilis)
        }
        coroutineScope.launch {
            awayBoard.readyAndStart(countDownMilis)
        }
    }

    fun sendTrashLines(board: TetrisBoard, numLines: Int) {
        // Send trash lines to another board.
        // We launch in another coroutine to prevent deadlock.
        coroutineScope.launch {
            when (board) {
                homeBoard -> awayBoard.trashReceived(numLines)
                awayBoard -> homeBoard.trashReceived(numLines)
            }
        }
    }

    fun gameOver(board: TetrisBoard) {
        coroutineScope.launch {
            mutex.withLock {
                if (state == BattleState.RUNNING) {
                    state = BattleState.STOPPED
                } else return@launch
            }
            val winnerBoard = when (board) {
                homeBoard -> awayBoard
                else -> homeBoard
            }
            board.gameOver(win = false)
            winnerBoard.gameOver(win = true)
            mutex.withLock {
                state = BattleState.FINISHED
            }
        }
    }
}