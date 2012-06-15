<?php

require_once('../lib/functionsnew.php');
require_once('RightOfWay.php');

$cleaned = clean_input($_REQUEST);

$inProj =  isset($cleaned["inProj"]) ? $cleaned["inProj"]: "4326";
$xy = reproject($cleaned["x"],$cleaned["y"],$inProj,"3857");

$conn=pg_connect("dbname=gis user=gis");

switch($cleaned["action"])
{
    case "findNearest":
        header("Content-type: application/json");
        $rightofway = ROW::findClosest($xy[0],$xy[1],$cleaned["dist"]);
        if($rightofway!==null)
            $rightofway->output();
        else
        {
            header("HTTP/1.1 404 Not Found");
            echo "No ROW found";
        }
        break;

    case "addProblem":
        $rightofway=new RightOfWay($cleaned["id"]);
        if($rightofway->isValid())
        {
            $rightofway->addProblem($cleaned["problem"],$xy[0],$xy[1]);
            echo "OK";
        }
        else
        {
            header("HTTP/1.1 404 Not Found");
            echo "Invalid id";
        }
        break;

	case "getProblems":
        $rightofway=new RightOfWay($cleaned["id"]);
        if($rightofway->isValid())
        {
			header("Content-type: application/json");
            echo $rightofway->getProblems();
        }
        else
        {
            header("HTTP/1.1 404 Not Found");
            echo "Invalid id";
        }
		break;

	case "getAllProblems":
		$bbox=array();
		if(isset($cleaned["bbox"]))
		{
			$bb=explode("," , $cleaned["bbox"]);
			if(count($bb)==4)
			{
				$sw=reproject($bb[0],$bb[1],$cleaned["inProj"],"3857");
				$ne=reproject($bb[2],$bb[3],$cleaned["inProj"],"3857");
				$bbox[0] = $sw[0];
				$bbox[1] = $sw[1];
				$bbox[2] = $ne[0];
				$bbox[3] = $ne[1];
			}
		}

		$problems = RightOfWay::getAllProblems($bbox);
		header("Content-type: application/json");
		$json = array();
		$json["type"]="FeatureCollection";
		$json["features"]=array();
		foreach($problems as $prob)
		{
			$f["type"]="Feature";
			$f["geometry"]=array();
			$f["geometry"]["type"] = "Point";
			$f["geometry"]["coordinates"]=array($prob["x"],$prob["y"]);
			$f["properties"]=array();
			$f["properties"]["text"]= $prob["problem"];
			$f["properties"]["parish_row"]= $prob["parish_row"];
			$json["features"][] = $f;
		}
		echo json_encode($json);
		break;
}

pg_close($conn);

?>
