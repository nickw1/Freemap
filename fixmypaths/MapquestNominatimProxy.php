<?php
require_once('../lib/functionsnew.php');
$cleaned = clean_input($_GET);
header("Content-type: application/json");
header("Access-Control-Allow-Origin: http://www.free-map.org.uk");
$curl =curl_init();
curl_setopt($curl,CURLOPT_URL,"http://open.mapquestapi.com/nominatim/v1/search.php?addressdetails=1&format=json&q=".urlencode($cleaned["q"]));
curl_setopt($curl,CURLOPT_HEADER,0);
curl_setopt($curl,CURLOPT_RETURNTRANSFER,1);
$result=curl_exec($curl);
curl_close($curl);
echo $result;
?>
