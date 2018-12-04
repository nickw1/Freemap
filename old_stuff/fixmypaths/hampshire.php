<?php

require_once('../lib/functionsnew.php');
require_once('../0.6/ws/DataGetter.php');
require_once('../0.6/ws/DBDetails.php');
require_once('copyrights.php');

$cleaned = clean_input($_REQUEST);


$bbox_in = explode(",", $cleaned["bbox"]);
$inProj =  $cleaned["inProj"]; 
$sw = reproject($bbox_in[0],$bbox_in[1],$inProj,"3857");
$ne = reproject($bbox_in[2],$bbox_in[3],$inProj,"3857");
$bbox = array($sw[0],$sw[1],$ne[0],$ne[1]);
$outProj =  $cleaned["outProj"]; 
adjustProj($outProj);

$conn=pg_connect("dbname=gis user=gis");

header("Content-type: application/json");
header("Access-Control-Allow-Origin: http://www.free-map.org.uk");
$dbd = new DBDetails(null, array("table"=>"hampshire",
                                "col"=>"the_geom"), null,null,null,null);
$dbd->setIntersection(false);
$bg=new BboxGetter($bbox,null,$dbd,3857);
$bg->includePolygons(false);
$data=$bg->getData(array("way"=>"all"),null,null,$outProj);
$counties = $bg->getUniqueList("county");
$arr = array();
foreach ($counties as $county)
	$arr[$county] = getCopyright($county);
$bg->setCopyright($arr);
$data=$bg->simpleGetData();

echo json_encode($data);

pg_close($conn);

?>
