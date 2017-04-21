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
<meta charset="utf-8" />
<link rel='stylesheet' type='text/css' href='fm/css/style.css' />
<script type='text/javascript' 
src='javascript/leaflet-0.7/leaflet.js'></script>
<?php
$kv = isset($_GET["kv"]) && ctype_digit($_GET["kv"]) && 
		is_dir("/var/www/javascript/kothic/$_GET[kv]") ? 
		$_GET["kv"] : 16;

?>
<script type='text/javascript' 
src='../javascript/kothic/<?php echo $kv; ?>/dist/kothic.js'>
</script>
<script type='text/javascript' 
src='../javascript/kothic/<?php echo $kv; ?>/dist/kothic-leaflet.js'></script>
<script type='text/javascript' 
src='../javascript/kothic/<?php echo $kv; ?>/dist/kothic-leaflet-clickable.js'>
</script>
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


<script type='text/javascript' src='fm/style_new.js?killcache=<?php echo time();?>'></script>

<script type='text/javascript' src='jslib/Dialog.js'></script>
<script type='text/javascript' src='jslib/SearchWidget.js'></script>

<script type='text/javascript'>
var loggedIn=<?php echo $loggedIn;?>;
</script>

<script type='text/javascript' src='fm/js/main.js'></script>
<script type='text/javascript' src='fm/js/InformationFormatter.js'></script>
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


	<div id='title'> 
		<div class='titlebox' id='titlebox'>
		<img src='fm/images/freemap_small.png' alt='Freenap logo' /> <br />
		</div>
	</div>


    <p id='intro'>Welcome to <em>Freemap</em>... 
	OpenStreetMap-based maps of the UK countryside, allowing 
	you to share footpath problems and walking routes. Now with
	field boundaries, stiles, gates and clickable POIs...
    <a href='fm/about.html'>More</a> </p>

    <div id='appmsg'>
    <a href='/common/opentrail.html'>Android app</a>
    also available!</div>


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
