package xyz.joonhyung.tetris

import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.util.concurrent.atomic.*

class TetrisBattleController(private val coroutineScope: CoroutineScope, val countDownMilis: Long = 3000) {
    var homeBoard = TetrisBoard(coroutineScope, this)
    var awayBoard = TetrisBoard(coroutineScope, this)

    private val finished = AtomicBoolean(false)

    suspend fun sendToClient(board: TetrisBoard) {
        for (message in board.messageChannel) {
            // TODO: Really send message to clients
            val json = Json(JsonConfiguration.Stable)
            val jsonString = json.stringify(TetrisBoard.Message.serializer(), message)
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