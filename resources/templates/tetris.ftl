<#-- @ftlvariable name="data" type="xyz.joonhyung.tetris.IndexData" -->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Tetris Battle 2P</title>
    <style type="text/css">
        div.tetris {
            width: 1200px;
            margin: auto;
        }
        div.tetris-home {
            width: 560px;
            float: left;
        }
        div.tetris-away {
            width: 560px;
            float: right;
        }
        div.hold {
            width: 120px;
            float: left;
        }
        div.board {
            width: 300px;
            float: left;
        }
        div.queue {
            width: 120px;
            float: left;
        }
        table.board {
            margin: 80px auto;
            border: 10px solid #333;
            border-collapse: collapse; /* */
        }
        td.board {
            width: 25px; height: 25px;
            background: #999;
            border: 1px solid #333; /* */
        }
        table.hold {
            margin: 80px auto;
            border: 5px solid #999;
            border-collapse: collapse;
        }
        td.hold {
            width: 20px; height: 20px;
            background: black;
            border: 1px solid black; /* */
        }
        table.queue {
            margin: 80px auto;
            border: 5px solid #999;
            border-collapse: collapse;
        }
        tbody.queue {
            margin: 80px auto;
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
    <div class="tetris" id="tetris" tabindex="0">
        <div class="tetris-home">
            <div class="hold">
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
            </div>
            <div class="board">
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
            </div>
            <div class="queue">
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
            </div>
        </div>
        <div class="tetris-away">
            <div class="hold">
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
            </div>
            <div class="board">
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
            </div>
            <div class="queue">
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
            </div>
        </div>
    </div>
</body>
</html>