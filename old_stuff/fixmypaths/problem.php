<?php

require_once('../lib/functionsnew.php');
require_once('Problem.php');

$cleaned = clean_input($_REQUEST);

$inProj =  isset($cleaned["inProj"]) ? $cleaned["inProj"]: "4326";
$outProj =  isset($cleaned["outProj"]) ? $cleaned["outProj"]: "3857";
if(isset($cleaned["x"]) && isset($cleaned["y"]))
    $xy = reproject($cleaned["x"],$cleaned["y"],$inProj,"3857");
$format = isset($cleaned["format"]) ? $cleaned["format"] : "geojson";
$mimetypes = array ("geojson"=>"application/json",
                    "xml"=>"text/xml");
$conn=pg_connect("dbname=gis user=gis");

switch($cleaned["action"])
{
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

        $problems = Problem::getAllProblems($bbox);
        prob_output($problems, $format, $outProj);
        break;

    case "getProblemsByAdmUnit":
        $admunits = array ("county","district","parish");
        $data = array();
        foreach($admunits as $admunit)
            if(isset($cleaned[$admunit]))
                $data[$admunit] = $cleaned[$admunit];
        $problems = Problem::getProblemsByAdmUnit($data);
        prob_output($problems, $format, $outProj);
        break;

    case "getProblemsByTime":
        $problems = Problem::getProblemsByTime($cleaned["days"]);
        prob_output($problems, $format, $outProj);
        break;

    case "fix":
        $prob = new Problem($cleaned["id"]);
        $prob->fix();
        break;

    case "remove":
        $prob = new Problem($cleaned["id"]);
        $prob->remove();
        break;

    case "addToLog":
        $prob = new Problem($cleaned["id"]);
        $prob->addToLog($cleaned["msg"]);
        break;

    case "getLog":
        $prob = new Problem($cleaned["id"]);
        $log = $prob->getLog();
        header("Content-type: application/json");
        echo json_encode($log);
        break;

    case "getLocation":
        header("Content-type: application/json");
        $prob = new Problem($cleaned["id"]);
        echo json_encode($prob->getLocation());
        break;
}

pg_close($conn);

function prob_output($problems, $format, $outProj)
{
        if($format=="xml")
        {
            header("Content-type: text/xml");
            $xml="";
            $xml .= "<freemap><projection>$outProj</projection>";
            foreach($problems as $prob)
                $xml .= $prob->output("xml",$outProj);
            $xml .= "</freemap>";
            echo $xml;
        }
        else
        {
            $json = array();
            $json["type"]="FeatureCollection";
            $json["features"]=array();
            foreach($problems as $prob)
                $json["features"][] = $prob->output("json",$outProj);
            header("Content-type: application/json");
            echo json_encode($json);
        }
}
?>
