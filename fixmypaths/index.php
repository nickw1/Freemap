<?php

require_once('../lib/User.php');

session_start();

$lat = (isset($_GET['lat'])) ? $_GET['lat']: 51.055;
$lon = (isset($_GET['lon'])) ? $_GET['lon']: -1.32; 
$probid = isset($_GET['probid']) ? $_GET['probid']: 0;
    
?>

<!DOCTYPE html>
<html>
<head>
<title>FixMyPaths</title>
<link rel="stylesheet" type="text/css" href="css/fixmypaths.css" />

</script>
<script type='text/javascript' 
src='http://www.free-map.org.uk/javascript/proj4js/lib/proj4js-combined.js'>
</script>

<!-- NON KOTHIC
<script type='text/javascript' src='Proj4Leaflet/src/proj4leaflet.js'></script>
-->
<script type='text/javascript'
src='http://www.free-map.org.uk/javascript/leaflet-0.5.1/dist/leaflet.js'>
</script>

<link rel='stylesheet' type='text/css'
href='http://www.free-map.org.uk/javascript/leaflet-0.5.1/dist/leaflet.css' />


<script type='text/javascript' 
src='http://www.free-map.org.uk/javascript/kothic/dist/kothic.js'></script>
<script type='text/javascript' 
src='http://www.free-map.org.uk/javascript/kothic/dist/kothic-leaflet.js'>
</script>
<script type='text/javascript' src='hampshire.js'></script>



<script type='text/javascript' 
src='http://www.free-map.org.uk/0.6/js/lib/Ajax.js'></script>
<script type='text/javascript' 
src='http://www.free-map.org.uk/0.6/js/lib/Util.js'></script>
<script type='text/javascript' 
src='http://www.free-map.org.uk/0.6/js/lib/SearchWidget.js'></script>
<script type='text/javascript'>
var lat=<?php echo $lat; ?>;
var lon=<?php echo $lon; ?>;
var probid=<?php echo $probid; ?>;
</script>

<script type='text/javascript' src='main.js'> </script>
<script type='text/javascript' src='reports.js'> </script>
<script type='text/javascript' src='multipopup.js'> </script>


</head>

<body onload='init()'>
<div id='sidebar'>
<h1 class="sidebarheading">FixMyPaths!</h1>
<div id='blurb'>
<p>Report problems to your county council!*
Click on a right of way to report a problem</p> 
<p><em>*Hampshire only at the present time. West Sussex, Wiltshire
and Surrey problems can also be reported, but are 
not forwarded to the council.</em></p>
<p><strong>Disclaimer:</strong> An independent project, 
not officially affiliated with Hampshire County Council.</p>
<p>Purple=footpath; green=bridleway; red=byway; blue=restricted byway.</p>
</div>
<div id='appmsg'>Download the FixMyPaths
<strong>Android App</strong>
<a href='app.html'>here</a>!</div>
<?php
write_login();
?>

<div id='search'></div>

</div>
<div id='updates'>
<h1 class='sidebarheading'>Recent reports</h1>
<div id='reports'>
</div>
</div>
<div id='main'>

<div id="map" style="height:768px">
</div>

</div>
</body>
</html>

<?php
function write_login()
{
    echo "<div id='logindiv'>";

    if(!isset($_SESSION['gatekeeper']))
    {
        echo "<p>";
		echo '<form method="post" '.
		'action="user.php?action=login&redirect=index.php">';
		?>
        <label for="username">Username</label> <br/>
        <input name="username" id="username" /> <br/>
        <label for="password">Password</label> <br/>
        <input name="password" id="password" type="password" /> <br/>
        <input type='submit' value='go' id='loginbtn'/>
		</form>
        </p>
        <p>
        <a href=
		'user.php?action=signup&redirect=http://www.fixmypaths.org/index.php'>
		Sign up</a>
        </p>
        <?php
    }
    else
    {
        echo "<em>Logged in as $_SESSION[gatekeeper]</em> ";
		$conn=pg_connect("dbname=gis user=gis");
		$u = User::getUserFromUsername($_SESSION["gatekeeper"]);
		if($u->isAdmin())
			echo "<a href='admin.php'>Admin</a> ";
		echo "<a href='user.php?action=logout'>Logout</a>";
		pg_close($conn);
    }
    echo "</div>";
}
