package xyz.joonhyung.tetris

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.serialization.json.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*

class TetrisBattleController(private val coroutineScope: CoroutineScope, val countDownMilis: Long = 3000) {
    var homeBoard = TetrisBoard(coroutineScope, this)
    var awayBoard = TetrisBoard(coroutineScope, this)

    val resetMessage = "reset"

    enum class UserType {
        HOME, AWAY, SPECTATOR
    }

    private val tetrisUsers = ConcurrentHashMap<WebSocketSession, UserType>()
    private val homeIsReady = AtomicBoolean(false)
    private val awayIsReady = AtomicBoolean(false)
    private val finished = AtomicBoolean(false)

    suspend fun ConcurrentHashMap<WebSocketSession, UserType>.send(frame: Frame) {
        forEach {
            try {
                it.key.send(frame.copy())
            } catch (t: Throwable) {
                try {
                    it.key.close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, ""))
                } catch (ignore: ClosedSendChannelException) {
                    // at some point it will get closed
                }
            }
        }
    }

    private suspend fun reset() {
        if (finished.compareAndSet(true, false)) {
            homeIsReady.set(false)
            awayIsReady.set(false)
            homeBoard.close()
            awayBoard.close()
            homeBoard = TetrisBoard(coroutineScope, this)
            awayBoard = TetrisBoard(coroutineScope, this)
            tetrisUsers.send(Frame.Text(resetMessage))
        }
    }

    fun TetrisBoard.getName(): String {
        return when (this) {
            homeBoard -> "home"
            awayBoard -> "away"
            else -> "unknown"
        }
    }

    suspend fun sendToClient(board: TetrisBoard) {
        for (message in board.messageChannel) {
            message.boardName = board.getName()
            val json = Json(JsonConfiguration.Stable)
            val jsonString = json.stringify(TetrisBoard.Message.serializer(), message)
            if (message.recipient == null) tetrisUsers.send(Frame.Text(jsonString))
            else message.recipient!!.send(Frame.Text(jsonString))
        }
    }

    private fun setReady(board: TetrisBoard) {
        val effectiveReady = when (board) {
            homeBoard -> homeIsReady.compareAndSet(false, true)
            else -> awayIsReady.compareAndSet(false, true)
        }
        if (effectiveReady && homeIsReady.get() && awayIsReady.get()) {
            readyAndStart()
        }
    }

    private suspend fun handleAction(board: TetrisBoard, action: String) {
        when (action) {
            "left" -> board.moveLeft()
            "right" -> board.moveRight()
            "rotateLeft" -> board.rotateLeft()
            "rotateRight" -> board.rotateRight()
            "hold" -> board.holdBlock()
            "softDrop" -> board.softDrop()
            "hardDrop" -> board.hardDrop()
            "surrender" -> board.surrender()
            "ready" -> setReady(board)
            "reset" -> reset()
        }
    }

    suspend fun clientJoined(webSocketSession: WebSocketSession) {
        tetrisUsers.computeIfAbsent(webSocketSession) { UserType.SPECTATOR }
        homeBoard.sendGameIfStarted(webSocketSession)
        awayBoard.sendGameIfStarted(webSocketSession)
    }

    fun clientLeft(webSocketSession: WebSocketSession) {
        tetrisUsers.remove(webSocketSession)
    }

    suspend fun receiveFromClient(webSocketSession: WebSocketSession, action: String) {
        when (action) {
            "home" -> tetrisUsers[webSocketSession] = UserType.HOME
            "away" -> tetrisUsers[webSocketSession] = UserType.AWAY
            "spectator" -> tetrisUsers[webSocketSession] = UserType.SPECTATOR
            else -> when (tetrisUsers[webSocketSession]) {
                UserType.HOME -> handleAction(homeBoard, action)
                UserType.AWAY -> handleAction(awayBoard, action)
                else -> {
                    // Do nothing
                }
            }
        }
    }

    fun readyAndStart() {
        coroutineScope.launch {
            sendToClient(homeBoard)
        }
        coroutineScope.launch {
            sendToClient(awayBoard)
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
            if (finished.compareAndSet(false, true)) {
                // TODO: Notify that [board] is loser
                val winnerBoard = when (board) {
                    homeBoard -> awayBoard
                    else -> homeBoard
                }
                board.gameOver(win = false)
                winnerBoard.gameOver(win = true)
            }
        }
    }
}