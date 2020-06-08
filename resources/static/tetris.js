// Global variable to hold the websocket.
let socket = null;

const colorMap = {
    "xyz.joonhyung.tetris.blocks.TetrominoI": "lightblue",
    "xyz.joonhyung.tetris.blocks.TetrominoJ": "darkblue",
    "xyz.joonhyung.tetris.blocks.TetrominoL": "orange",
    "xyz.joonhyung.tetris.blocks.TetrominoO": "yellow",
    "xyz.joonhyung.tetris.blocks.TetrominoS": "green",
    "xyz.joonhyung.tetris.blocks.TetrominoZ": "red",
    "xyz.joonhyung.tetris.blocks.TetrominoT": "magenta",
    "trash": "lightgray",
    "blankCell": "#999",
    "blankQueue": "black",
    "blankHold": "black"
};

const cellMap = {
    0: "blankCell",
    1: "trash",
    2: "xyz.joonhyung.tetris.blocks.TetrominoJ",
    3: "xyz.joonhyung.tetris.blocks.TetrominoL",
    4: "xyz.joonhyung.tetris.blocks.TetrominoI",
    5: "xyz.joonhyung.tetris.blocks.TetrominoO",
    6: "xyz.joonhyung.tetris.blocks.TetrominoT",
    7: "xyz.joonhyung.tetris.blocks.TetrominoS",
    8: "xyz.joonhyung.tetris.blocks.TetrominoZ"
}

let knockout = {
    "home": 0,
    "away": 0
}
let lineSent = {
    "home": 0,
    "away": 0
}
let currentPosition = {
    "home": null,
    "away": null
};
let currentBlock = {
    "home": null,
    "away": null
};
let queuedTrash = {
    "home": 0,
    "away": 0
}
let stackedTrash = {
    "home": 0,
    "away": 0
}
let started = false;
let board = {
    "team": [],
    "away": []
}

function initBoard(team) {
    board[team] = [];
    for (let i = 0; i < 10; i++) {
        let tempBoard = []
        for (let j = 0; j < 60; j++) {
            tempBoard.push("blankCell");
            if (j < 20) setCell(team, i, j, "blankCell");
        }
        board[team].push(tempBoard)
    }
}

function initGame(team) {
    initBoard(team)
    knockout[team] = 0
    lineSent[team] = 0
    currentPosition[team] = null
    currentBlock[team] = null
    queuedTrash[team] = 0
    stackedTrash[team] = 0
}

function setCell(team, i, j, cell) {
    document.getElementById(team + "-cell-" + i + "-" + j).style.background = colorMap[cell];
}

function getQueueColor(team, i, x, y) {
    return document.getElementById(team + "-queue-" + i + "-" + x + "-" + y).style.background;
}

function setQueueColor(team, i, x, y, color) {
    document.getElementById(team + "-queue-" + i + "-" + x + "-" + y).style.background = color;
}

function setQueueCell(team, i, x, y, cell) {
    setQueueColor(team, i, x, y, colorMap[cell]);
}

function setupQueue(team, queue) {
    for (let i = 0; i < 5; i++) {
        const block = queue[i];
        for (let x = 0; x < 4; x++) {
            for (let y = 0; y < 4; y++) {
                setQueueCell(team, i, x, y, "blankQueue");
            }
        }
        for (const relPos of posMap[block.type][block.rotateIndex]) {
            const pos = add(Position(1, 2), relPos);
            setQueueCell(team, i, pos.x, pos.y, block.type);
        }
    }
}

function pollAndPush(team, block) {
    for (let i = 0; i < 4; i++) {
        for (let x = 0; x < 4; x++) {
            for (let y = 0; y < 4; y++) {
                setQueueColor(team, i, x, y, getQueueColor(team, i + 1, x, y));
            }
        }
    }
    for (let x = 0; x < 4; x++) {
        for (let y = 0; y < 4; y++) {
            setQueueCell(team, 4, x, y, "blankQueue");
        }
    }
    for (const relPos of posMap[block.type][block.rotateIndex]) {
        const pos = add(Position(1, 2), relPos);
        setQueueCell(team, 4, pos.x, pos.y, block.type);
    }
}

function setupBoard(team, intBoard) {
    for (let i = 0; i < 10; i++) {
        for (let j = 0; j < 60; j++) {
            board[team][i][j] = cellMap[intBoard[i][j]];
            if (j < 20) setCell(team, i, j, board[team][i][j]);
        }
    }
}

function setHoldCell(team, x, y, cell) {
    document.getElementById(team + "-hold-" + x + "-" + y).style.background = colorMap[cell];
}

function setupHold(team, block) {
    for (let x = 0; x < 4; x++) {
        for (let y = 0; y < 4; y++) {
            setHoldCell(team, x, y, "blankHold");
        }
    }
    for (const relPos of posMap[block.type][block.rotateIndex]) {
        const pos = add(Position(1, 2), relPos);
        setHoldCell(team, pos.x, pos.y, block.type);
    }
}

function isValid(x, y) {
    return x >= 0 && x < 10 && y >= 0 && y < 20;
}

function hideCurrentBlock(team) {
    const block = currentBlock[team];
    const position = currentPosition[team];
    if (block == null || position == null) return;
    for (const relPos of posMap[block.type][block.rotateIndex]) {
        const pos = add(position, relPos);
        if (isValid(pos.x, pos.y)) setCell(team, pos.x, pos.y, "blankCell");
    }
}

function showCurrentBlock(team) {
    const block = currentBlock[team];
    const position = currentPosition[team];
    if (block == null || position == null) return;
    for (const relPos of posMap[block.type][block.rotateIndex]) {
        const pos = add(position, relPos);
        if (isValid(pos.x, pos.y)) setCell(team, pos.x, pos.y, block.type);
    }
}

function min(x, y) {
    return x < y ? x : y;
}

function max(x, y) {
    return x > y ? x : y;
}

function lockDown(team, block, position) {
    let minY = 100;
    let maxY = -1;
    for (const relPos of posMap[block.type][block.rotateIndex]) {
        const pos = add(position, relPos);
        minY = min(minY, pos.y);
        maxY = max(maxY, pos.y);
        board[team][pos.x][pos.y] = block.type;
        if (isValid(pos.x, pos.y)) setCell(team, pos.x, pos.y, block.type);
    }
    let numLines = 0;
    let lineShiftList = [];
    for (let y = minY; y <= maxY; y++) {
        let swept = true;
        for (let x = 0; x < 10; x++) {
            if (board[team][x][y] === "blankCell") {
                swept = false;
                break;
            }
        }
        if (swept) numLines++;
        else if (numLines > 0) lineShiftList.push([numLines, y]);
    }
    for (const lineShiftInfo of lineShiftList) {
        const lines = lineShiftInfo[0];
        const y = lineShiftInfo[1];
        for (let x = 0; x < 10; x++) {
            board[team][x][y - lines] = board[team][x][y];
            if (isValid(x, y - lines)) setCell(team, x, y - lines, board[team][x][y]);
        }
    }
    if (numLines > 0) {
        for (let x = 0; x < 10; x++) {
            for (let y = maxY + 1; y < 60; y++) {
                board[team][x][y - numLines] = board[team][x][y];
                if (isValid(x, y - numLines)) setCell(team, x, y - numLines, board[team][x][y]);
            }
            for (let y = 60 - numLines; y < 60; y++) {
                board[team][x][y] = "blankCell";
                if (isValid(x, y)) setCell(team, x, y, "blankCell");
            }
        }
    }
}

function removeTrashLines(team, numLines) {
    for (let x = 0; x < 10; x++) {
        for (let y = stackedTrash[team] - numLines; y < 60 - numLines; y++) {
            board[team][x][y] = board[team][x][y + numLines];
            if (isValid(x, y)) setCell(team, x, y, board[team][x][y]);
        }
    }
    stackedTrash[team] -= numLines;
}

function stackTrash(team) {
    const truncatedTrashLines = min(queuedTrash[team], 20 - stackedTrash[team]);
    for (let x = 0; x < 10; x++) {
        for (let y = 59; y >= truncatedTrashLines; y--) {
            board[team][x][y] = board[team][x][y - truncatedTrashLines];
            if (isValid(x, y)) setCell(team, x, y, board[team][x][y]);
        }
        for (let y = 0; y < truncatedTrashLines; y++) {
            board[team][x][y] = "trash";
            if (isValid(x, y)) setCell(team, x, y, "trash");
        }
    }
    stackedTrash[team] += truncatedTrashLines;
    queuedTrash[team] = 0;
}

function handleReady(team, countDownMilis) {
    initGame(team)
    // Currently, do nothing.
}

function handleStart(team, block, queue) {
    started = true;
    currentBlock[team] = block;
    setupQueue(team, queue);
}

function handleMove(team, block, position) {
    if (started) {
        hideCurrentBlock(team);
        currentBlock[team] = block;
        currentPosition[team] = position;
        showCurrentBlock(team);
    }
}

function handleLockDown(team, block, position, newBlock, newQueuedBlock, trashSent) {
    if (started) {
        hideCurrentBlock(team);
        lockDown(team, block, position);
        lineSent[team] += trashSent;
        const compensatedTrash = min(trashSent, stackedTrash[team]);
        trashSent -= compensatedTrash;
        if (compensatedTrash > 0) removeTrashLines(team, compensatedTrash);
        const compensatedQueuedTrash = min(trashSent, queuedTrash[team]);
        trashSent -= compensatedQueuedTrash;
        queuedTrash[team] -= compensatedQueuedTrash;
        if (queuedTrash[team] > 0) stackTrash(team);

        currentBlock[team] = newBlock;
        currentPosition[team] = null;
        pollAndPush(team, newQueuedBlock);
    }
}

function handleHold(team, heldBlock, newBlock, noBlockHeld, lastQueuedBlock) {
    if (started) {
        hideCurrentBlock(team);
        setupHold(team, heldBlock);
        if (noBlockHeld) pollAndPush(team, lastQueuedBlock);
        currentBlock[team] = newBlock;
        currentPosition[team] = null;
    }
}

function handleTrashReceived(team, numLines) {
    if (started) queuedTrash[team] += numLines;
}

function handleFinished(team, win) {
    if (started && win) {
        alert("Team " + team + " wins!");
    }
}

function handleKnockout(team, knockoutCount) {
    if (started) {
        // TODO: this should be moved to lockdown
        knockout[team] = knockoutCount;
        removeTrashLines(team, stackedTrash[team]);
    }
}

function handleBoard(team, board, queue, heldBlock, block, position, trashLines, trashSent, gameState) {
    initGame(team)
    setupBoard(team, board);
    setupQueue(team, queue);
    if (heldBlock != null) setupHold(team, heldBlock);
    stackedTrash[team] = trashLines;
    currentBlock[team] = block;
    currentPosition[team] = position;
    lineSent[team] = trashSent;
    started = true;
    showCurrentBlock(team)
}

/**
 * This function is in charge of connecting the client.
 */
function connect() {
    // First we create the socket.
    // The socket will be connected automatically asap. Not now but after returning to the event loop,
    // so we can register handlers safely before the connection is performed.
    console.log("Begin connect");
    socket = new WebSocket("ws://" + window.location.host + "/tetris");

    // We set a handler that will be executed if the socket has any kind of unexpected error.
    // Since this is a just sample, we only report it at the console instead of making more complex things.
    socket.onerror = function() {
        console.log("socket error");
    };

    // We set a handler upon connection.
    // What this does is to put a text in the messages container notifying about this event.
    socket.onopen = function() {
        // write("Connected");
    };

    // If the connection was closed gracefully (either normally or with a reason from the server),
    // we have this handler to notify to the user via the messages container.
    // Also we will retry a connection after 5 seconds.
    socket.onclose = function(evt) {
        // Try to gather an explanation about why this was closed.
        // var explanation = "";
        // if (evt.reason && evt.reason.length > 0) {
        //     explanation = "reason: " + evt.reason;
        // } else {
        //     explanation = "without a reason specified";
        // }

        // Notify the user using the messages container.
        // write("Disconnected with close code " + evt.code + " and " + explanation);
        // Try to reconnect after 5 seconds.
        setTimeout(connect, 5000);
    };

    // If we receive a message from the server, we want to handle it.
    socket.onmessage = function(event) {
        received(event.data.toString());
    };
}

function handleMessage(response) {
    const team = response.boardName;
    switch (response.type) {
        case "xyz.joonhyung.tetris.TetrisBoard.Message.Ready":
            handleReady(team, response.countDownMilis);
            break;
        case "xyz.joonhyung.tetris.TetrisBoard.Message.Start":
            handleStart(team, response.block, response.queue);
            break;
        case "xyz.joonhyung.tetris.TetrisBoard.Message.Move":
            handleMove(team, response.block, response.position);
            break;
        case "xyz.joonhyung.tetris.TetrisBoard.Message.LockDown":
            handleLockDown(team, response.block, response.position,
                           response.newBlock, response.newQueuedBlock,
                           response.trashSent);
            break;
        case "xyz.joonhyung.tetris.TetrisBoard.Message.Hold":
            handleHold(team, response.heldBlock, response.newBlock,
                       response.noBlockHeld, response.lastQueuedBlock);
            break;
        case "xyz.joonhyung.tetris.TetrisBoard.Message.TrashReceived":
            handleTrashReceived(team, response.numLines);
            break;
        case "xyz.joonhyung.tetris.TetrisBoard.Message.Finished":
            handleFinished(team, response.win);
            break;
        case "xyz.joonhyung.tetris.TetrisBoard.Message.Knockout":
            handleKnockout(team, response.knockoutCount);
            break;
        case "xyz.joonhyung.tetris.TetrisBoard.Message.Board":
            handleBoard(team, response.board, response.queue,
                        response.heldBlock, response.block,
                        response.position, response.trashSent,
                        response.gameState);
            break;
        default:
            console.log(response.type);
    }
}

/**
 * Handle messages received from the sever.
 *
 * @param message The textual message
 */
function received(message) {
    try {
        const jsonResponse = JSON.parse(message)
        handleMessage(jsonResponse)
    } catch (e) {
        console.log(e)
    }
}

/**
 * Writes a message in the HTML 'messages' container that the user can see.
 *
 * @param message The message to write in the container
 */
function write(message) {
    // We first create an HTML paragraph and sets its class and contents.
    // Since we are using the textContent property.
    // No HTML is processed and every html-related character is escaped property. So this should be safe.
    var line = document.createElement("p");
    line.className = "message";
    line.textContent = message;

    // Then we get the 'messages' container that should be available in the HTML itself already.
    var messagesDiv = document.getElementById("messages");
    // We adds the text
    messagesDiv.appendChild(line);
    // We scroll the container to where this text is so the use can see it on long conversations if he/she has scrolled up.
    messagesDiv.scrollTop = line.offsetTop;
}

/**
 * Function in charge of sending the 'commandInput' text to the server via the socket.
 */
function onSend() {
    var input = document.getElementById("commandInput");
    // Validates that the input exists
    if (input) {
        var text = input.value;
        // Validates that there is a text and that the socket exists
        if (text && socket) {
            // Sends the text
            socket.send(text);
            // Clears the input so the user can type a new command or text to say
            input.value = "";
        }
    }
}

/**
 * The initial code to be executed once the page has been loaded and is ready.
 */
function start() {
    // First, we should connect to the server.
    connect();

    document.getElementById("tetris").addEventListener("keydown", function(e) {
        switch (e.code) {
            case "ArrowLeft":
                socket.send("left");
                break;
            case "ArrowRight":
                socket.send("right");
                break;
            case "ArrowUp":
                socket.send("rotateRight");
                break;
            case "ArrowDown":
                socket.send("softDrop");
                break;
            case "Space":
                socket.send("hardDrop");
                break;
            case "KeyZ":
                socket.send("rotateLeft");
                break;
            case "ShiftLeft":
                socket.send("hold");
                break;
        }
    });
    document.getElementById("home-ready").addEventListener("click", function(e) {
        socket.send("home");
        socket.send("ready");
        document.getElementById("home-ready").setAttribute("disabled", "disabled");
        document.getElementById("away-ready").setAttribute("disabled", "disabled");
    });
    document.getElementById("away-ready").addEventListener("click", function(e) {
        socket.send("away");
        socket.send("ready");
        document.getElementById("home-ready").setAttribute("disabled", "disabled");
        document.getElementById("away-ready").setAttribute("disabled", "disabled");
    });
}

function interfaceReady() {
    for (const team of ["home", "away"]) {
        for (let x = 0; x < 10; x++) {
            for (let y = 0; y < 20; y++) {
                if (!document.getElementById(team + "-cell-" + x + "-" + y)) return false;
            }
        }
        for (let x = 0; x < 4; x++) {
            for (let y = 0; y < 4; y++) {
                if (!document.getElementById(team + "-hold-" + x + "-" + y)) return false;
            }
        }
        for (let i = 0; i < 5; i++) {
            for (let x = 0; x < 4; x++) {
                for (let y = 0; y < 4; y++) {
                    if (!document.getElementById(team + "-queue-" + i + "-" + x + "-" + y)) return false;
                }
            }
        }
        if (!document.getElementById(team + "-ready")) return false;
    }
    return true;
}

/**
 * The entry point of the client.
 */
function initLoop() {
    // Is the sendButton available already? If so, start. If not, let's wait a bit and rerun this.
    if (interfaceReady()) {
        // setCell("home", 0, 0, "red");
        start();
    } else {
        setTimeout(initLoop, 300);
    }
}

// This is the entry point of the client.
setTimeout(initLoop, 1000);
