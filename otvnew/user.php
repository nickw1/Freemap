<?php

require_once('../lib/functionsnew.php');
require_once('../lib/UserController.php');
require_once('../lib/UserView.php');

session_start();

$conn = pg_connect("user=gis dbname=gis");

$view = new UserView ("OpenTrailView Signup/Login", "../css/otv.css");
$controller = new UserController($view);

$controller->execute ($_REQUEST);

pg_close($conn);

?>
