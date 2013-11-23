<?php

require_once("PanoController.php");
require_once("PanoView.php");

session_start();
$conn=pg_connect("dbname=gis user=gis");


$view = new PanoView();
$controller = new PanoController($view);

$cleaned = clean_input($_REQUEST, "pgsql");
$code = $controller->action ($cleaned);
if($code!=200)
{
	header("HTTP/1.1 $code");
	echo "HTTP error $code";
}
pg_close($conn);
?>
