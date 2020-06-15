package xyz.joonhyung.tetris

import io.ktor.http.cio.websocket.WebSocketSession
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import xyz.joonhyung.tetris.blocks.Position
import xyz.joonhyung.tetris.blocks.Tetromino

@Serializable
sealed class TetrisMessage {
    var boardName: String? = null
    @Transient
    var recipient: String? = null

    @Serializable
    data class Ready(val countDownMilis: Long) : TetrisMessage()
    @Serializable
    data class Start(val block: Tetromino, val queue: ArrayList<Tetromino>) : TetrisMessage()
    @Serializable
    data class Move(val block: Tetromino, val position: Position) : TetrisMessage()
    @Serializable
    data class LockDown(val block: Tetromino, val position: Position,
                        val newBlock: Tetromino, val newQueuedBlock: Tetromino,
                        val trashSent: Int) : TetrisMessage()
    @Serializable
    data class Hold(val heldBlock: Tetromino, val newBlock: Tetromino,
                    val noBlockHeld: Boolean, val lastQueuedBlock: Tetromino
    ) : TetrisMessage()
    @Serializable
    data class TrashReceived(val numLines: Int) : TetrisMessage()
    @Serializable
    data class Finished(val win: Boolean) : TetrisMessage()
    @Serializable
    data class Knockout(val knockoutCount: Int) : TetrisMessage()
    @Serializable
    data class Board(val board: Array<IntArray>, val queue: ArrayList<Tetromino>,
                     val heldBlock: Tetromino?, val block: Tetromino,
                     val position: Position, val trashLines: Int, val trashSent: Int,
                     val knockoutCount: Int, val gameState: TetrisBoard.GameState
    ) : TetrisMessage()

    @Serializable
    class Reset : TetrisMessage()

    @Serializable
    data class BoardUser(val homeUser: String?, val homeIsReady: Boolean,
                         val awayUser: String?, val awayIsReady: Boolean) : TetrisMessage()
}