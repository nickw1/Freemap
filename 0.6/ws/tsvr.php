<?php

// Tiled data server
// Input: 
// x,y,z - standard Google tiling system values
// poi,way - comma separated tag values to select given features
// kothic - output kothic-js format geojson rather than standard geojson if !=0
// contours - output LandForm Panorama contours if !=0
// coastline - output coastline if !=0
// tbl_prefix - prefix for tables (default planet_osm)

require_once('../../lib/functionsnew.php');
require_once('DataGetter.php');
require_once('xml.php');
require_once('DBDetails.php');

$cleaned = clean_input($_REQUEST);

// DBDetails: poi way poly contour coast ann

define('CONTOUR_CACHE','/var/www/images/contours');
define('CACHE','/home/www-data/fmapcache');

$x = $cleaned["x"];
$y = $cleaned["y"];
$z = $cleaned["z"];

$tbl_prefix=isset($cleaned["tbl_prefix"]) ? $cleaned["tbl_prefix"]:"planet_osm";


$outProj = (isset($cleaned['outProj'])) ? $cleaned['outProj']: '900913';
adjustProj($outProj);

$conn=pg_connect("dbname=gis user=gis");

$bbox = get_sphmerc_bbox($x,$y,$z);
if(isset($cleaned["kothic"]) && $cleaned["kothic"])
{
    $sw = sphmerc_to_ll($bbox[0],$bbox[1]);
    $ne = sphmerc_to_ll($bbox[2],$bbox[3]);
    $kg=isset($cleaned["kg"]) ? $cleaned["kg"]: 1000;
    if(!file_exists(CONTOUR_CACHE."/$kg/$z/$x"))
        mkdir(CONTOUR_CACHE."/$kg/$z/$x",0755,true);
    if(!file_exists(CACHE."/$kg/$z/$x"))
        mkdir(CACHE."/$kg/$z/$x",0755,true);
		
    $bg = new BboxGetter($bbox,$kg,$tbl_prefix);

    if($z<=7)
    {
        $bg->addWayFilter("highway","motorway,trunk,primary,".
                            "motorway_link,primary_link,trunk_link");
        $bg->addPOIFilter("place","city");
        $bg->includePolygons(false);
        unset($cleaned["contour"]);
    }
    elseif($z<=9)
    {
        $bg->addWayFilter("highway","motorway,trunk,primary,secondary,".
                            "motorway_link,primary_link,secondary_link,".
                            "trunk_link");
        $bg->addPOIFilter("place","city,town");
        $bg->includePolygons(false);
        unset($cleaned["contour"]);
    }
    elseif($z<=11)
    {
        $bg->addWayFilter("highway",
                "motorway,trunk,primary,secondary,tertiary,unclassified,".
                "motorway_link,trunk_link,primary_link,secondary_link,".
                "tertiary_link,unclassified_link");
        $bg->addPOIFilter("place","city,town,village");
        $bg->includePolygons(false);
        unset($cleaned["contour"]);
    }

    $data=$bg->getData($cleaned,CONTOUR_CACHE."/$kg/$z/$x/$y.json",
						CACHE."/$kg/$z/$x/$y.json",$outProj);
    $data["granularity"] = $kg;
    $data["bbox"] = array($sw['lon'],$sw['lat'],$ne['lon'],$ne['lat']);
    echo "onKothicDataResponse(".json_encode($data).",$z,$x,$y);";
}
else
{
    header("Content-type: application/json");
    $bg=new BboxGetter($bbox,null,$tbl_prefix);
    $data=$bg->getData($cleaned,null,null,$outProj);
    echo json_encode($data);
}

pg_close($conn);

?>
