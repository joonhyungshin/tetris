// Just for easy copy-and-paste
function arrayOf(a, b, c, d) {
    return [a, b, c, d]
}

function PositionClass(x, y) {
    this.x = x;
    this.y = y;
}

function Position(x, y) {
    return new PositionClass(x, y)
}

function add(p1, p2) {
    return Position(p1.x + p2.x, p1.y + p2.y);
}

const posMap = {
    "xyz.joonhyung.tetris.blocks.TetrominoL": arrayOf(
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
    ),
    "xyz.joonhyung.tetris.blocks.TetrominoJ": arrayOf(
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
    ),
    "xyz.joonhyung.tetris.blocks.TetrominoI": arrayOf(
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
    ),
    "xyz.joonhyung.tetris.blocks.TetrominoO": arrayOf(
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
    ),
    "xyz.joonhyung.tetris.blocks.TetrominoT": arrayOf(
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
    ),
    "xyz.joonhyung.tetris.blocks.TetrominoS": arrayOf(
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
    ),
    "xyz.joonhyung.tetris.blocks.TetrominoZ": arrayOf(
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
}