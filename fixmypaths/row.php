<?php

require_once('../lib/functionsnew.php');
require_once('RightOfWay.php');

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
    case "findNearest":
        //header("Content-type: application/json");
        $rightofway = RightOfWay::findClosest($xy[0],$xy[1],$cleaned["dist"]);
        if($rightofway!==null)
        {
            if(isset($mimetypes[$format]))
                header("Content-type: ".$mimetypes[$format]);
            $rightofway->output($format);
        }
        else
        {
            header("HTTP/1.1 404 Not Found");
            echo "No ROW found";
        }
        break;

    case "addProblem":
        if(isset($cleaned["problem"]) && $cleaned["problem"]!="" &&
            isset($cleaned["category"]) && $cleaned["category"]!="" &&
            isset($cleaned["reporter_name"]) && $cleaned["reporter_name"]!=""
            &&isset($cleaned["reporter_email"]) && 
            filter_var($cleaned["reporter_email"],FILTER_VALIDATE_EMAIL))
        {        
            $rightofway=new RightOfWay($cleaned["id"]);
            if($rightofway->isValid())
            {
                $rightofway->addProblem($cleaned["problem"],
                                $cleaned["category"],
                                $cleaned["reporter_name"],
                                $cleaned["reporter_email"],
                                $xy[0],$xy[1]);
                echo "Successfully added";
            }
            else
            {
                header("HTTP/1.1 404 Not Found");
                echo "Invalid id";
            }
        }
        else
        {
            header("HTTP/1.1 400 Bad Request");
            echo "Required information missing or email in invalid format";
        }
        break;

    case "addMultiProblems":
            $valid=false;
            if(isset($cleaned["data"]) && $cleaned["data"]!="" &&
                isset($cleaned["reporter_name"])&&$cleaned["reporter_name"]!=""
                &&isset($cleaned["reporter_email"]) && 
                filter_var($cleaned["reporter_email"],FILTER_VALIDATE_EMAIL))
            {
                $data = @simplexml_load_string(stripslashes($_POST["data"]));
                if($data)
                {
                    foreach($data->annotation as $annotation)
                    {
                        $attrs = $annotation->attributes();
						if(isset($attrs["x"]) && isset($attrs["y"]) &&
								isset($annotation->description) &&
								isset($annotation->extras) &&
								isset($annotation->extras->row_id))
						{
                        	$desc=pg_escape_string($annotation->description);
							$type=isset($annotation->type) ?
								pg_escape_string($annotation->type):
								"unknown";
							$id=pg_escape_string($annotation->extras->row_id);
                        	$rightofway=new RightOfWay($id);
                        	if($rightofway->isValid())
                        	{
								// Note attributes need to be cast to float
								// otherwise this will not work
								$xy1 = reproject((float)$attrs["x"],
										(float)$attrs["y"],
									$inProj,"3857");
                            	$rightofway->addProblem($desc, $type,
                                	$cleaned["reporter_name"],
                                	$cleaned["reporter_email"],
									$xy1[0],$xy1[1]);
							}
                        }
                    }
                }
            }
            else
            {
                header("HTTP/1.1 400 Bad Request");
                echo "Unexpected format";
            }
        break;

    case "getProblems":
        if(isset($cleaned["id"]))
            $rightsofway=array(new RightOfWay($cleaned["id"]));
        elseif(isset($cleaned["parish"]) && isset($cleaned["routeno"]))
            $rightsofway= RightOfWay::findFromRouteNo
                ($cleaned["parish"],$cleaned["routeno"]);


        if(count($rightsofway)>0)
        {
            header("Content-type: application/json");
            $json=array();
            foreach($rightsofway as $rightofway)
            {
                if($rightofway->isValid())
                {
                    $data = $rightofway->getProblems();
                    if(count($data)>0)
                        $json[] = $data;
                }
            }
            echo json_encode($json);
        }
        else
        {
            header("HTTP/1.1 404 Not Found");
            echo "Invalid id or parish/routeno";
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
        if($format=="xml")
        {
            header("Content-type: text/xml");
            $xml="";
            $xml .= "<freemap><projection>$outProj</projection>";
            foreach($problems as $prob)
            {
                $coords=reproject($prob["x"],$prob["y"],"3857",$outProj);
                $xml .= "<annotation x='$coords[0]' y='$coords[1]' ".
                        "id='$prob[id]'>".
                        "<description>$prob[problem]</description>".
                        "</annotation>";
            }
            $xml .= "</freemap>";
            echo $xml;
        }
        else
        {
            $json = array();
            $json["type"]="FeatureCollection";
            $json["features"]=array();
            foreach($problems as $prob)
            {
                $f["type"]="Feature";
                $f["geometry"]=array();
                   $f["geometry"]["type"] = "Point";
                $f["geometry"]["coordinates"] = reproject
                    ($prob["x"],$prob["y"],"3857",$outProj);
                $f["properties"]=array();
                    $f["properties"]["text"]= $prob["problem"];
                $f["properties"]["gid"] = $prob["id"];
                $f["properties"]["parish_row"]= $prob["parish_row"];
                $json["features"][] = $f;
            }
            header("Content-type: application/json");
            echo json_encode($json);
        }
        break;
}

pg_close($conn);

?>
