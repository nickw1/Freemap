<?php
require_once('../lib/FreemapUserController.php');
require_once('../lib/FreemapUserView.php');
require_once('../lib/functionsnew.php');

session_start();
$conn=new PDO ("pgsql:host=localhost;dbname=gis2", "gis");
$controller = new FreemapUserController
	(new FreemapUserView("User Control", "css/style.css"), $conn,
			"gatekeeper", "level");
$controller->execute ($_REQUEST);
?>
