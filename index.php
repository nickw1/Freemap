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

<script type='text/javascript' src='jslib/Dialog.js'></script>
<script type='text/javascript' src='jslib/SearchWidget.js'></script>

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
if(false)
{
	?>
	<body>
	<p>Freemap is temporarily unavailable due to some problems with the
	database when upgrading the server. It is hoped to be restored during
	this week. Also, OpenTrail 0.3 will temporarily be unable to download
	data from the Freemap server. 14/11/16</p>
	</body>
	<?php
}
else if(file_exists(POPULATE_LOCK))
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
	

	<!-- filled in by js using matchMedia() -->
	<div id='title'> </div>


    <p id='intro'>Welcome to <em>Freemap 0.7 (preview)</em>, 
    with kothic-js client-side rendering. 
    <a href='fm/about.html'>More...</a> </p>
	<p><strong>Important!</strong> Due to issues with the server OS
	upgrade, not all features may be available for the next few days. However
	the map and search do work. 18/11/16</p>

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
