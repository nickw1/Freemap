<?php

require_once('DataGetter.php');
require_once('DBDetails.php');
require_once('../../lib/functionsnew.php');
require_once('xml.php');

$cleaned = clean_input($_GET);

$tbl_prefix = isset($cleaned["tbl_prefix"]) ? $cleaned["tbl_prefix"] : "planet_osm";

$conn=pg_connect("dbname=gis user=gis");

if (!isset($cleaned["q"]))
{
    header("HTTP/1.1 400 Bad Request");
    echo "Please specify a search term.";
}
else
{
    $format=isset($cleaned["format"])  ? $cleaned["format"]:"xml";
    $ns=new NameSearch($cleaned["q"], $tbl_prefix);
    if(isset($cleaned["outProj"]))
        adjustProj($cleaned["outProj"]);
    $data=$ns->getData($cleaned,isset($cleaned['outProj'])?
        $cleaned['outProj']:null);
    switch($cleaned["format"])
    {
        case "json":
        case "geojson":
            header("Content-type: application/json");
            echo json_encode($data);
            break;

        default:
            header("Content-type: text/xml");
            to_xml($data);
            break;
    }
}

pg_close($conn);

?>
    
