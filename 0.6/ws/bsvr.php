<?php

// get annotations from the server as GeoJSON
// input: wgs84 latlon bbox

require_once('../../lib/functionsnew.php');
require_once('DataGetter.php');
require_once('DBDetails.php');
require_once('xml.php');
require_once('../../lib/latlong.php');


// fernhurst -80454 6629930

// Input:
//
// poi=[comma separated tag list]
// way=[comma separated tag list]
// bbox=[bbox, in wgs84 latlon, google spherical mercator or osgb]
// inProj = input projection (4326, 900913/3857/3785, 27700); bbox is in this
// outProj = output projection (projection of output data)
// format = geojson or xml

$conn=pg_connect("dbname=gis user=gis");
$cleaned = clean_input($_REQUEST);
$cleaned["format"] = (isset($cleaned["format"])) ? $cleaned["format"]:"xml";

$bbox = $cleaned["bbox"];
$values = explode(",",$bbox);
if(count($values)!=4) 
{
    header("HTTP/1.1 400 Bad Request");
    echo "invalid bbox";
    exit;
}

$inProj = (isset($cleaned['inProj'])) ? $cleaned['inProj']: '4326';
$outProj = (isset($cleaned['outProj'])) ? $cleaned['outProj']: '4326';

adjustProj($inProj);
adjustProj($outProj);

if($cleaned['inUnits'] && $cleaned['inUnits']=='microdeg' && $inProj=='4326')
{
	for($i=0; $i<4; $i++)
		$values[$i] /= 1000000.0;
}

// Native projection of DB is 900913 (Google Mercator)
list($sw['e'],$sw['n']) = reproject($values[0],$values[1],$inProj,'900913');
list($ne['e'],$ne['n']) = reproject($values[2],$values[3],$inProj,'900913');

$bbox = array($sw["e"],$sw["n"],$ne["e"],$ne["n"]);

$bg=new BboxGetter($bbox);
$data=$bg->getData($cleaned,null,null,$outProj=='900913'?null:$outProj);

switch($cleaned["format"])
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
