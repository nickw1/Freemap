<?php

require_once('DataGetter.php');
require_once('DBDetails.php');
require_once('../../lib/functionsnew.php');
require_once('xml.php');


$cleaned = clean_input($_GET, null);


if (!isset($cleaned["q"]) || !preg_match("/^[\w\s-']+$/", $cleaned["q"]))
{
    header("HTTP/1.1 400 Bad Request");
    echo "Please specify a valid search term.";
}
else
{
    $format=isset($cleaned["format"]) && ctype_alpha($cleaned["format"])
			  ? $cleaned["format"]:"xml";
    if(isset($cleaned["outProj"]) && ctype_alnum($cleaned["outProj"]))
        adjustProj($cleaned["outProj"]);
    $ns=new NameSearch($cleaned["q"], $cleaned["outProj"]);
    $data=$ns->getData($cleaned);
		//,isset($cleaned['outProj'])?$cleaned['outProj']:null);
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


?>
    
