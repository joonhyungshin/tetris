<#-- @ftlvariable name="data" type="xyz.joonhyung.tetris.IndexData" -->
<html>
    <body>
        <ul>
        <#list data.items as item>
            <li>${item}</li>
        </#list>
        </ul>
    </body>
</html>
