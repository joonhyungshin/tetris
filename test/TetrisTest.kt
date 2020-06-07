package xyz.joonhyung.tetris

import kotlin.test.*
import kotlinx.coroutines.*
import xyz.joonhyung.tetris.blocks.*

class TetrisTest {
    @Test
    fun testBoard() = runBlocking() {
        val board = TetrisBoard(this, softDropTimeout = 1L, hardDropTimeout = 1L)
        board.start()
        delay(1000)
    }

    @Test
    fun testTSpinDouble() = runBlocking() {
        val board = TetrisBoard(this)
        for (i in 0 until board.width) {
            for (j in 0..1) {
                board.setCell(i, j, TetrisBoard.Cell.OCCUPIED_I)
            }
        }
        board.setCells(mapOf(
            Position(4, 0) to TetrisBoard.Cell.EMPTY,
            Position(3, 1) to TetrisBoard.Cell.EMPTY,
            Position(4, 1) to TetrisBoard.Cell.EMPTY,
            Position(5, 1) to TetrisBoard.Cell.EMPTY,
            Position(3, 2) to TetrisBoard.Cell.OCCUPIED_I
        ))
        board.currentBlock = TetrominoT()
        board.start()
        board.rotateRight()
        while (board.currentPosition.y > 1) {
            board.softDrop()
        }
        board.rotateRight()
        board.hardDrop()
        board.surrender()
        assertEquals(2, board.totalLinesRemoved)
        assertEquals(4, board.totalTrashSent)
    }

    @Test
    fun testTSpinSingle() = runBlocking() {
        val board = TetrisBoard(this)
        for (i in 0 until board.width) {
            for (j in 0..1) {
                board.setCell(i, j, TetrisBoard.Cell.OCCUPIED_I)
            }
        }
        board.setCells(mapOf(
            Position(0, 0) to TetrisBoard.Cell.EMPTY,
            Position(4, 0) to TetrisBoard.Cell.EMPTY,
            Position(3, 1) to TetrisBoard.Cell.EMPTY,
            Position(4, 1) to TetrisBoard.Cell.EMPTY,
            Position(5, 1) to TetrisBoard.Cell.EMPTY,
            Position(3, 2) to TetrisBoard.Cell.OCCUPIED_I
        ))
        board.currentBlock = TetrominoT()
        board.start()
        board.rotateRight()
        while (board.currentPosition.y > 1) {
            board.softDrop()
        }
        board.rotateRight()
        board.hardDrop()
        board.surrender()
        assertEquals(1, board.totalLinesRemoved)
        assertEquals(2, board.totalTrashSent)
    }

    @Test
    fun testTSpinTriple() = runBlocking() {
        val board = TetrisBoard(this)
        for (i in 0 until board.width) {
            for (j in 0..2) {
                board.setCell(i, j, TetrisBoard.Cell.OCCUPIED_I)
            }
        }
        board.setCells(mapOf(
            Position(6, 0) to TetrisBoard.Cell.EMPTY,
            Position(6, 1) to TetrisBoard.Cell.EMPTY,
            Position(6, 2) to TetrisBoard.Cell.EMPTY,
            Position(7, 1) to TetrisBoard.Cell.EMPTY,
            Position(5, 3) to TetrisBoard.Cell.OCCUPIED_I,
            Position(5, 4) to TetrisBoard.Cell.OCCUPIED_I,
            Position(6, 4) to TetrisBoard.Cell.OCCUPIED_I
        ))
        board.currentBlock = TetrominoT()
        board.start()
        while (board.currentPosition.x < board.width - 2) {
            board.moveRight()
        }
        while (board.currentPosition.y > 3) {
            board.softDrop()
        }
        board.moveLeft()
        board.rotateRight()
        board.hardDrop()
        board.surrender()
        assertEquals(3, board.totalLinesRemoved)
        assertEquals(6, board.totalTrashSent)
    }

    @Test
    fun testBackToBackTetris() = runBlocking {
        val board = TetrisBoard(this)
        for (i in 0 until board.width - 1) {
            for (j in 0..7) {
                board.setCell(i, j, TetrisBoard.Cell.OCCUPIED_I)
            }
        }
        board.start()
        for (i in 0..1) {
            board.currentBlock = TetrominoI()
            board.rotateRight()
            while (board.currentPosition.x < board.width - 2) {
                board.moveRight()
            }
            board.hardDrop()
        }
        board.surrender()
        assertEquals(8, board.totalLinesRemoved)
        assertEquals(4 + 6 + 1 + 10, board.totalTrashSent)
    }

    @Test
    fun testZSpin() = runBlocking {
        val board = TetrisBoard(this)
        for (i in 0 until board.width) {
            board.setCell(i, 0, TetrisBoard.Cell.OCCUPIED_I)
        }
        board.setCells(mapOf(
            Position(7, 0) to TetrisBoard.Cell.EMPTY,
            Position(8, 0) to TetrisBoard.Cell.EMPTY,
            Position(8, 1) to TetrisBoard.Cell.OCCUPIED_I
        ))
        board.start()
        board.currentBlock = TetrominoZ()
        board.rotateLeft()
        while (board.currentPosition.x < board.width - 2) {
            board.moveRight()
        }
        while (board.currentPosition.y > 2) {
            board.softDrop()
        }
        board.rotateLeft()
        board.hardDrop()
        board.surrender()
        assertEquals(1, board.totalLinesRemoved)
        assertEquals(0, board.totalTrashSent)
    }

    @Test
    fun testZSpinTriple() = runBlocking {
        val board = TetrisBoard(this)
        for (i in 0 until board.width) {
            for (j in 0..2) {
                board.setCell(i, j, TetrisBoard.Cell.OCCUPIED_I)
            }
        }
        board.setCells(mapOf(
            Position(4, 0) to TetrisBoard.Cell.EMPTY,
            Position(4, 1) to TetrisBoard.Cell.EMPTY,
            Position(5, 1) to TetrisBoard.Cell.EMPTY,
            Position(5, 2) to TetrisBoard.Cell.EMPTY,
            Position(3, 3) to TetrisBoard.Cell.OCCUPIED_I
        ))
        board.start()
        board.currentBlock = TetrominoZ()
        while (board.currentPosition.y > 3) {
            board.softDrop()
        }
        board.rotateRight()
        board.hardDrop()
        board.surrender()
        assertEquals(3, board.totalLinesRemoved)
        assertEquals(2, board.totalTrashSent)
    }

    @Test
    fun testISpin() = runBlocking {
        val board = TetrisBoard(this)
        for (i in 0 until board.width - 3) {
            for (j in 0..1) {
                board.setCell(i, j, TetrisBoard.Cell.OCCUPIED_I)
            }
        }
        for (i in board.width - 3 until board.width) {
            for (j in 0..3) {
                board.setCell(i, j ,TetrisBoard.Cell.OCCUPIED_I)
            }
        }
        board.setCells(mapOf(
            Position(3, 0) to TetrisBoard.Cell.EMPTY,
            Position(4, 0) to TetrisBoard.Cell.EMPTY,
            Position(5, 0) to TetrisBoard.Cell.EMPTY,
            Position(6, 0) to TetrisBoard.Cell.EMPTY,
            Position(6, 1) to TetrisBoard.Cell.EMPTY
        ))
        board.start()
        board.currentBlock = TetrominoI()
        board.rotateLeft()
        while (board.currentPosition.x < 6) {
            board.moveRight()
        }
        while (board.currentPosition.y > 2) {
            board.softDrop()
        }
        board.rotateLeft()
        board.hardDrop()
        board.surrender()
        assertEquals(1, board.totalLinesRemoved)
        assertEquals(0, board.totalTrashSent)
    }

    @Test
    fun testLSpin() = runBlocking {
        val board = TetrisBoard(this)
        for (i in 0 until board.width) {
            for (j in 0..1) {
                board.setCell(i, j, TetrisBoard.Cell.OCCUPIED_I)
            }
        }
        board.setCells(mapOf(
            Position(3, 0) to TetrisBoard.Cell.EMPTY,
            Position(4, 0) to TetrisBoard.Cell.EMPTY,
            Position(5, 0) to TetrisBoard.Cell.EMPTY,
            Position(5, 1) to TetrisBoard.Cell.EMPTY
        ))
        board.start()
        board.currentBlock = TetrominoL()
        board.rotateLeft()
        board.moveRight()
        while (board.currentPosition.y > 1) {
            board.softDrop()
        }
        board.rotateRight()
        board.hardDrop()
        board.surrender()
        assertEquals(2, board.totalLinesRemoved)
        assertEquals(1 + 10, board.totalTrashSent)
    }

    @Test
    fun testTSpinMiniTypeOne() = runBlocking {
        val board = TetrisBoard(this)
        for (i in 0 until board.width) {
            board.setCell(i, 0, TetrisBoard.Cell.OCCUPIED_I)
        }
        board.setCells(mapOf(
            Position(6, 0) to TetrisBoard.Cell.EMPTY,
            Position(5, 1) to TetrisBoard.Cell.OCCUPIED_I,
            Position(5, 2) to TetrisBoard.Cell.OCCUPIED_I
        ))
        board.start()
        board.currentBlock = TetrominoT()
        while (board.currentPosition.x < 7) {
            board.moveRight()
        }
        while (board.currentPosition.y > 1) {
            board.softDrop()
        }
        board.rotateRight()
        board.hardDrop()
        board.surrender()
        assertEquals(1, board.totalLinesRemoved)
        assertEquals(1, board.totalTrashSent)
    }

    @Test
    fun testTSpinMiniTypeTwo() = runBlocking {
        val board = TetrisBoard(this)
        for (i in 0 until board.width) {
            for (j in 0..1) {
                board.setCell(i, j, TetrisBoard.Cell.OCCUPIED_I)
            }
        }
        board.setCells(mapOf(
            Position(7, 0) to TetrisBoard.Cell.EMPTY,
            Position(8, 0) to TetrisBoard.Cell.EMPTY,
            Position(8, 1) to TetrisBoard.Cell.EMPTY,
            Position(9, 0) to TetrisBoard.Cell.EMPTY,
            Position(9, 1) to TetrisBoard.Cell.EMPTY
        ))
        board.start()
        board.currentBlock = TetrominoT()
        board.rotateLeft()
        while (board.currentPosition.x < 9) {
            board.moveRight()
        }
        while (board.currentPosition.y > 1) {
            board.softDrop()
        }
        board.rotateRight()
        board.hardDrop()
        board.surrender()
        assertEquals(1, board.totalLinesRemoved)
        assertEquals(1, board.totalTrashSent)
    }

    @Test
    fun testTrash() = runBlocking {
        val board = TetrisBoard(this)
        board.start()
        board.trashReceived(5)
        board.hardDrop()
        board.surrender()
    }
}