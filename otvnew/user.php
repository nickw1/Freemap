<?php

require_once('../lib/functionsnew.php');
require_once('../lib/FreemapUserController.php');
require_once('../lib/FreemapUserView.php');

session_start();

$conn = new PDO("pgsql:host=localhost;dbname=gis", "gis"); 

$view = new FreemapUserView ("OpenTrailView Signup/Login", "css/otv.css");
$controller = new FreemapUserController($view, $conn, "gatekeeper", "level");

$controller->execute ($_REQUEST);

?>
