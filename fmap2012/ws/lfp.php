<?php

// box res points

// this fully deals in metres. it's up to clients to work with km if needed

require_once('../../lib/functionsnew.php');
require_once('../../qs/phpcoord-2.3.php');

$cleaned = clean_input($_REQUEST);
if(!isset($cleaned["e"]) || !isset($cleaned["n"]) ||
	$cleaned["e"]%5000 || $cleaned["n"]%5000)
{
	header("HTTP/1.1 400 Bad Request");
	echo "Invalid coords, must be divisible by 5000";
	exit;
}

$e=$cleaned['e'];
$n=$cleaned['n'];
$os = new OSRef($e,$n);
$str = $os->toSixFigureString();
$gs = strtolower(substr($str,0,2));
$file = "/var/www/downloads/lfp/$gs/$gs".$str[2].$str[5].
	(($n%10000==5000) ? "n":"s") .
	(($e%10000==5000) ? "e":"w") . ".hgtl";

if(!file_exists($file))
{
	header("HTTP/1.1 404 Not Found");
	echo "Cannot find matching height file ($file)";
}
else
{
	header("Content-type: application/json");
	$fp=fopen($file,"r");
	$json["heights"]=array();
	for($h=0; $h<10201; $h++)
	{
		$ha=fread($fp,2);
		$json["heights"][]=ord($ha[0]) + ord($ha[1])*256;
	}
	fclose($fp);
	$json["res"] = 50; // metres
	$json["box"] = array((int)$e,(int)$n,$e+5000,$n+5000);
	echo json_encode($json);
}

?>
