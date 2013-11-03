<?php

require_once('Photosphere.php');

$photosphere = new Photosphere("/home/www-data/uploads/otvnew/test.jpg");

echo "Latitude " . $photosphere->getLatitude() .
	" Longitude " . $photosphere->getLongitude() .
	" hasPano ". $photosphere->hasGPano() . " PoseHeadingDegrees:".
	$photosphere->getGPanoAttribute("PoseHeadingDegrees");

?>
