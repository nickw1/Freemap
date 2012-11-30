<?php


$lat = (isset($_GET['lat'])) ? $_GET['lat']: 51.055;
$lon = (isset($_GET['lon'])) ? $_GET['lon']: -1.32; 
$zoom = (isset($_GET['zoom'])) ? $_GET['zoom']: 14; 
    
?>

<!DOCTYPE html>
<html>
<head>
<title>FixMyPaths</title>
<style type='text/css'>
body { font-family: DejaVu Sans, verdana, helvetica, arial, sans-serif; }
a{ text-decoration: none; }
#sidebar { float: left; width: 256px; background-color: #c0c0ff; color: white;
position: fixed; height:100%; text-align: center }
#sidebar a { color: yellow; }
.sidebarheading { font-family: Liberation Serif, Bookman Old Style } 
#sidebar h1,h2,h3 { font-weight: normal; }
#main { margin-left: 288px; width: 768px; max-width: 1024px }
#search  { font-size: 75%; width:75%; margin-left: auto; margin-right: auto }
#search h1 { font-size: 100% }
#searchterm { width: 75%; }
#blurb { font-size: 75% }
#appmsg { background-color: white; color: black; border: 2px solid black; }
#appmsg strong { color: red ; }
#appmsg a { color: blue; }
#sidebar a#permalink { font-weight: bold; color: blue; }
</style>
<script type='text/javascript' src='freemap/Leaflet/dist/leaflet.js'></script>
<script type='text/javascript' src='freemap/kothic/dist/kothic.js'></script>
<script type='text/javascript' 
src='freemap/kothic/dist/kothic-leaflet.js'></script>
<script type='text/javascript' src='hampshire.js'></script>
<link rel='stylesheet' type='text/css' 
href='freemap/Leaflet/dist/leaflet.css' />
<script type='text/javascript' src='freemap/js/lib/Ajax.js'></script>
<script type='text/javascript' src='freemap/js/lib/Util.js'></script>
<script type='text/javascript' src='freemap/js/lib/Dialog.js'></script>
<script type='text/javascript' src='freemap/js/lib/SearchWidget.js'></script>
<script type='text/javascript'>
var lat=<?php echo $lat; ?>;
var lon=<?php echo $lon; ?>;
var zoom=<?php echo $zoom;?>;
</script>
<script type='text/javascript' src='rootjs/proj4js/lib/proj4js-combined.js'>
</script>

<script type='text/javascript' src='main.js'> </script>


<link rel='alternate' type='application/rss+xml'
title='The Freemap blog, revisited' href='http://www.free-map.org.uk/wordpress/'
/>

</head>

<body onload='init()'>
<div id='sidebar'>
<h1 class="sidebarheading">FixMyPaths!</h1>
<div id='search'> </div>
<div id='blurb'>
<p>A <a href='http://www.free-map.org.uk'>Freemap</a> project using
Hampshire County Council open data, allowing users to report problems
directly to the council.
Click on a right of way to report a problem (<em>real</em> problems
only!).</p>
<p><strong>Disclaimer:</strong> An independent project, 
not officially affiliated with Hampshire County Council.</p>
<p>Green=footpath; brown=bridleway; red=byway; purple=restricted byway.</p>
</div>
<div id='appmsg'><strong>WANTED!</strong> Testers for the
<a href='app.html'>FixMyPaths Android App</a>!</div>
<p><a href='#' id='permalink'>Permalink</a></p>
</div>
<div id='main'>

<div id="map" style="width:1024px; height:768px">
</div>

</div>
</body>
</html>
