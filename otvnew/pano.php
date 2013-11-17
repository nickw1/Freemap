<?php

require_once('Panorama.php');
require_once('../lib/functionsnew.php');

$cleaned = clean_input ($_REQUEST);
$conn=pg_connect("dbname=gis user=gis");

switch ($cleaned["action"])
{
    case "getNearest":
        if(isset($cleaned["lat"]) && isset($cleaned["lon"]))
        {
            $nearest = Panorama::getNearest($cleaned["lon"], $cleaned["lat"]);
            echo $nearest->getId();
        }
        else
            header("HTTP/1.1 400 Bad Request");
        break;

    default:
        if(isset($cleaned["id"]) && ctype_digit($cleaned["id"]))
        {
            header ("Content-type: image/jpg");
            $panorama = new Panorama ($cleaned["id"]);
            echo $panorama->getRawImageData();
        }
        else
            header("HTTP/1.1 400 Bad Request");
        break;
}

pg_close($conn);

?>
