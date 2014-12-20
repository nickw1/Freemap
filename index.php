<?php

require_once('lib/functionsnew.php');
require_once('common/defines.php');


session_start();

$loggedIn = (isset($_SESSION['gatekeeper'])) ? "true": "false";
    
?>
<!DOCTYPE html>
<html>
<head>
<title>FREEMAP</title>
<link rel='stylesheet' type='text/css' href='fm/css/style.css' />
<link rel='stylesheet' type='text/css' href='fm/css/print.css' media="print" />
<script type='text/javascript' 
src='javascript/leaflet-0.7/leaflet.js'></script>
<?php
// Performance on Chrome of new kothic is significantly less good than old
if(strpos($_SERVER["HTTP_USER_AGENT"], "Chrome") !== false ||
	(isset($_GET["kv"]) && $_GET["kv"]=="11"))
{
?>
<!-- old kothic-->
<script type='text/javascript' src='../javascript/kothic/dist/kothic.js'>
</script>
<script type='text/javascript' 
src='../javascript/kothic/dist/kothic-leaflet.js'></script>

<?php
}
else
{
?>
<!-- new kothic-->
<script type='text/javascript' src='javascript/kothic-js/dist/kothic.js'>
</script>
<script type='text/javascript' 
src='javascript/kothic-js/dist/kothic-leaflet.js'></script>
<?php
}
?>
<link rel='stylesheet' type='text/css' 
href='javascript/leaflet-0.7/leaflet.css' />

<script type='text/javascript'
src='javascript/Leaflet.draw/dist/leaflet.draw.js'></script>
<link rel='stylesheet' type='text/css'
href='javascript/Leaflet.draw/dist/leaflet.draw.css' />
<!--
<script type='text/javascript'
src='http://leaflet.github.io/Leaflet.draw/leaflet.draw.js'></script>
<link rel='stylesheet' type='text/css'
href='http://leaflet.github.io/Leaflet.draw/leaflet.draw.css' />
-->


<script type='text/javascript' src='fm/style.js'></script>

<script type='text/javascript' src='fm/js/lib/Dialog.js'></script>
<script type='text/javascript' src='fm/js/lib/SearchWidget.js'></script>

<script type='text/javascript'>
var loggedIn=<?php echo $loggedIn;?>;
</script>

<script type='text/javascript' src='fm/js/main.js'></script>
<script type='text/javascript' src='fm/js/FeatureLoader.js'> </script>
<script type='text/javascript' src='fm/js/WRViewMgr.js'> </script>


<link rel='alternate' type='application/rss+xml'
title='The Freemap blog, revisited' href='/wordpress/' />

</head>

<?php
if(file_exists(POPULATE_LOCK))
{
    ?>
    <body>
    <p>The Freemap database is updated at 2am UK time every Wednesday and
    Freemap is unavailable while the update takes place. The update is
    taking place right now.
    It typically takes no more than 1hr30 so should be over by 4am....
    so please return later!</p>
    </body>
    <?php
}
else
{
    ?>
    <body onload='init(<?php echo $loggedIn;?>)'> 
    <?php write_sidebar(true); ?>

    <div id='main'>

    <div id="modebar"></div>

    <div id="map"></div>

    </div>

    </body>

    <?php
}

?>

</html>

<?php
function write_sidebar($homepage=false)
{
?>
    <div id='sidebar'>

    <div class='titlebox'>
    <img src='fm/images/freemap_small.png' alt='freemap_small' /><br/>
    </div>

    <p>Welcome to <em>Freemap 0.7 (preview)</em>, 
    with kothic-js client-side rendering. 
    <a href='fm/about.html'>More...</a> </p>

    <div id='appmsg'>
    <a href='/common/opentrail.html'>Android app</a>
    now available!</div>


    <?php
    write_login();
    ?>


    <div id='searchdiv'></div>
    </div>
    <?php
}

function write_login()
{
    echo "<div id='logindiv'>";

    if(!isset($_SESSION['gatekeeper']))
    {
        ?>
        <p>
        <label for="username">Username</label> <br/>
        <input name="username" id="username" /> <br/>
        <label for="password">Password</label> <br/>
        <input name="password" id="password" type="password" /> <br/>
        <input type='button' value='go' id='loginbtn'/>
        </p>
        <p>
        <a href='fm/user.php?action=signup'>Sign up</a>
        </p>
        <!--
        <p><em>Please note that logging in will place a cookie on your
        machine to identify you to the server. Please only proceed if
        you are happy with this.</em></p>
        -->
        <?php
    }
    else
    {
        echo "<em>Logged in as $_SESSION[gatekeeper]</em>\n";
        echo "<a href='#' id='myroutes'>My routes</a> | ".
        "<a href='/fm/user.php?action=logout&redirect=".
            htmlentities($_SERVER['PHP_SELF'])."'>Log out</a> ";
    }
    echo "</div>";
}
?>
