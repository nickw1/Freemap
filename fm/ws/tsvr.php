<?php

// To work with the *.php code
// NOT TESTED !!!

// Tiled data server
// Input: 
// x,y,z - standard Google tiling system values
// poi,way - comma separated tag values to select given features
// kothic - output kothic-js format geojson rather than standard geojson if !=0
// contours - output LandForm Panorama contours if !=0
// coastline - output coastline if !=0
// tbl_prefix - prefix for tables (default planet_osm)

require_once('../../lib/functionsnew.php');
require_once('../../common/defines.php');
require_once('DataGetter.php');
require_once('xml.php');
require_once('DBDetails.php');

header("Access-Control-Allow-Origin: http://www.opentrailview.org");

define('CONTOUR_CACHE2','/home/www-data/contourcache');
define('CACHE2','/home/www-data/fmapcache2');

$cleaned = clean_input($_GET, null);

// DBDetails: poi way poly contour coast ann


$x = $cleaned["x"];
$y = $cleaned["y"];
$z = $cleaned["z"];

$tbl_prefix=isset($cleaned["tbl_prefix"]) ? $cleaned["tbl_prefix"]:"planet_osm";
$ext=isset($cleaned["ext"]) ? $cleaned["ext"]:0;

$outProj = (isset($cleaned['outProj'])) ? $cleaned['outProj']: '3857';
adjustProj($outProj);

if(!ctype_digit($x) || !ctype_digit($y) || !ctype_digit($z) ||
        !ctype_alnum($outProj) || !preg_match("/^\w+$/", $tbl_prefix) ||
        isset($cleaned["poi"])&&!preg_match("/^(\w+,)*\w+$/", $cleaned["poi"])||
           isset($cleaned["way"]) && !preg_match("/^(\w+,)*\w+$/", 
            $cleaned["way"]) || 
        isset($cleaned["contour"]) && !ctype_digit($cleaned["contour"]) ||
        isset($cleaned["coastline"]) && !ctype_digit($cleaned["coastline"]))
{
    header("HTTP/1.1 400 Bad Request");
    echo "Invalid format for input data";
    exit;
}
     
$bbox = get_sphmerc_bbox($x,$y,$z);
header("Content-type: application/json");
$bg = new BboxGetter($bbox,"3857",$outProj,$ext,$tbl_prefix);
if(isset($cleaned["cache"]) && $cleaned["cache"])
{
    if(!file_exists(CONTOUR_CACHE."/$outProj/$z/$x"))
        mkdir(CONTOUR_CACHE."/$outProj/$z/$x",0755,true);
    if(!file_exists(CACHE."/$outProj/$z/$x"))
        mkdir(CACHE."/$outProj/$z/$x",0755,true);
        
    $data = false;

    if($z<=7)
    {
        $data = [];
        /* 100618 risk that OpenTrail 0.4 might try to request data at a low zoom level
        which might kill the server. Prevent it.
        */
    }
    elseif($z<=9)
    {
        $bg->addWayFilter("highway","motorway,trunk,primary,secondary,".
                            "motorway_link,primary_link,secondary_link,".
                            "trunk_link");
        $bg->addWayFilter("railway","rail,preserved");
        $bg->addWayFilter("waterway","river");
        $bg->addPOIFilter("place","city,town");
        $bg->includePolygons(false);
    }
    elseif($z<=11)
    {
        $bg->addWayFilter("highway",
                "motorway,trunk,primary,secondary,tertiary,unclassified,".
                "motorway_link,trunk_link,primary_link,secondary_link,".
                "tertiary_link,unclassified_link");
        $bg->addWayFilter("railway","rail,preserved");
        $bg->addWayFilter("waterway","river");
        $bg->addPOIFilter("place","city,town,village");
        $bg->addPOIFilter("railway","station");
    }

    if($z<=13) {
        unset($cleaned["contour"]);
    }

    if($data===false) {
        $data=$bg->getData($cleaned,CONTOUR_CACHE."/$outProj/$z/$x/$y.json",
                        CACHE."/$outProj/$z/$x/$y.json",$x,$y,$z);
//        $data["bbox"] = array($sw['lon']-0.01,$sw['lat']-0.01, $ne['lon']+0.01,$ne['lat']+0.01);
    }
}
else
{
    $data=$bg->getData($cleaned,null,null);
}
    echo json_encode($data);


?>
