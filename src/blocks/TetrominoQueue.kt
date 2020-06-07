package xyz.joonhyung.tetris.blocks

import java.util.ArrayDeque
import kotlinx.serialization.*

@Serializable
class TetrominoQueue(private val numTetrominoes: Int = 7, private val numTetrominoesPreview: Int = 5) {
    @Transient private val queue = ArrayDeque<Tetromino>()

    /**
     * Basic assumption: [numTetrominoesPreview] is very small.
     */
    val previewQueue: ArrayList<Tetromino>

    fun addNewTetrominoes() {
        val tetrominoes = arrayListOf(
                TetrominoL(),
                TetrominoJ(),
                TetrominoI(),
                TetrominoO(),
                TetrominoT(),
                TetrominoS(),
                TetrominoZ()
        )
        tetrominoes.shuffle()
        queue.addAll(tetrominoes)
    }

    init {
        addNewTetrominoes()
        addNewTetrominoes()
        previewQueue = arrayListOf()
        for (i in 1..numTetrominoesPreview) {
            previewQueue.add(queue.poll())
        }
    }

    private fun pollFromMainQueue(): Tetromino {
        val nextBlock = queue.poll()
        if (queue.size <= numTetrominoes) {
            addNewTetrominoes()
        }
        return nextBlock
    }

    fun poll(): Tetromino {
        val firstTetromino = previewQueue[0]
        for (i in 0 until numTetrominoesPreview - 1) {
            previewQueue[i] = previewQueue[i + 1]
        }
        previewQueue[numTetrominoesPreview - 1] = pollFromMainQueue()
        return firstTetromino
    }

    fun lastPreviewBlock(): Tetromino {
        return previewQueue.last()
    }
}