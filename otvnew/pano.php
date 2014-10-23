<?php

require_once("PanoController.php");
require_once("PanoView.php");

session_start();
$conn=new PDO("pgsql:host=localhost;dbname=gis", "gis");


// Yes I know this is crap, but I've PDO-ised the user code and don't
// have time to do the rest right now...
$oldconn = pg_connect("dbname=gis user=gis");

$view = new PanoView();
$controller = new PanoController($view, $conn);

$cleaned = clean_input($_REQUEST, "pgsql");
$code = $controller->action ($cleaned);
if($code!=200)
{
	header("HTTP/1.1 $code");
	echo "HTTP error $code";
}
pg_close($oldconn);
?>
