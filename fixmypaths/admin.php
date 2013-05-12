<?php
session_start();

if(!isset($_SESSION["gatekeeper"]))
{
    header("Location: index.php");
}
else
{
    ?>
    <!DOCTYPE html>
    <html>
    <head>
    <script type='text/javascript' src='admin.js'></script>
    <script type='text/javascript' 
    src='http://www.free-map.org.uk/freemap/js/lib/Ajax.js'></script>
    <link rel='stylesheet' type='text/css' href='css/fixmypaths.css' />
    <style type='text/css'>
    #newlog 
    {
        width: 400px;
        height: 300px;
    }
    td
    {
        font-size: 90%;
    }
    </style>
    </head>
    <body onload='init()'>
    <h1>FixMyPaths Admin</h1>

    <div style="float:right"><a href='user.php?action=logout'>Logout</a></div>
    <div id="admincontainer">
    
    <label for="county">County:</label> 
    <select name="county" id="county"></select>
    <label for="district">District or Borough:</label> 
    <select name="district" id="district"></select>
    <label for="parish">Parish:</label> 
    <select name="parish" id="parish"></select>


    </div>

    </div>

    <div id="problems"></div>
    </body>
    </html>
    <?php
}

?>
