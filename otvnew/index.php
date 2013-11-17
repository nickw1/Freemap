<?php
session_start();

$lat = isset($_GET["lat"]) ? $_GET["lat"] : 50.9;
$lon = isset($_GET["lon"]) ? $_GET["lon"] : -1.4;

// Change on other servers
define('FREEMAP_ROOT','http://www.free-map.org.uk');

?>

<!DOCTYPE html>
<html>
<head>
<title>opentrailview2 - virtual tours of the countryside using 
openstreetmap and photospheres</title>
<link rel='stylesheet' type='text/css' href='/css/otv.css' />
<link rel='stylesheet' type='text/css' 
href='<?=FREEMAP_ROOT ?>/javascript/leaflet-0.5.1/dist/leaflet.css' />
<script type='text/javascript' 
src='<?=FREEMAP_ROOT ?>/javascript/photosphere/lib/sphere.js'></script>
<script type='text/javascript' 
src='<?=FREEMAP_ROOT ?>/javascript/photosphere/extern/three.min.js'>
</script>
<script type='text/javascript' 
src='<?=FREEMAP_ROOT ?>/javascript/exif-js/binaryajax.js'></script>
<script type='text/javascript' 
src='<?=FREEMAP_ROOT ?>/javascript/exif-js/exif.js'></script>
<script type='text/javascript' 
src='<?=FREEMAP_ROOT ?>/javascript/leaflet-0.5.1/dist/leaflet.js'>
</script>
<script type='text/javascript' 
src='<?=FREEMAP_ROOT ?>/0.6/js/lib/Dialog.js'> </script>
<script type='text/javascript' 
src='<?=FREEMAP_ROOT ?>/0.6/js/lib/Ajax.js'> </script>
<script type='text/javascript' 
src='<?=FREEMAP_ROOT ?>/0.6/js/lib/Login.js'> </script>
<script type='text/javascript' 
src='<?=FREEMAP_ROOT ?>/0.6/js/lib/NominatimWidget.js'> </script>
<script type='text/javascript' 
src='<?=FREEMAP_ROOT ?>/0.6/js/FeatureLoader.js'> </script>
<script type='text/javascript' src='js/main.js'></script>
<script type='text/javascript'>

var HttpData = {}, SessionData= {};
HttpData.lat = <?php echo $lat; ?>;
HttpData.lon = <?php echo $lon; ?>;
SessionData.username = "<?php echo isset($_SESSION["gatekeeper"]) ? 
    $_SESSION["gatekeeper"] :""?>";
HttpData.panoId = <?php echo isset($_GET["id"]) && 
    ctype_digit($_GET["id"]) ?  $_GET["id"] : 0 ?>;
HttpData.getNearest = <?php echo isset($_GET["lat"]) && 
    isset($_GET["lon"]) ?  "true" : "false" ?>;


</script>
</head>
<body onload='init()'>

<div id="content">

    <h1>OpenTrailView2</h1>
    <div style="float: left">
	openstreetmap + photosphere = <em>opentrailview2</em>.
    <span id='signup'>
    <?php
    if(!isset($_SESSION["gatekeeper"]))
    {
        echo " <a href='user.php?action=signup'>Signup</a> | ";
    }
    ?>
    </span>
	<span><a href="about.html">More...</a></span>
    </div>

	<div id="search" style="float:right; position:relative;"></div>


    <div id="container"> 

        <div id="loginlink">
        <a href="#" id="login" class="panolink">Login</a>
        </div>

        <div id="commands">
		<a id="switcher" href="#" class="panolink">pano view</a>
		</div>

    </div>

</div>

</body>
</html>
