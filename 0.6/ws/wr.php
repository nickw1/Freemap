<?php
session_start();

require_once('../../lib/functionsnew.php');
require_once('Walkroute.php');


$pconn=pg_connect("dbname=gis user=gis");
$cleaned=clean_input($_REQUEST,'pgsql');


switch($cleaned["action"])
{
	case "add":
		if($_SERVER['REQUEST_METHOD']=='POST')
		{
			if(isset($_SESSION["gatekeeper"]))
			{
				$userid=get_user_id ($_SESSION['gatekeeper'],
								'users','username','id','pgsql');
				$j=Walkroute::addWR(stripslashes($_POST["route"]),$userid);
				echo $j;
			}
			else
			{
				header("HTTP/1.1 401 Unauthorized");
			}
		}
		else
		{
			header("HTTP/1.1 400 Bad Request");
			echo "Please use a POST request";
		}
		break;

	case "get":
		if($_SERVER['REQUEST_METHOD']=='GET' &&
			isset($cleaned["format"]) && isset($cleaned["id"]))
		{
			$wr=new Walkroute($cleaned["id"]);
			$doAnnotations = (isset($cleaned["ann"]) && $cleaned["ann"] ) ?
				true:false;
			if($wr->isValid())
			{
				switch($cleaned["format"])
				{
					case "json":
					case "geojson":
						header("Content-type: application/json");
						echo $wr->toGeoJSON($doAnnotations);
						break;

					case "gpx":
						header("Content-type: text/xml");
						header("Content-Disposition: attachment; filename=".
									"walkroute${cleaned[id]}.gpx");
						echo $wr->toGPX($doAnnotations);
						break;
				}
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
		if($_SERVER['REQUEST_METHOD']=='GET'
			&& isset($cleaned["bbox"]) && isset($cleaned["format"]))
		{
			$b = explode(",",$cleaned["bbox"]);
			if(is_array($b) && count($b)==4)
			{
				$routes=Walkroute::getRoutesByBbox($b[0],$b[1],$b[2],$b[3]);
				Walkroute::outputRoutes($routes,$cleaned["format"]);
			}
		}
		else
		{
			header("HTTP/1.1 400 Bad Request");
			echo "Please supply a bounding box and a format, and use GET";
		}
		break;

	case "getByUser":
		if($_SERVER['REQUEST_METHOD'] != 'GET')
		{
			header("HTTP/1.1 400 Bad Request");
			echo "Please use GET";
		}
		elseif(isset($_SESSION["gatekeeper"]))
		{
			$userid=get_user_id ($_SESSION['gatekeeper'],
								'users','username','id','pgsql');
			$routes=Walkroute::getRoutesByUser($userid);
			Walkroute::outputRoutes($routes,$cleaned["format"]);
		}
		else
			header("HTTP/1.1 401 Unauthorized");
		break;
}

pg_close($pconn);
?>
