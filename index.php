<?php

require_once('../lib/functionsnew.php');
require_once('common/defines.php');


    
?>
<!DOCTYPE html>
<html>
<head>
<title>FREEMAP</title>
<meta charset="utf-8" />
<link rel='stylesheet' type='text/css' href='fm/css/style.css' />
<link rel='stylesheet' type='text/css' href='css/style.css' />
<script type='text/javascript' src='https://unpkg.com/leaflet@1.3.4/dist/leaflet.js'></script>
<link rel='stylesheet' type='text/css' href='https://unpkg.com/leaflet@1.3.4/dist/leaflet.css' />
<script src='https://unpkg.com/tangram/dist/tangram.min.js'></script>
<script type='text/javascript'>
const lat = <?php echo isset($_GET["lat"]) && preg_match("/^[\d\.\-]+$/", $_GET["lat"]) ? $_GET["lat"] : 51.05; ?>;
const lon = <?php echo isset($_GET["lon"]) && preg_match("/^[\d\.\-]+$/", $_GET["lon"]) ? $_GET["lon"] : -0.72; ?>;
const zoom = <?php echo isset($_GET["zoom"]) && ctype_digit($_GET["zoom"]) ? $_GET["zoom"] : 14;?>;
</script>
<script src='tangram/js/bundle.js' defer></script>
</head>


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
    <body> 
    <?php write_topbar(true); ?>
    <div id='main'>

    <div id="map"></div>

    </div>

    </body>

    <?php
}

?>

</html>

<?php
function write_topbar($homepage=false)
{
?>
    <div id='topbar'>

		<div id='imgdiv' style='float:left; top:10px; position:relative; margin-right: 10px;'>
		<img src='fm/images/freemap_small.png' alt='Freenap logo' /> 
		</div>


    <div id='intro' style='margin-left:auto; margin-right:auto'>Welcome to <em>Freemap</em>... 
	OpenStreetMap-based maps of the countryside of England and Wales.
	<strong><a href='fm/about.html'>More</a></strong> </div>

	<div id='search'>
    <input id='q' />
	<input type='button' id='searchBtn' value='Search!'>
	</div>
	</div>

    </div>
    <?php
}

?>
