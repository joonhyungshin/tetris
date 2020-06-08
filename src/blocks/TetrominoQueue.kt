package xyz.joonhyung.tetris.blocks

import java.util.ArrayDeque

class TetrominoQueue(private val numTetrominoes: Int = 7, private val numTetrominoesPreview: Int = 5) {
    private val queue = ArrayDeque<Tetromino>()

    /**
     * Basic assumption: [numTetrominoesPreview] is very small.
     */
    private val previewQueue = ArrayDeque<Tetromino>()

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
        previewQueue.add(pollFromMainQueue())
        return previewQueue.poll()
    }

    fun lastPreviewBlock(): Tetromino {
        return previewQueue.last()
    }

    fun getPreviewArray(): ArrayList<Tetromino> {
        val previewArray = ArrayList<Tetromino>()
        previewQueue.forEach {
            previewArray.add(it.copyOf())
        }
        return previewArray
    }
}