<?php

require_once('lib/functionsnew.php');
require_once('common/defines.php');


    
?>
<!DOCTYPE html>
<html>
<head>
<title>FREEMAP</title>
<meta charset="utf-8" />
<link rel='stylesheet' type='text/css' href='fm/css/style.css' />
<link rel='stylesheet' type='text/css' href='css/style.css' />
<link rel='stylesheet' type='text/css' href='https://unpkg.com/leaflet@1.6.0/dist/leaflet.css' />
<script type='text/javascript' src='https://unpkg.com/leaflet@1.6.0/dist/leaflet.js'></script>
<script type='text/javascript' src='3rdparty/kothic-js/dist/kothic.js'></script>
<script type='text/javascript' src='3rdparty/kothic-js/dist/kothic-leaflet.js'></script>
<script type='text/javascript' src='map/style_new.js'></script>

<script type='text/javascript'
src='javascript/Leaflet.draw/dist/leaflet.draw.js'></script>
<link rel='stylesheet' type='text/css'
href='javascript/Leaflet.draw/dist/leaflet.draw.css' />

<script type='text/javascript' src='jslib/Dialog.js'></script>
<script type='text/javascript' src='jslib/SearchWidget.js'></script>


<script type='text/javascript' src='fm/js/main.js'></script>
<script type='text/javascript' src='fm/js/InformationFormatter.js'></script>
<script type='text/javascript' src='fm/js/FeatureLoader.js'> </script>
<script type='text/javascript' src='fm/js/WRViewMgr.js'> </script>


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
	annotatable
	OpenStreetMap-based maps of the countryside of England and Wales, 
	allowing you to add notes and walking routes (note that a login
	is no longer required, however additions to the map must be authorised
	by the administrator).  
	<strong><a href='fm/about.html'>More</a></strong> </p>


    <div id='appmsg'>
    <a href='/common/opentrail.html'>Android app</a>
    also available!</div>




    <div id='searchdiv'></div>
    </div>
    <?php
}

?>
