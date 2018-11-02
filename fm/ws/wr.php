<?php
session_start();

require_once('../../lib/functionsnew.php');
require_once('Walkroute.php');
require_once('ws_defines.php');

$format = isset($_REQUEST["format"]) && ctype_alpha($_REQUEST["format"])
         ? $_REQUEST["format"]:"geojson";
$action = isset($_REQUEST["action"]) ? $_REQUEST["action"]:"get";

$conn = new PDO ("pgsql:host=localhost;dbname=".WS_DATABASE, WS_DATABASE_USER);

$cget = clean_input($_GET, null);
$cpost = clean_input($_POST, null);
$cpost["route"] = html_entity_decode($cpost["route"]); // TODO somehting better than this

switch($action)
{
    case "add":
        if($_SERVER['REQUEST_METHOD']=='POST')
        {
            $userid = 0;
            if($userid<0)
            {
                header("HTTP/1.1 401 Unauthorized");
            }
            else if (isset($cpost["id"]) && ctype_digit($cpost["id"]))
            {
                $wr = new Walkroute($conn, $cpost["id"]);
                $wr->updateRoute($cpost["route"]);
            }
            else
            {
                $j=Walkroute::addWR($conn,$cpost["route"],$userid,
                                        $format);
                echo $j;
            }
        }
        else
        {
            header("HTTP/1.1 400 Bad Request");
            echo "Please use a POST request";
        }
        break;

    case "addWaypoint":
        $badreq=true;
        if($_SERVER['REQUEST_METHOD']=='POST')
        {
            $userid = 0; 
           if($userid<0) 
            {
                header("HTTP/1.1 401 Unauthorized");
                $badreq=false;
            }
            else if (isset($cpost["id"]) && ctype_digit($cpost["id"])
                        && isset($cpost["data"]))
            {
                $f= json_decode($cpost["data"], true);
                if(
                    preg_match("/^-?[\d\.]+$/",
                        $f["geometry"]["coordinates"][0]) 
                        &&
                    preg_match("/^-?[\d\.]+$/",
                        $f["geometry"]["coordinates"][1]) 
                    )
                {    
                    $id=Walkroute::addRouteWaypoint($conn,
                                        $cpost["id"]
                                            ,$f["geometry"]["coordinates"][0],
                                            $f["geometry"]["coordinates"][1],
                                            0, 
                                            htmlentities
                                            ($f["properties"]["description"]));
                    echo $id;
                    $badreq=false;
                }
            }
        }

        if($badreq)    
            header("HTTP/1.1 400 Bad Request");
       
        break;

    case "get":
        if($_SERVER['REQUEST_METHOD']=='GET' && isset($cget["id"]))
        {
          $id=$cget["id"];
          if(ctype_digit($id))
          {
            $wr=new Walkroute($conn, $id);
            $doAnnotations = isset($cget["ann"]) && $cget["ann"]!=0;
            if($wr->isValid())
            {
                switch($format)
                {
                    case "json":
                    case "geojson":
                        header("Content-type: application/json");
                        echo $wr->toGeoJSON($doAnnotations);
                        break;

                    case "gpx":
                        header("Content-type: text/xml");
                        header("Content-Disposition: attachment; filename=".
                                    "walkroute${id}.gpx");
                        echo $wr->toGPX($doAnnotations);
                        break;
                }
            }
            else
            {
                header("HTTP/1.1 404 Not Found");
                echo "Cannot find walkroute with that ID";
            }
          }
          else
          {
            header("HTTP/1.1 400 Bad Request");
            echo "Walkroute ID should be numeric!";
          }
        }
        else
        {
            header("HTTP/1.1 400 Bad Request");
            echo "Please supply a format (json, gpx) and route ID, and use GET";
            exit;
        }
        break;
                    
    case "getByBbox":
        if($_SERVER['REQUEST_METHOD']=='GET' && isset($cget["bbox"])
            && preg_match("/^(-?[\d\.]+,){3}-?[\d\.]+$/", $cget["bbox"]))
        {
            $b = explode(",",$cget["bbox"]);
            $routes=Walkroute::getRoutesByBbox($conn,$b[0],$b[1],$b[2],$b[3]);
            Walkroute::outputRoutes($routes,$format);
        }
        else
        {
            header("HTTP/1.1 400 Bad Request");
            echo "Please supply a valid bounding box and a format, and use GET";
        }
        break;
    
    case "getByRadius":
        if($_SERVER['REQUEST_METHOD']=='GET' &&
            isset($cget["radius"]) && 
            isset($cget['lon']) && isset($cget['lat']) &&
            preg_match("/^[\d\.]+$/", $cget["radius"]) &&
            preg_match("/^-?[\d\.]+$/", $cget["lat"]) &&
            preg_match("/^-?[\d\.]+$/", $cget["lon"]))
        {
            $routes=Walkroute::getRoutesByRadius
                ($conn, $cget['lon'],$cget['lat'],$cget["radius"]);
            Walkroute::outputRoutes($routes,$format);
        }
        else
        {
            header("HTTP/1.1 400 Bad Request");
            echo "Please supply a lon, lat, radius, and use GET";
        }
        break;

    case "getByUser":
        if($_SERVER['REQUEST_METHOD'] != 'GET')
        {
            header("HTTP/1.1 400 Bad Request");
            echo "Please use GET";
        }
        else
        {
            $userid=0;
            if(isset($cget["userid"]) && ctype_digit($cget["userid"]))
                $userid = $cget["userid"];

            if($userid>0)
            {
                $routes=Walkroute::getRoutesByUser($conn, $userid);
                Walkroute::outputRoutes($routes,$format);
            }
            else
            {
                header("HTTP/1.1 400 Bad Request");
            }
        }
        break;
    case "edit":
        if($_SERVER['REQUEST_METHOD']=='POST')
        {
            $userid = 0; 
            if($userid<=0)
            {
                header("HTTP/1.1 401 Unauthorized");
            }
            else
            {
                $wr = new Walkroute($conn,$cpost["id"]);
                $wr->updateRoute($cpost["route"],$format);
            }
        }
        break;

    case "delete":
        if($_SERVER['REQUEST_METHOD']=='POST')
        {
            if(ctype_digit($cpost["id"]))
            {
                $wr = new Walkroute($conn,$cpost["id"]);
                $userid=0;
                if(false)
                    $wr->delete();
                else
                       header("HTTP/1.1 401 Unauthorized"); 
            }
            else
                header("HTTP/1.1 400 Bad Request");
        }
        break;
    
    case "deleteMulti":
        if($_SERVER['REQUEST_METHOD']=='POST')
        {
            $userid=0;
            $deleted = array();
            if($userid<=0)
                header("HTTP/1.1 401 Unauthorized");    
            elseif(isset($cpost["ids"]))
            {
                $ids = json_decode ($cpost["ids"]);
                foreach($ids as $id)
                {
                    if(ctype_digit($id))
                    {
                        $wr = new Walkroute($conn, $id);
                        if(false)
                        {
                            $wr->delete();
                            $deleted[] = $id;
                        }
                    }        
                }
                header("Content-type: application/json");
                echo json_encode($deleted);
            }
            else
                header("HTTP/1.1 400 Bad Request");
        }
    
        break;

    case "moveWaypoint":
        if($_SERVER['REQUEST_METHOD']=='POST' &&
                ctype_digit($cpost["id"]) &&
                preg_match("/^-?[\d\.]+$/", $cpost["lon"]) &&
                preg_match("/^-?[\d\.]+$/", $cpost["lat"]))
        {
            $userid = 0; 
            if($userid>0)
            {
                $wr = Walkroute::getWalkrouteFromWaypoint($conn, $cpost["id"]);
                if($wr===null)
                    header("HTTP/1.1 404 Not Found");
                elseif(false)
                    Walkroute::moveWaypoint
                        ($conn, $cpost["id"], $cpost["lon"], $cpost["lat"]);
                else
                    header("HTTP/1.1 401 Unauthorized");
            }
            else
                header("HTTP/1.1 401 Unauthorized");
        }
        else
            header("HTTP/1.1 400 Bad Request");
    
        break;

    case "deleteMultiWaypoints":
        if($_SERVER['REQUEST_METHOD']=='POST')
        {
            $userid=0;
            $deleted = array();
            if($userid<=0)
                header("HTTP/1.1 401 Unauthorized");    
            elseif(isset($cpost["ids"]))
            {
                $ids = json_decode ($cpost["ids"]);
                foreach($ids as $id)
                {
                    if(ctype_digit("$id"))
                    {
                        $wr = Walkroute::getWalkrouteFromWaypoint($conn, $id);
                        if(false)
                        {
                            Walkroute::deleteWaypoint($conn, $id);
                            $deleted[] = $id;
                        }
                    }        
                }
                header("Content-type: application/json");
                echo json_encode($deleted);
            }
            else
                header("HTTP/1.1 400 Bad Request");
        }
    
        break;
}

?>
