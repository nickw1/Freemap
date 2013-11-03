<?php
session_start();

$lat = isset($_GET["lat"]) ? $_GET["lat"] : 51.05;
$lon = isset($_GET["lon"]) ? $_GET["lon"] : -0.72;
?>

<!DOCTYPE html>
<html>
<head>
<title>opentrailview2</title>
<link rel='stylesheet' type='text/css' href='/css/otv.css' />
<link rel='stylesheet' type='text/css' 
href='http://www.free-map.org.uk/javascript/leaflet-0.5.1/dist/leaflet.css' />
<script type='text/javascript' 
src='http://www.free-map.org.uk/javascript/photosphere/lib/sphere.js'></script>
<script type='text/javascript' 
src='http://www.free-map.org.uk/javascript/photosphere/extern/three.min.js'>
</script>
<script type='text/javascript' 
src='http://www.free-map.org.uk/javascript/exif-js/binaryajax.js'></script>
<script type='text/javascript' 
src='http://www.free-map.org.uk/javascript/exif-js/exif.js'></script>
<script type='text/javascript' 
src='http://www.free-map.org.uk/javascript/leaflet-0.5.1/dist/leaflet.js'>
</script>
<script type='text/javascript' 
src='http://www.free-map.org.uk/javascript/kothic/dist/kothic.js'>
</script>
<script type='text/javascript' 
src='http://www.free-map.org.uk/javascript/kothic/dist/kothic-leaflet.js'>
</script>
<script type='text/javascript' src='http://www.free-map.org.uk/0.6/style.js'>
</script>
<script type='text/javascript'>

var lat = <?php echo $lat; ?>;
var lon = <?php echo $lon; ?>;

function init()
{
	new Photosphere("../PANO_20131026_143345.jpg").loadPhotosphere
		(document.getElementById("photosphere1"));
}

</script>
</head>
<body onload='init()'>

<div id="container">

<h1>OpenTrailView2</h1>
<p style="float:left">openstreetmap + photosphere = <em>opentrailview2</em>.
<?php
if(isset($_SESSION["gatekeeper"]))
{
	echo " <a href='fileupload.php'>Upload photospheres...</a>";
}
?>
</p>

<p id="loginlink">
<?php
if(isset($_SESSION["gatekeeper"]))
{
	echo "Logged in as $_SESSION[gatekeeper]";
	echo ' <a href="user.php?action=logout&redirect=index.php">Logout</a>';
}
else
{
	echo '<a href="user.php?action=login&redirect=index.php">Login</a>';
}
?>
</p>

<div style="width:1024px; height:600px; clear:both" id="photosphere1"></div>


</div>
</body>
</html>
