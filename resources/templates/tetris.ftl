<#-- @ftlvariable name="data" type="xyz.joonhyung.tetris.IndexData" -->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Tetris Battle 2P</title>
    <style type="text/css">
        div.tetris {
            width: 1200px;
            height: 700px;
            margin: 30px auto;
        }
        div.tetris-home {
            width: 560px;
            height: 650px;
            float: left;
            position: relative;
        }
        div.tetris-away {
            width: 560px;
            height: 700px;
            float: right;
            position: relative;
        }
        div.ko {
            width: 100px;
            height: 100px;
            font-size: 60px;
            color: blue;
            text-align: center;
            position: absolute;
            top: 230px;
            left: 20px;
        }
        div.line-sent {
            width: 100px;
            height: 100px;
            font-size: 40px;
            text-align: center;
            position: absolute;
            top: 370px;
            left: 20px;
        }
        div.knockout {
            width: 360px;
            height: 180px;
            font-size: 70px;
            font-weight: bold;
            color: red;
            text-align: center;
            position:absolute;
            top: 250px;
            left: 100px;
            z-index: 1;
            visibility: hidden;
        }
        table.board {
            border: 10px solid #333;
            border-collapse: collapse; /* */
            position: absolute;
            top: 50px;
            left: 135px;
            z-index: 0;
        }
        td.board {
            width: 25px; height: 25px;
            background: #999;
            border: 1px solid #333; /* */
        }
        table.hold {
            border: 5px solid #999;
            border-collapse: collapse;
            position: absolute;
            top: 50px;
            left: 20px;
        }
        td.hold {
            width: 20px; height: 20px;
            background: black;
            border: 1px solid black; /* */
        }
        table.queue {
            border: 5px solid #999;
            border-collapse: collapse;
            position: absolute;
            top: 50px;
            left: 445px;
        }
        tbody.queue {
            margin: auto;
            border: 5px solid #999;
            border-collapse: collapse;
        }
        td.queue {
            width: 20px; height: 20px;
            background: black;
            border: 1px solid black; /* */
        }
    </style>
    <script type="text/javascript" src="/static/tetrominoes.js"></script>
    <script type="text/javascript" src="/static/tetris.js"></script>
</head>
<body>
    <button id="home-ready">Home Ready</button>
    <button id="away-ready">Away Ready</button>
    <button id="reset">Reset</button>
    <button id="left">left - ArrowLeft</button>
    <button id="right">right - ArrowRight</button>
    <button id="soft-drop">softDrop - ArrowDown</button>
    <button id="hard-drop">hardDrop - Space</button>
    <button id="rotate-right">rotateRight - ArrowUp</button>
    <button id="rotate-left">rotateLeft - ControlLeft</button>
    <button id="hold">hold - ShiftLeft</button>
    <div class="tetris" id="tetris" tabindex="0" autofocus>
        <div class="tetris-home">
            <table class="queue">
                <#list 0..4 as i>
                    <tbody class="queue">
                    <#list 3..0 as y>
                        <tr class="queue">
                            <#list 0..3 as x>
                                <td class="queue" id="home-queue-${i}-${x}-${y}">

                                </td>
                            </#list>
                        </tr>
                    </#list>
                    </tbody>
                </#list>
            </table>
            <table class="board">
                <#list 19..0 as y>
                    <tr class="board">
                        <#list 0..9 as x>
                            <td class="board" id="home-cell-${x}-${y}">

                            </td>
                        </#list>
                    </tr>
                </#list>
            </table>
            <table class="hold">
                <#list 3..0 as y>
                    <tr class="hold">
                        <#list 0..3 as x>
                            <td class="hold" id="home-hold-${x}-${y}">

                            </td>
                        </#list>
                    </tr>
                </#list>
            </table>
            <div class="ko" id="home-ko">
                0
            </div>
            <div class="line-sent" id="home-sent">
                0
            </div>
            <div class="knockout" id="home-knockout">
                KO!
            </div>
        </div>
        <div class="tetris-away">
            <table class="queue">
                <#list 0..4 as i>
                    <tbody class="queue">
                    <#list 3..0 as y>
                        <tr class="queue">
                            <#list 0..3 as x>
                                <td class="queue" id="away-queue-${i}-${x}-${y}">

                                </td>
                            </#list>
                        </tr>
                    </#list>
                    </tbody>
                </#list>
            </table>
            <table class="board">
                <#list 19..0 as y>
                    <tr class="board">
                        <#list 0..9 as x>
                            <td class="board" id="away-cell-${x}-${y}">

                            </td>
                        </#list>
                    </tr>
                </#list>
            </table>
            <table class="hold">
                <#list 3..0 as y>
                    <tr class="hold">
                        <#list 0..3 as x>
                            <td class="hold" id="away-hold-${x}-${y}">

                            </td>
                        </#list>
                    </tr>
                </#list>
            </table>
            <div class="ko" id="away-ko">
                0
            </div>
            <div class="line-sent" id="away-sent">
                0
            </div>
            <div class="knockout" id="away-knockout">
                KO!
            </div>
        </div>
    </div>
</body>
</html>