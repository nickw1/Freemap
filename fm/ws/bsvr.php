<?php

// get annotations from the server as GeoJSON
// input: wgs84 latlon bbox

require_once('../../lib/functionsnew.php');
require_once('DataGetter.php');
require_once('DBDetails.php');
require_once('xml.php');
require_once('../../lib/latlong.php');

header("Access-Control-Allow-Origin: http://www.opentrailview.org");

// fernhurst -80454 6629930

// Input:
//
// poi=[comma separated tag list]
// way=[comma separated tag list]
// bbox=[bbox, in wgs84 latlon, google spherical mercator or osgb]
// inProj = input projection (4326, 900913/3857/3785, 27700); bbox is in this
// outProj = output projection (projection of output data)
// format = geojson or xml

$inProj = (isset($_GET['inProj'])) ? $_GET['inProj']: '4326';
$outProj = (isset($_GET['outProj'])) ? $_GET['outProj']: '4326';
$format = (isset($_GET["format"])) ? $_GET["format"]:"xml";

if(isset($_GET["poi"]) && !preg_match("/^(\w+,)*\w+$/", $_GET["poi"]) ||
   isset($_GET["way"]) && !preg_match("/^(\w+,)*\w+$/", $_GET["way"]) || 
   !preg_match("/^(-?[\d\.]+,){3}-?[\d\.]+$/", $_GET["bbox"]) ||
   !ctype_alnum($inProj) || !ctype_alnum($outProj) || !ctype_alpha($format))
{
	header("HTTP/1.1 400 Bad Request");
	echo "input data in invalid format";
	exit;
}

$bbox = $_GET["bbox"];
$values = explode(",",$bbox);
if(count($values)!=4) 
{
    header("HTTP/1.1 400 Bad Request");
    echo "invalid bbox";
    exit;
}


adjustProj($inProj);
adjustProj($outProj);

if(isset($_GET['inUnits'])
	 && $_GET['inUnits']=='microdeg' && $inProj=='4326')
{
	for($i=0; $i<4; $i++)
		$values[$i] /= 1000000.0;
}

// Native projection of DB is 900913 (Google Mercator)

// 041114 ensure reprojected bbox completely contains original bbox by
// taking the min and max eastings and northings of each corner
list($sw['e'],$sw['n']) = reproject($values[0],$values[1],$inProj,'900913');
list($nw['e'],$nw['n']) = reproject($values[0],$values[3],$inProj,'900913');
list($ne['e'],$ne['n']) = reproject($values[2],$values[3],$inProj,'900913');
list($se['e'],$se['n']) = reproject($values[2],$values[1],$inProj,'900913');


$bbox = array(min($sw["e"],$nw["e"]),min($sw["n"],$se["n"]),
				max($ne["e"],$se["e"]), max($ne["n"],$nw["n"]));


$bg=new BboxGetter($bbox);
$data=$bg->getData($_GET,null,null,$outProj=='900913'?null:$outProj);

switch($format)
{
    case "geojson":
    case "json":
        header("Content-type: application/json");
        echo json_encode($data);
        break;

    default:
        header("Content-type: text/xml");
		echo '<?xml version="1.0"?>';
        to_xml($data);
        break;
}


?>
