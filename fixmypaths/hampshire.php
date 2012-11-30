<?php

require_once('../lib/functionsnew.php');
require_once('../0.6/ws/DataGetter.php');
require_once('../0.6/ws/DBDetails.php');

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
$dbd = new DBDetails(null, array("table"=>"hampshire",
                                "col"=>"the_geom"), null,null,null,null);
$dbd->setIntersection(false);
$bg=new BboxGetter($bbox,null,$dbd,3857);
$bg->includePolygons(false);
$bg->setCopyright("Hampshire County Council 2012, Ordnance Survey ".
            "OpenData Licence");
$data=$bg->getData(array("way"=>"all"),null,$outProj);
echo json_encode($data);

pg_close($conn);

?>
