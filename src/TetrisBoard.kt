package xyz.joonhyung.tetris

import kotlin.math.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.sync.*
import xyz.joonhyung.tetris.blocks.*

class TetrisBoard(private val coroutineScope: CoroutineScope = GlobalScope,
                  private val tetrisBattleController: TetrisBattleController? = null,
                  val knockoutLimit: Int = 3,
                  val knockoutTimeout: Long = 700L,
                  val softDropTimeout: Long = 1000L,
                  val hardDropTimeout: Long = 700L) {
    enum class Cell(val code: Int) {
        EMPTY(0),
        TRASH(1),
        OCCUPIED_J(2),
        OCCUPIED_L(3),
        OCCUPIED_I(4),
        OCCUPIED_O(5),
        OCCUPIED_T(6),
        OCCUPIED_S(7),
        OCCUPIED_Z(8)
    }

    private fun Tetromino.getCode() = when (this) {
        is TetrominoJ -> Cell.OCCUPIED_J.code
        is TetrominoL -> Cell.OCCUPIED_L.code
        is TetrominoI -> Cell.OCCUPIED_I.code
        is TetrominoO -> Cell.OCCUPIED_O.code
        is TetrominoT -> Cell.OCCUPIED_T.code
        is TetrominoS -> Cell.OCCUPIED_S.code
        is TetrominoZ -> Cell.OCCUPIED_Z.code
    }

    enum class GameState {
        READY, RUNNING, STOPPED, KNOCKOUT, FINISHED, CLOSED
    }

    enum class TRotation {
        NONE, NORMAL, TST
    }

    enum class TSpinState {
        NONE, T_SPIN, T_SPIN_MINI
    }

    val messageChannel = Channel<TetrisMessage>(Channel.UNLIMITED)

    private val mutex = Mutex()
    val width = 10
    val height = 20
    val paddedHeight = 3 * height
    val board = Array(width) { IntArray(paddedHeight) { Cell.EMPTY.code } }
    private val queue = TetrominoQueue()
    var currentBlock: Tetromino = queue.poll()
    var heldBlock: Tetromino? = null
        private set
    var heldBefore: Boolean = false
        private set
    private val startPosition = Position(4, 19)
    var currentPosition = startPosition
    private var suspendedSoftDrop: Job? = null
    private var suspendedHardDrop: Job? = null

    var gameState = GameState.READY
        private set
    var numKnockouts = 0

    val lockDownLimit: Int = 15
    var lockDownCount: Int = 0
        private set
    var lockDownActivated: Boolean = false
        private set

    var trashLines: Int = 0
        private set
    var queuedTrashLines: Int = 0
        private set
    var totalTrashSent: Int = 0
        private set
    var totalLinesRemoved: Int = 0
        private set
    var lineCombo: Int = -1
        private set

    var lastMoveRotation = TRotation.NONE
    var backToBack = false
        private set

    private var numBlockCells: Int = 0

    private fun cancelDropTimers() {
        suspendedSoftDrop?.cancel()
        suspendedHardDrop?.cancel()
    }

    private fun cleanUp() {
        cancelDropTimers()
    }

    private fun resetSoftDropTimer() {
        cancelDropTimers()
        suspendedSoftDrop = coroutineScope.launch {
            delay(softDropTimeout)
            softDrop()
        }
    }

    private fun resetHardDropTimer() {
        cancelDropTimers()
        suspendedHardDrop = coroutineScope.launch {
            delay(hardDropTimeout)
            hardDrop()
        }
    }

    private fun isValid(i: Int, j: Int): Boolean {
        if (i < 0 || i >= width || j < 0 || j >= paddedHeight) {
            return false
        }
        return board[i][j] == Cell.EMPTY.code
    }

    private fun isValid(position: Position): Boolean {
        return isValid(position.x, position.y)
    }

    private fun moveAvailable(position: Position, relPosArray: Array<Position>? = null): Boolean {
        val positions = relPosArray ?: currentBlock.getPositions()
        for (relPos in positions) {
            if (!isValid(position + relPos)) return false
        }
        return true
    }

    private fun isKO(): Boolean {
        return !moveAvailable(startPosition)
    }

    private fun onGround(): Boolean {
        return !moveAvailable(currentPosition + Position(0, -1))
    }

    private fun runGravity() {
        if (onGround()) {
            resetHardDropTimer()
        } else {
            resetSoftDropTimer()
        }
    }

    private fun startDrop() {
        if (isKO()) {
            numKnockouts++
            messageChannel.offer(TetrisMessage.Knockout(numKnockouts))
            if (numKnockouts >= knockoutLimit || trashLines == 0) {
                cleanUp()
                gameState = GameState.STOPPED
                tetrisBattleController?.gameOver(this)
                return
            }
            gameState = GameState.KNOCKOUT
            coroutineScope.launch {
                delay(knockoutTimeout)
                mutex.withLock {
                    removeTrashLines(trashLines)
                    gameState = GameState.RUNNING
                    startDrop()
                }
            }
        } else {
            currentPosition = startPosition
            lockDownCount = 0
            lockDownActivated = false
            lastMoveRotation = TRotation.NONE
            messageChannel.offer(TetrisMessage.Move(currentBlock.copyOf(), currentPosition))
            runGravity()
        }
    }

    private fun getTSpinState(): TSpinState {
        if (currentBlock !is TetrominoT || lastMoveRotation == TRotation.NONE) return TSpinState.NONE
        var occupied = 0
        for (i in -1..1 step 2) {
            for (j in -1..1 step 2) {
                if (!isValid(currentPosition + Position(i, j))) occupied++
            }
        }
        if (occupied < 3) return TSpinState.NONE
        val pointingPosition = (currentBlock as TetrominoT).getPointingPosition()
        occupied = 0
        val adjacentList = listOf(
            Position(1, 0),
            Position(0, 1),
            Position(-1, 0),
            Position(0, -1)
        )
        for (relPos in adjacentList) {
            if (!isValid(currentPosition + pointingPosition + relPos)) occupied++
        }
        return if (occupied == 1 && lastMoveRotation == TRotation.NORMAL) TSpinState.T_SPIN_MINI else TSpinState.T_SPIN
    }

    private fun lineSweep(tSpinState: TSpinState): Pair<Int, Int> {
        var minY = Int.MAX_VALUE
        var maxY = Int.MIN_VALUE
        val positions = currentBlock.getPositions()
        for (relPos in positions) {
            minY = min(minY, currentPosition.y + relPos.y)
            maxY = max(maxY, currentPosition.y + relPos.y)
        }
        var numLines = 0
        val lineShiftList = ArrayList<Pair<Int, Int>>() 
        for (y in minY..maxY) {
            var swept = true
            for (x in 0 until width) {
                if (board[x][y] == Cell.EMPTY.code) {
                    swept = false
                    break
                }
            }
            if (swept) {
                numLines++
            } else if (numLines > 0) {
                lineShiftList.add(Pair(numLines, y))
            }
        }
        for (lineShiftInfo in lineShiftList) {
            val lines = lineShiftInfo.first
            val y = lineShiftInfo.second
            for (x in 0 until width) {
                board[x][y - lines] = board[x][y]
            }
        }
        var trashSent = 0
        if (numLines > 0) {
            for (x in 0 until width) {
                for (y in maxY + 1 until paddedHeight) {
                    board[x][y - numLines] = board[x][y]
                }
                for (y in paddedHeight - numLines until paddedHeight) {
                    board[x][y] = Cell.EMPTY.code
                }
            }

            numBlockCells -= numLines * width
            lineCombo++

            // Combo
            if (lineCombo > 0) trashSent += min((lineCombo - 1) / 2 + 1, 4)

            // Perfect clear
            if (numBlockCells == 0) trashSent += 10

            // Extra
            trashSent += when (numLines) {
                1 -> when (tSpinState) {
                    TSpinState.T_SPIN -> if (backToBack) 3 else 2
                    TSpinState.T_SPIN_MINI -> if (backToBack) 2 else 1
                    else -> 0
                }
                2 -> when (tSpinState) {
                    TSpinState.NONE -> 1
                    else -> if (backToBack) 6 else 4
                }
                3 -> when (tSpinState) {
                    TSpinState.NONE -> 2
                    else -> if (backToBack) 9 else 6
                }
                4 -> if (backToBack) 6 else 4
                else -> 0
            }

            backToBack = tSpinState != TSpinState.NONE || numLines == 4
        } else lineCombo = -1

        totalTrashSent += trashSent
        totalLinesRemoved += numLines
        return Pair(numLines, trashSent)
    }

    private fun stackTrash() {
        val truncatedTrashLines = min(queuedTrashLines, height - trashLines)
        for (x in 0 until width) {
            for (y in paddedHeight - 1 downTo truncatedTrashLines) {
                board[x][y] = board[x][y - truncatedTrashLines]
            }
            for (y in 0 until truncatedTrashLines) {
                board[x][y] = Cell.TRASH.code
            }
        }
        trashLines += truncatedTrashLines
        queuedTrashLines = 0
    }

    private fun removeTrashLines(numLines: Int) {
        for (x in 0 until width) {
            for (y in trashLines - numLines until paddedHeight - numLines) {
                board[x][y] = board[x][y + numLines]
            }
        }
        trashLines -= numLines
    }

    private fun lockDown() {
        val tSpinState = getTSpinState()
        val positions = currentBlock.getPositions()
        for (relPos in positions) {
            val position = currentPosition + relPos
            board[position.x][position.y] = currentBlock.getCode()
        }
        numBlockCells += positions.size
        val sweepResult = lineSweep(tSpinState)
        var trashSent = sweepResult.second

        val compensatedTrash = min(trashSent, trashLines)
        trashSent -= compensatedTrash
        if (compensatedTrash > 0) removeTrashLines(compensatedTrash)

        val compensatedQueuedTrash = min(trashSent, queuedTrashLines)
        trashSent -= compensatedQueuedTrash
        queuedTrashLines -= compensatedQueuedTrash

        if (trashSent > 0) tetrisBattleController?.sendTrashLines(this, trashSent)
        if (queuedTrashLines > 0) stackTrash()

        heldBefore = false
        val newBlock = queue.poll()
        messageChannel.offer(TetrisMessage.LockDown(currentBlock, currentPosition,
                                              newBlock.copyOf(), queue.lastPreviewBlock().copyOf(),
                                              sweepResult.second))
        currentBlock = newBlock

        startDrop()
    }

    private fun checkLockDown(moved: Boolean = true): Boolean {
        if (onGround()) {
            if (lockDownActivated) {
                if (lockDownCount >= lockDownLimit) {
                    lockDown()
                    return true
                } else if (moved) {
                    lockDownCount++
                }
            } else {
                lockDownActivated = true
            }
        } else if (lockDownActivated && moved) {
            lockDownCount++
        }
        return false
    }

    private fun moveTo(position: Position, onGroundBefore: Boolean) {
        currentPosition = position
        messageChannel.offer(TetrisMessage.Move(currentBlock.copyOf(), currentPosition))
        if (!checkLockDown(moved = true)) {
            val onGroundNow = onGround()
            if (onGroundBefore && !onGroundNow) {
                resetSoftDropTimer()
            } else if (onGroundNow) {
                resetHardDropTimer()
            }
        }
    }

    private fun tryMoveTo(position: Position): Boolean {
        if (moveAvailable(position)) {
            moveTo(position, onGroundBefore = onGround())
            return true
        }
        return false
    }

    private fun isBlock(i: Int, j: Int): Boolean {
        return board[i][j] != Cell.EMPTY.code && board[i][j] != Cell.TRASH.code
    }

    private fun isBlock(position: Position): Boolean {
        return isBlock(position.x, position.y)
    }

    suspend fun start() = mutex.withLock {
        if (gameState == GameState.READY) {
            gameState = GameState.RUNNING
            messageChannel.offer(TetrisMessage.Start(currentBlock.copyOf(), queue.getPreviewArray()))
            startDrop()
        }
    }
    suspend fun readyAndStart(countDownMilis: Long) = mutex.withLock {
        if (gameState == GameState.READY) {
            messageChannel.offer(TetrisMessage.Ready(countDownMilis))
            coroutineScope.launch {
                delay(countDownMilis)
                start()
            }
        }
    }
    suspend fun softDrop() = mutex.withLock {
        if (gameState == GameState.RUNNING && !onGround()) {
            currentPosition += Position(0, -1)
            lastMoveRotation = TRotation.NONE
            messageChannel.offer(TetrisMessage.Move(currentBlock.copyOf(), currentPosition))
            if (!checkLockDown(moved = false)) runGravity()
        }
    }
    suspend fun hardDrop() = mutex.withLock {
        if (gameState == GameState.RUNNING) {
            while (!onGround()) {
                currentPosition += Position(0, -1)
            }
            lockDown()
        }
    }
    suspend fun moveLeft() = mutex.withLock {
        if (gameState == GameState.RUNNING) {
            val newPosition = currentPosition + Position(-1, 0)
            lastMoveRotation = TRotation.NONE
            tryMoveTo(newPosition)
        }
    }
    suspend fun moveRight() = mutex.withLock {
        if (gameState == GameState.RUNNING) {
            val newPosition = currentPosition + Position(1, 0)
            lastMoveRotation = TRotation.NONE
            tryMoveTo(newPosition)
        }
    }
    suspend fun rotateLeft() = mutex.withLock {
        if (gameState == GameState.RUNNING) {
            val positions = currentBlock.rotateLeft(dryRun = true)
            val rotationCandidates = currentBlock.getLeftRotationCandidates()
            for (rotationCandidate in rotationCandidates) {
                val newPosition = currentPosition + rotationCandidate
                if (moveAvailable(newPosition, positions)) {
                    val onGroundBefore = onGround()
                    currentBlock.rotateLeft(dryRun = false)
                    lastMoveRotation = if (rotationCandidate == Position(1, -2)) TRotation.TST
                    else TRotation.NORMAL
                    moveTo(newPosition, onGroundBefore)
                    return
                }
            }
        }
    }
    suspend fun rotateRight() = mutex.withLock {
        if (gameState == GameState.RUNNING) {
            val positions = currentBlock.rotateRight(dryRun = true)
            val rotationCandidates = currentBlock.getRightRotationCandidates()
            for (rotationCandidate in rotationCandidates) {
                val newPosition = currentPosition + rotationCandidate
                if (moveAvailable(newPosition, positions)) {
                    val onGroundBefore = onGround()
                    currentBlock.rotateRight(dryRun = false)
                    lastMoveRotation = if (rotationCandidate == Position(-1, -2)) TRotation.TST
                    else TRotation.NORMAL
                    moveTo(newPosition, onGroundBefore)
                    return
                }
            }
        }
    }
    suspend fun holdBlock() = mutex.withLock {
        if (gameState == GameState.RUNNING) {
            if (!heldBefore) {
                currentBlock.rotateIndex = 0
                val noBlockHeld = (heldBlock == null)
                val nextBlock = heldBlock ?: queue.poll()
                heldBlock = currentBlock
                heldBefore = true
                messageChannel.offer(TetrisMessage.Hold(currentBlock.copyOf(), nextBlock.copyOf(),
                                                  noBlockHeld, queue.lastPreviewBlock().copyOf()))
                currentBlock = nextBlock
                startDrop()
            }
        }
    }
    suspend fun trashReceived(trashLines: Int) = mutex.withLock {
        if (gameState == GameState.RUNNING || gameState == GameState.KNOCKOUT) {
            queuedTrashLines += trashLines
            messageChannel.offer(TetrisMessage.TrashReceived(trashLines))
        }
    }
    suspend fun gameOver(win: Boolean = false) = mutex.withLock {
        if (gameState != GameState.FINISHED && gameState != GameState.CLOSED) {
            cleanUp()
            gameState = GameState.FINISHED
            messageChannel.offer(TetrisMessage.Finished(win))
        }
    }
    suspend fun surrender() = mutex.withLock {
        if (gameState == GameState.READY || gameState == GameState.RUNNING || gameState == GameState.KNOCKOUT) {
            cleanUp()
            gameState = GameState.STOPPED
            tetrisBattleController?.gameOver(this)
        }
    }
    suspend fun printBoard() = mutex.withLock {
        for (j in height - 1 downTo 0) {
            for (i in 0 until width) {
                print(board[i][j])
            }
            println()
        }
    }
    suspend fun setCells(cells: Map<Position, Cell>) = mutex.withLock {
        cells.forEach {
            val wasBlock = isBlock(it.key)
            board[it.key.x][it.key.y] = it.value.code
            val nowBlock = isBlock(it.key)
            if (!wasBlock && nowBlock) numBlockCells++
            if (wasBlock && !nowBlock) numBlockCells--
        }
    }
    suspend fun setCell(i: Int, j: Int, cell: Cell) {
        setCells(mapOf(Position(i, j) to cell))
    }

    private fun copyBoard(): Array<IntArray> {
        return Array(width) {
            board[it].copyOf()
        }
    }

    suspend fun sendGameIfStarted(recipient: String) = mutex.withLock {
        /**
         * If the game state is [GameState.READY], we don't send the message simply because
         * we don't have to, and we shouldn't reveal the queue.
         */
        if (gameState != GameState.READY && gameState != GameState.CLOSED) {
            val message = TetrisMessage.Board(copyBoard(), queue.getPreviewArray(),
                                              heldBlock?.copyOf(), currentBlock.copyOf(),
                                              currentPosition, trashLines, totalTrashSent,
                                              numKnockouts, gameState)
            message.recipient = recipient
            messageChannel.offer(message)
        }
    }

    suspend fun close() = mutex.withLock {
        if (gameState == GameState.FINISHED) {
            gameState = GameState.CLOSED
            messageChannel.close()
        }
    }
}
