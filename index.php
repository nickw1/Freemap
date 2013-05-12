<?php
session_start();
$lon = (isset($_GET['lon'])) ? $_GET['lon']: -0.72;
$lat = (isset($_GET['lat'])) ? $_GET['lat']: 51.05;
$loggedIn = (isset($_SESSION['gatekeeper'])) ? "true": "false";
?>
<!DOCTYPE html>
<html>
<head>
<title>freemap-vmd : OS Open Data plus OSM footpaths</title>
<script type='text/javascript'>
var lat = <?php echo $lat; ?>;
var lon = <?php echo $lon; ?>;
var loggedIn = <?php echo $loggedIn; ?>;
</script>
<script type='text/javascript' src='freemap/js/main.js'></script>
<script type='text/javascript'
src='javascript/leaflet-0.5.1/dist/leaflet.js'>
</script>
<script type='text/javascript' 
src='javascript/proj4js/lib/proj4js-combined.js'>
</script>
<script type='text/javascript' 
src='fixmypaths/Proj4Leaflet/src/proj4leaflet.js'></script>
<script type='text/javascript' src='freemap/js/lib/Ajax.js'></script>
<script type='text/javascript' src='freemap/js/lib/Dialog.js'></script>
<script type='text/javascript' src='freemap/js/WRAddMgr.js'></script>
<script type='text/javascript' src='freemap/js/WRViewMgr.js'></script>
<script type='text/javascript' src='freemap/js/FeatureLoader.js'></script>
<script type='text/javascript' src='freemap/js/DistanceWidget.js'></script>
<script type='text/javascript' src='freemap/js/lib/SearchWidget.js'></script>
<script type='text/javascript' src='freemap/js/FootpathRenderer.js'></script>
<link rel='stylesheet' type='text/css' 
href='javascript/leaflet-0.5.1/dist/leaflet.css' />
<link rel='stylesheet' type='text/css' href='freemap/css/style.css' />
</head>
<body onload='init()'>

<?php write_sidebar(); ?>

<div id='main'>
<h1>freemap-vmd</h1>
	<div id="modebar"></div>

	<div id="map" style="width: 800px; height:600px; "></div>

	<div>
	Footpath data from:
	<select id='lyr'>
	<option>OpenStreetMap (ODBL)</option>
	<option>County Council open data (Open Government Licence)</option>
	</select>
	</div>

</div>
</body>
</html>

<?php
function write_sidebar($homepage=false)
{
?>
    <div id='sidebar'>

    <?php
    write_login();
    ?>


    <div>
    <?php
    write_milometer();
    ?>
    </div>

    <div id='search'></div>
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
        <a href='freemap/user.php?action=signup'>Sign up</a>
        </p>
        <?php
    }
    else
    {
        echo "<em>Logged in as $_SESSION[gatekeeper]</em>\n";
        echo "<a href='#' id='myroutes'>My routes</a> | ".
        "<a href='freemap/user.php?action=logout&redirect=".
            htmlentities($_SERVER['PHP_SELF'])."'>Log out</a> ";
    }
    echo "</div>";
}
