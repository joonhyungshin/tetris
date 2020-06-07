package xyz.joonhyung.tetris.blocks

import kotlinx.serialization.*

@Serializable
data class Position(val x: Int, val y: Int) {
    operator fun plus(p: Position): Position {
        return Position(x + p.x, y + p.y)
    }

    operator fun minus(p: Position): Position {
        return Position(x - p.x, y - p.y)
    }
}

@Serializable
sealed class Tetromino {
    var rotateIndex = 0
    abstract fun getPositions(index: Int? = null): Array<Position>
    abstract fun getLeftRotationCandidates(): Array<Position>
    abstract fun getRightRotationCandidates(): Array<Position>
    fun rotateLeft(dryRun: Boolean = false): Array<Position> {
        val nextIndex = (rotateIndex + 3) % 4
        if (!dryRun) rotateIndex = nextIndex
        return getPositions(nextIndex)
    }
    fun rotateRight(dryRun: Boolean = false): Array<Position> {
        val nextIndex = (rotateIndex + 1) % 4
        if (!dryRun) rotateIndex = nextIndex
        return getPositions(nextIndex)
    }
}

@Serializable
class TetrominoL : Tetromino() {
    companion object {
        val positions = arrayOf(
                arrayOf(
                        Position(-1, 0),
                        Position(0, 0),
                        Position(1, 0),
                        Position(1, 1)
                ),
                arrayOf(
                        Position(0, 1),
                        Position(0, 0),
                        Position(0, -1),
                        Position(1, -1)
                ),
                arrayOf(
                        Position(1, 0),
                        Position(0, 0),
                        Position(-1, 0),
                        Position(-1, -1)
                ),
                arrayOf(
                        Position(0, -1),
                        Position(0, 0),
                        Position(0, 1),
                        Position(-1, 1)
                )
        )
        val rotationPositions = rotationPositionsTypeOne
    }

    override fun getPositions(index: Int?): Array<Position> { return positions[index ?: rotateIndex] }
    override fun getLeftRotationCandidates(): Array<Position> { return rotationPositions[rotateIndex][0] }
    override fun getRightRotationCandidates(): Array<Position> { return rotationPositions[rotateIndex][1] }
}
@Serializable
class TetrominoJ : Tetromino() {
    companion object {
        val positions = arrayOf(
                arrayOf(
                        Position(-1, 1),
                        Position(-1, 0),
                        Position(0, 0),
                        Position(1, 0)
                ),
                arrayOf(
                        Position(1, 1),
                        Position(0, 1),
                        Position(0, 0),
                        Position(0, -1)
                ),
                arrayOf(
                        Position(1, -1),
                        Position(1, 0),
                        Position(0, 0),
                        Position(-1, 0)
                ),
                arrayOf(
                        Position(-1, -1),
                        Position(0, -1),
                        Position(0, 0),
                        Position(0, 1)
                )
        )
        val rotationPositions = rotationPositionsTypeOne
    }

    override fun getPositions(index: Int?): Array<Position> { return positions[index ?: rotateIndex] }
    override fun getLeftRotationCandidates(): Array<Position> { return rotationPositions[rotateIndex][0] }
    override fun getRightRotationCandidates(): Array<Position> { return rotationPositions[rotateIndex][1] }
}
@Serializable
class TetrominoI : Tetromino() {
    companion object {
        val positions = arrayOf(
                arrayOf(
                        Position(-1, 0),
                        Position(0, 0),
                        Position(1, 0),
                        Position(2, 0)
                ),
                arrayOf(
                        Position(1, 1),
                        Position(1, 0),
                        Position(1, -1),
                        Position(1, -2)
                ),
                arrayOf(
                        Position(2, -1),
                        Position(1, -1),
                        Position(0, -1),
                        Position(-1, -1)
                ),
                arrayOf(
                        Position(0, -2),
                        Position(0, -1),
                        Position(0, 0),
                        Position(0, 1)
                )
        )
        val rotationPositions = rotationPositionsTypeTwo
    }

    override fun getPositions(index: Int?): Array<Position> { return positions[index ?: rotateIndex] }
    override fun getLeftRotationCandidates(): Array<Position> { return rotationPositions[rotateIndex][0] }
    override fun getRightRotationCandidates(): Array<Position> { return rotationPositions[rotateIndex][1] }
}
@Serializable
class TetrominoO : Tetromino() {
    companion object {
        val positions = arrayOf(
                arrayOf(
                        Position(0, 0),
                        Position(1, 0),
                        Position(1, 1),
                        Position(0, 1)
                ),
                arrayOf(
                        Position(0, 0),
                        Position(1, 0),
                        Position(1, 1),
                        Position(0, 1)
                ),
                arrayOf(
                        Position(0, 0),
                        Position(1, 0),
                        Position(1, 1),
                        Position(0, 1)
                ),
                arrayOf(
                        Position(0, 0),
                        Position(1, 0),
                        Position(1, 1),
                        Position(0, 1)
                )
        )
    }

    override fun getPositions(index: Int?): Array<Position> { return positions[index ?: rotateIndex] }
    override fun getLeftRotationCandidates(): Array<Position> { return arrayOf(Position(0, 0)) }
    override fun getRightRotationCandidates(): Array<Position> { return arrayOf(Position(0, 0)) }
}
@Serializable
class TetrominoT : Tetromino() {
    companion object {
        val positions = arrayOf(
                arrayOf(
                        Position(0, 0),
                        Position(-1, 0),
                        Position(0, 1),
                        Position(1, 0)
                ),
                arrayOf(
                        Position(0, 0),
                        Position(0, 1),
                        Position(1, 0),
                        Position(0, -1)
                ),
                arrayOf(
                        Position(0, 0),
                        Position(1, 0),
                        Position(0, -1),
                        Position(-1, 0)
                ),
                arrayOf(
                        Position(0, 0),
                        Position(0, -1),
                        Position(-1, 0),
                        Position(0, 1)
                )
        )
        val rotationPositions = rotationPositionsTypeOne
    }

    override fun getPositions(index: Int?): Array<Position> { return positions[index ?: rotateIndex] }
    override fun getLeftRotationCandidates(): Array<Position> { return rotationPositions[rotateIndex][0] }
    override fun getRightRotationCandidates(): Array<Position> { return rotationPositions[rotateIndex][1] }
    fun getPointingPosition(): Position { return positions[rotateIndex][2] }
}
@Serializable
class TetrominoS : Tetromino() {
    companion object {
        val positions = arrayOf(
                arrayOf(
                        Position(-1, 0),
                        Position(0, 0),
                        Position(0, 1),
                        Position(1, 1)
                ),
                arrayOf(
                        Position(0, 1),
                        Position(0, 0),
                        Position(1, 0),
                        Position(1, -1)
                ),
                arrayOf(
                        Position(1, 0),
                        Position(0, 0),
                        Position(0, -1),
                        Position(-1, -1)
                ),
                arrayOf(
                        Position(0, -1),
                        Position(0, 0),
                        Position(-1, 0),
                        Position(-1, 1)
                )
        )
        val rotationPositions = rotationPositionsTypeOne
    }

    override fun getPositions(index: Int?): Array<Position> { return positions[index ?: rotateIndex] }
    override fun getLeftRotationCandidates(): Array<Position> { return rotationPositions[rotateIndex][0] }
    override fun getRightRotationCandidates(): Array<Position> { return rotationPositions[rotateIndex][1] }
}
@Serializable
class TetrominoZ : Tetromino() {
    companion object {
        val positions = arrayOf(
                arrayOf(
                        Position(-1, 1),
                        Position(0, 1),
                        Position(0, 0),
                        Position(1, 0)
                ),
                arrayOf(
                        Position(1, 1),
                        Position(1, 0),
                        Position(0, 0),
                        Position(0, -1)
                ),
                arrayOf(
                        Position(1, -1),
                        Position(0, -1),
                        Position(0, 0),
                        Position(-1, 0)
                ),
                arrayOf(
                        Position(-1, -1),
                        Position(-1, 0),
                        Position(0, 0),
                        Position(0, 1)
                )
        )
        val rotationPositions = rotationPositionsTypeOne
    }

    override fun getPositions(index: Int?): Array<Position> { return positions[index ?: rotateIndex] }
    override fun getLeftRotationCandidates(): Array<Position> { return rotationPositions[rotateIndex][0] }
    override fun getRightRotationCandidates(): Array<Position> { return rotationPositions[rotateIndex][1] }
}