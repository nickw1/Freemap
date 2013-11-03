<!DOCTYPE html>
<html>
<head>
<title>coming soon, opentrailview with photospheres...</title>
<link rel='stylesheet' type='text/css' href='css/otv.css' />
<script type='text/javascript' 
src='http://www.free-map.org.uk/javascript/photosphere/lib/sphere.js'></script>
<script type='text/javascript' 
src='http://www.free-map.org.uk/javascript/photosphere/extern/three.min.js'>
</script>
<script type='text/javascript' 
src='http://www.free-map.org.uk/javascript/exif-js/binaryajax.js'></script>
<script type='text/javascript' 
src='http://www.free-map.org.uk/javascript/exif-js/exif.js'></script>
<script type='text/javascript'>

function init()
{
	new Photosphere("PANO_20131026_143345.jpg").loadPhotosphere
		(document.getElementById("photosphere1"));
}
</script>
</head>
<body onload='init()'>
<h1>OpenTrailView2</h1>
<p>openstreetmap + photosphere = <em>opentrailview2</em>. coming soon...</p>
<div style="width:1024px; height:600px;"  id="photosphere1"></div>
</body>
</html>
