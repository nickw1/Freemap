<?php


$lat = (isset($_GET['lat'])) ? $_GET['lat']: 51.05;
$lon = (isset($_GET['lon'])) ? $_GET['lon']: -0.9; 
$zoom = (isset($_GET['zoom'])) ? $_GET['zoom']: 14; 
    
?>

<!DOCTYPE html>
<html>
<head>
<title>OpenHants</title>
<style type='text/css'>
body { font-family: DejaVu Sans, verdana, helvetica, arial, sans-serif; }
a{ text-decoration: none; }
</style>
<script type='text/javascript' src='../0.6/Leaflet/dist/leaflet.js'></script>
<script type='text/javascript' src='../0.6/kothic/dist/kothic.js'></script>
<script type='text/javascript' 
src='../0.6/kothic/dist/kothic-leaflet.js'></script>
<script type='text/javascript' src='hampshire.js'></script>
<link rel='stylesheet' type='text/css' 
href='../0.6/Leaflet/dist/leaflet.css' />
<script type='text/javascript' src='../0.6/js/lib/Ajax.js'></script>
<script type='text/javascript' src='../0.6/js/lib/Dialog.js'></script>
<script type='text/javascript'>
var lat=<?php echo $lat; ?>;
var lon=<?php echo $lon; ?>;
var zoom=<?php echo $zoom;?>;
</script>
<script type='text/javascript' src='/js/proj4js/lib/proj4js-combined.js'>
</script>

<script type='text/javascript' src='main.js'> </script>


<link rel='alternate' type='application/rss+xml'
title='The Freemap blog, revisited' href='/wordpress/' />

</head>

<body onload='init()'>
<div id='main'>

<h1>OpenHants</h1>
<p>A <a href='http://www.free-map.org.uk'>Freemap</a> project. Not 
officially affiliated with Hampshire County Council, but using their new
open right of way data.</p>
<p>Green=footpath; brown=bridleway; red=byway; purple=restricted byway.</p>
<div id="map" style="width:1024px; height:768px"></div>

</div>
</body>
</html>
