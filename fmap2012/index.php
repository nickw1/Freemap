<?php

require_once('/home/www-data/private/defines.php');

session_start();

$conn=pg_connect("dbname=gis user=gis");

$lat = (isset($_GET['lat'])) ? $_GET['lat']: 51.05;
$lon = (isset($_GET['lon'])) ? $_GET['lon']: -0.72;
$zoom = (isset($_GET['zoom'])) ? $_GET['zoom']: 14;
    
?>

<html>
<head>
<title>FREEMAP 0.6 - EXPERIMENTAL!</title>
<link rel='stylesheet' type='text/css' href='css/style.css' />
<script type='text/javascript' src='leaflet/leaflet.js'></script>
<script type='text/javascript' src='kothic/dist/kothic.js'></script>
<script type='text/javascript' src='kothic/dist/kothic-leaflet.js'></script>
<script type='text/javascript' src='style.js'></script>
<link rel='stylesheet' type='text/css' href='leaflet/leaflet.css' />
<script type='text/javascript' src='js/lib/Ajax.js'></script>
<script type='text/javascript' src='js/lib/SearchWidget.js'></script>
<script type='text/javascript'>
var lat=<?php echo $lat; ?>;
var lon=<?php echo $lon; ?>;
var zoom=<?php echo $zoom;?>;
</script>

<script type='text/javascript' src='js/main.js'> </script>


<link rel='alternate' type='application/rss+xml'
title='The Freemap blog, revisited' href='/wordpress/' />

</head>

<body onload='init()'>


<div id='main'>


<div id="map"> </div>

<?php write_sidebar(true); ?>
</div>


<?php

pg_close($conn);

?>

</body>
</html>

<?php
function write_sidebar($homepage=false)
{
?>
	<div id='sidebar'>

	<div class='titlebox'>
	<img src='/freemap/images/freemap_small.png' alt='freemap_small' /><br/>
	</div>

	<p>Welcome to <em>Freemap 0.6</em>, a new and experimental version of
	Freemap aiming to use kothic-js client-side rendering. First pass at
	rendering now done.
	<a href='/wordpress'>Blog</a></p>

	<p>On Linux, Chrome recommended.</p>

	<?php
	write_login();
	?>

	<div>
	<?php
	write_searchbar();
	write_milometer();
	?>
	</div>

	<div id='searchdiv'></div>
	</div>

	<?php
}

function write_searchbar()
{
}

function write_milometer()
{
}

function write_login()
{
	echo "<div id='logindiv'>";

	if(!isset($_SESSION['gatekeeper']))
	{
		echo "<form method='post' ".
		"action='user.php?action=login&redirect=".
			htmlentities($_SERVER['PHP_SELF'])."'>\n";
		?>
		<label for="username">Username</label> <br/>
		<input name="username" id="username" /> <br/>
		<label for="password">Password</label> <br/>
		<input name="password" id="password" type="password" /> <br/>
		<input type='submit' value='go' id='loginbtn'/>
		</form>
		<?php
	}
	else
	{
		echo "<em>Logged in as $_SESSION[gatekeeper]</em>\n";
		echo "<a href='/common/user.php?action=logout&redirect=".
			htmlentities($_SERVER['PHP_SELF'])."'>Log out</a> ";
	}
	echo "</div>";
}
?>
