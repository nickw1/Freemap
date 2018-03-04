<?php

function to_xml(&$data)
{
    echo "<osmdata>";
    foreach($data["features"] as $feature)
    {
        switch($feature["geometry"]["type"])
        {
            case "Point":
                poiToXML($feature);
                break;

            case "LineString":
                wayToXML($feature);
                break;

            case "Polygon":
                polygonToXML($feature);
                break;
        }
    }
    echo "</osmdata>";
}

function poiToXML(&$poi)
{
    $x = $poi["geometry"]["coordinates"][0];
    $y = $poi["geometry"]["coordinates"][1];
    if(isset($poi["properties"]["annotation"]) &&
            $poi["properties"]["annotation"] == "yes")
    {
        echo "<annotation x='$x' y='$y' id='".$poi["properties"]["id"]."'>";
        echo "<description>".htmlentities($poi["properties"]["text"]).
			"</description>";
		echo "<type>".$poi["properties"]["annotationtype"]."</type>";
        echo "</annotation>";
    }
    else
    {
        echo "<poi x='$x' y='$y'>";
        foreach($poi["properties"] as $k=>$v)
            echo "<tag k=\"$k\" v=\"$v\" />";
        echo "</poi>";
    }
}

function wayToXML(&$way)
{
    echo "<way>";
    foreach($way["geometry"]["coordinates"] as $p)
    {
        $x = $p[0];
        $y = $p[1];
        echo "<point x='$x' y='$y' />";
    }
    foreach($way["properties"] as $k=>$v)
        echo "<tag k=\"$k\" v=\"$v\" />";
    echo "</way>";
}

function polygonToXML(&$way)
{
    echo "<polygon>";
    foreach($way["geometry"]["coordinates"][0] as $p)
    {
        $x = $p[0];
        $y = $p[1];
        echo "<point x='$x' y='$y' />";
    }
    foreach($way["properties"] as $k=>$v)
        echo "<tag k=\"$k\" v=\"$v\" />";
    echo "</polygon>";
}



?>
