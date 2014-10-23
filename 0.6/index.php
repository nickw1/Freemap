<?php

require_once('../lib/functionsnew.php');
require_once('../common/defines.php');


session_start();

$conn=pg_connect(pgconnstring());

$lat = (isset($_GET['lat'])) ? $_GET['lat']: 
    (isset($_GET['y']) ? $_GET["y"]/1000000 : "null");
$lon = (isset($_GET['lon'])) ? $_GET['lon']: 
    (isset($_GET['x']) ? $_GET["x"]/1000000 : "null");
$zoom = (isset($_GET['zoom'])) ? $_GET['zoom']: "null";
$loggedIn = (isset($_SESSION['gatekeeper'])) ? "true": "false";
    
?>
<!DOCTYPE html>
<html>
<head>
<title>FREEMAP</title>
<link rel='stylesheet' type='text/css' href='css/style.css' />
<script type='text/javascript' 
src='../javascript/leaflet-0.5.1/dist/leaflet.js'>
</script>
<script type='text/javascript' src='../javascript/kothic/dist/kothic.js'>
</script>
<script type='text/javascript' 
src='../javascript/kothic/dist/kothic-leaflet.js'></script>
<script type='text/javascript' src='style.js'></script>
<link rel='stylesheet' type='text/css' 
href='../javascript/leaflet-0.5.1/dist/leaflet.css' />
<script type='text/javascript' src='js/lib/Util.js'></script>
<script type='text/javascript' src='js/lib/Ajax.js'></script>
<script type='text/javascript' src='js/lib/SearchWidget.js'></script>
<script type='text/javascript' src='js/lib/Dialog.js'></script>
<script type='text/javascript' src='js/lib/FreemapWidget.js'></script>
<script type='text/javascript'>
var lat=<?php echo $lat; ?>;
var lon=<?php echo $lon; ?>;
var zoom=<?php echo $zoom;?>;
var loggedIn=<?php echo $loggedIn;?>;
</script>

<script type='text/javascript' src='js/main.js'> </script>
<script type='text/javascript' src='js/WRAddMgr.js'> </script>
<script type='text/javascript' src='js/WRViewMgr.js'> </script>
<script type='text/javascript' src='js/FeatureLoader.js'> </script>
<script type='text/javascript' src='js/DistanceWidget.js'> </script>


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
    <body onload='init()'> 
    <?php write_sidebar(true); ?>

    <div id='main'>

    <div id="modebar"></div>

    <div id="map"></div>

    </div>

    </body>

    <?php
}
pg_close($conn);

?>

</html>

<?php
function write_sidebar($homepage=false)
{
?>
    <div id='sidebar'>

    <div class='titlebox'>
    <img src='images/freemap_small.png' alt='freemap_small' /><br/>
    </div>

    <p>Welcome to <em>Freemap 0.6</em>, 
    with kothic-js client-side rendering. 
    <a href='about.html'>More...</a> </p>

    <div id='appmsg'>
    <a href='/common/opentrail.html'>Android app</a>
    now available!</div>


    <?php
    write_login();
    ?>


    <div>
    <?php
    write_milometer();
    ?>
    </div>

    <div id='searchdiv'></div>
    </div>
    <?php
}

function write_milometer()
{
    echo "<div id='dist'><span id='units'>000</span>.".
            "<span id='tenths'>0</span>".
            "<select id='distUnits'>".
            "<option value='miles'>miles</option>".
            "<option value='km'>km</option></select></div>";
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
        <a href='user.php?action=signup'>Sign up</a>
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
        "<a href='user.php?action=logout&redirect=".
            htmlentities($_SERVER['PHP_SELF'])."'>Log out</a> ";
    }
    echo "</div>";
}
?>
