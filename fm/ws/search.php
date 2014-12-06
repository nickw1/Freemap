<?php

require_once('DataGetter.php');
require_once('DBDetails.php');
require_once('../../lib/functionsnew.php');
require_once('xml.php');

header("Access-Control-Allow-Origin: http://www.fixmypaths.org");


$tbl_prefix = isset($_GET["tbl_prefix"]) &&
		preg_match("/^\w+$/", $_GET["tbl_prefix"])
		? $_GET["tbl_prefix"] : "planet_osm";



if (!isset($_GET["q"]) || !preg_match("/^[\w\s-']+$/", $_GET["q"]))
{
    header("HTTP/1.1 400 Bad Request");
    echo "Please specify a valid search term.";
}
else
{
    $format=isset($_GET["format"]) && ctype_alpha($_GET["format"])
			  ? $_GET["format"]:"xml";
    $ns=new NameSearch($_GET["q"], $tbl_prefix);
    if(isset($_GET["outProj"]) && ctype_alnum($_GET["outProj"]))
        adjustProj($_GET["outProj"]);
    $data=$ns->getData($_GET,isset($_GET['outProj'])?
        $_GET['outProj']:null);
    switch($_GET["format"])
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


?>
    
