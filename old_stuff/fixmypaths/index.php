<?php

//die("Temporarily unavailable for today 07/10/14 - try tomorrow");

require_once('../lib/FreemapUserManager.php');

session_start();

$lat = (isset($_GET['lat'])) ? $_GET['lat']: 51.05;
$lon = (isset($_GET['lon'])) ? $_GET['lon']: -0.72; 
$probid = isset($_GET['probid']) ? $_GET['probid']: 0;
    
?>

<!DOCTYPE html>
<html>
<head>
<title>FixMyPaths</title>
<link rel="stylesheet" type="text/css" href="css/fixmypaths.css" />
</head>

<body>
<p>Please note that FixMyPaths is no longer being maintained. You can 
share footpath problems on <a href='http://www.free-map.org.uk'>Freemap</a>
instead.</p>
</body>
</html>
