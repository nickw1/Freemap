<?php

class Walkroute
{
    private $id, $points, $waypoints, $title, $description, $valid, $distance,
            $annotations;

    public function __construct($id=null)
    {
        $this->valid = false;
        $this->points=array();
        $this->waypoints=array();
        $this->annotations=array();
        if($id!==null)
        {
            $result=pg_query("SELECT id,title,description,startlat,startlon,".
                "distance,astext(the_geom) FROM walkroutes WHERE id=" .$id);
            if(pg_numrows($result)==1)
            {
                $row=pg_fetch_array($result,null,PGSQL_ASSOC);
                $this->id=$id;
                $this->loadFromRow($row);
            }
        }
    }

    public function getTitle()
    {
        return $this->title;
    }

    public function getDescription()
    {
        return $this->description;
    }

    public function getID()
    {
        return $this->id;
    }

    public function isValid()
    {
        return $this->valid;
    }

    public function loadFromRow($row)
    {
        $this->valid=true;
        $this->title = $row["title"];
        $this->description = $row["description"];
        $this->distance = $row["distance"];
        $this->id=$row["id"];
        $m = array(); 
        preg_match("/LINESTRING\((.+)\)/",$row['astext'],$m);
        $p = explode(",", $m[1]);
        foreach($p as $pt)
        {
            $this->points[] = explode(" ", $pt);
        }

        $result2=pg_query("SELECT * FROM wr_waypoints WHERE wrid=".
                                $this->id . " ORDER BY wpid");
        while($row2=pg_fetch_array($result2,null,PGSQL_ASSOC))
        {
            $ll=sphmerc_to_ll($row2["x"],$row2["y"]);
            $this->waypoints[] = array("lon"=>$ll["lon"],
                                        "lat"=>$ll["lat"],
                                        "id"=>$row2["wpid"],
                                        "description"=>$row2["description"]); 
        }
        $this->findAnnotations();
    }

    public function findAnnotations()
    {
        $q = "SELECT astext(a.xy), a.text, ".
            "line_locate_point(r.the_geom,a.xy)".
            " AS llp FROM annotations a, walkroutes r ".
            " WHERE r.id=$this->id AND distance(r.the_geom,a.xy) < 100 ".
            "ORDER BY llp";
        $result=pg_query($q);
        while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
        {
            $m = array(); 
            preg_match("/POINT\((.+)\)/",$row['astext'],$m);
            $p = explode(" ", $m[1]);
            $ann = sphmerc_to_ll($p[0],$p[1]);
            $ann["description"] = $row["text"];
            $ann["id"] = sprintf("ANN-%03d",count($this->annotations)+1);
            $this->annotations[] = $ann; 
        }
    }

    public function toGeoJSON($doAnnotations=false)
    {
        $json=array();
        $json["type"] = "FeatureCollection";
        $json["features"] = array();
        $json["features"][0] = array();
        $json["features"][0]["type"] = "Feature";
        $json["features"][0]["geometry"] = array();
        $json["features"][0]["geometry"]["type"] = "LineString";
        $json["features"][0]["geometry"]["coordinates"] = array();
        $json["features"][0]["properties"]=array();
        $json["features"][0]["properties"]["description"] =$this->description;
        $json["features"][0]["properties"]["title"] =$this->title;
        $json["features"][0]["properties"]["distance"] =$this->distance;
        foreach($this->points as $pt)
        {
            $ll = sphmerc_to_ll($pt[0],$pt[1]);
            $json["features"][0]["geometry"]["coordinates"][] = array
                ($ll["lon"],$ll["lat"]);
        }
        
        foreach($this->waypoints as $wpt)
        {
            $f=array();
            $f["type"] = "Feature";
            $f["geometry"] = array();
            $f["geometry"]["type"] = "Point";
            $f["geometry"]["coordinates"] = array($wpt["lon"],$wpt["lat"]);
            $f["properties"]=array();
            $f["properties"]["description"] = $wpt["description"];
            $f["properties"]["featureClass"] = "stage"; 
            $f["properties"]["id"] = $wpt["id"];
            $json["features"][] = $f;
        }

        if($doAnnotations)
        {    
            foreach($this->annotations as $ann)
            {
                $f=array();
                $f["type"] = "Feature";
                $f["geometry"] = array();
                $f["geometry"]["type"] = "Point";
                $f["geometry"]["coordinates"] = array($ann["lon"],$ann["lat"]);
                $f["properties"]=array();
                $f["properties"]["description"] = $ann["description"];
                $f["properties"]["featureClass"] = "annotation"; 
                $f["properties"]["id"] = $ann["id"];
                $json["features"][] = $f;
            }
        }

        return json_encode($json);
    }

    public function toSummaryGeoJSON()
    {
        $json=array();
        $json["type"]="Feature";
        $json["geometry"]=array();
        $json["geometry"]["type"]="Point";
        $startLL = sphmerc_to_ll($this->points[0][0],$this->points[0][1]);
        $json["geometry"]["coordinates"] = array 
            ($startLL["lon"],$startLL["lat"]);
        $json["properties"]=array();
        $json["properties"]["title"]=$this->title;
        $json["properties"]["description"]=$this->description;
        $json["properties"]["id"]=$this->id;
        return $json;
    }

    public function toGPX($doAnnotations=false)
    {
        $str="<gpx><trk><name>$this->title</name><desc>$this->description".
            "</desc><number>".$this->id."</number>".
			"<extensions><distance>$this->distance</distance>".
            "</extensions><trkseg>";
        foreach($this->points as $pt)
        {
            $ll = sphmerc_to_ll($pt[0],$pt[1]);
            $str .= "<trkpt lat='$ll[lat]' lon='$ll[lon]'></trkpt>";
        }
        $str .= "</trkseg></trk>";
        foreach($this->waypoints as $wpt)
        {
            $str .= "<wpt lat='$wpt[lat]' lon='$wpt[lon]'>";
            $str .= "<name>".sprintf("%03d",$wpt['id'])."</name>";
            $str .= "<desc>$wpt[description]</desc>";
            $str .= "<type>stage</type>";
            $str .= "</wpt>";
        }

        if($doAnnotations)
        {
            foreach($this->annotations as $ann)
            {
                $str .= "<wpt lat='$ann[lat]' lon='$ann[lon]'>";
                $str .= "<name>".sprintf("%03d",$ann['id'])."</name>";
                $str .= "<desc>$ann[description]</desc>";
                $str .= "<type>annotation</type>";
                $str .= "</wpt>";
            }
        }

        $str.="</gpx>";
        return $str;
    }

    public function toSummaryGPX()
    {
        $ll = sphmerc_to_ll($this->points[0][0],$this->points[0][1]);
        return "<wpt lat='$ll[lat]' lon='$ll[lon]'><name>$this->title</name>".
                "<desc>$this->description</desc>".
				"<cmt>$this->id</cmt></wpt>";
    }

    public static function addWR($geojson,$userid)
    {
        $data = json_decode($geojson,true);
        $id=Walkroute::doAddRoute($data["features"][0],$userid);
        for($i=1; $i<count($data["features"]); $i++)
        {
            Walkroute::addRouteWaypoint($id,$data["features"][$i]);
        }
        return $id;
    }

    public static function getRoutesByBbox($w,$s,$e,$n)
    {
        return Walkroute::getRoutesFromQuery
            ("SELECT *,astext(the_geom) FROM walkroutes WHERE startlat ".
                            "BETWEEN $s AND $n AND startlon ".
                            "BETWEEN $w AND $e");
    }

	// TODO startlon and startlat really needs to be a geometry then we
	// can just use postgis distance
    public static function getRoutesByRadius($lon,$lat,$radius)
    {
        
        $routes=array();
        $result=pg_query("SELECT *,astext(the_geom) FROM walkroutes");
        while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
        {
            if(haversine_dist($row["startlon"],$row["startlat"],$lon,$lat)
                < $radius)
            {
                $wr=new Walkroute();
                $wr->loadFromRow($row);
                $routes[] = $wr; 
            }
        }
        return $routes;
    }

    public static function getRoutesByUser($userid)
    {
        return Walkroute::getRoutesFromQuery
            ("SELECT *,astext(the_geom) FROM walkroutes WHERE userid=$userid"); 
    }

    private static function getRoutesFromQuery($query)
    {
        $routes=array();
        $result=pg_query($query);
        while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
        {
            $wr=new Walkroute();
            $wr->loadFromRow($row);
            $routes[] = $wr; 
        }
        return $routes;
    }

    private static function doAddRoute(&$f,$userid)
    {
        $f["properties"] = clean_input($f["properties"]);
        for($i=0; $i<count($f["geometry"]["coordinates"]); $i++)
        {    
            $f["geometry"]["coordinates"][$i][0] = 
                pg_escape_string($f["geometry"]["coordinates"][$i][0]);
            $f["geometry"]["coordinates"][$i][1] = 
                pg_escape_string($f["geometry"]["coordinates"][$i][1]);
        }
        $q=("INSERT INTO walkroutes(title,description,distance,the_geom,".
                "startlon,startlat,userid) VALUES ('".
                $f["properties"]["title"] . "','" .
                $f["properties"]["description"] . "'," .
                $f["properties"]["distance"] . "," .
                "GeomFromText('" . 
                Walkroute::mkgeom($f["geometry"]["coordinates"]) .
                    "', 900913) , ".
                $f["geometry"]["coordinates"][0][0] . ",".
                $f["geometry"]["coordinates"][0][1] . ",$userid)" );
        pg_query($q);
        $i= pg_insert_id('walkroutes');
        return $i;
    }

    private static function addRouteWaypoint($rteid,&$f)
    {
        $f["properties"] = clean_input($f["properties"]);
        $sphmerc = ll_to_sphmerc($f["geometry"]["coordinates"][0],
                                $f["geometry"]["coordinates"][1]);
        pg_query("INSERT INTO wr_waypoints(wrid,wpid,description,x,y) ".
            "VALUES ($rteid," . $f["properties"]["id"] . ",'" .
                $f["properties"]["description"] . "',$sphmerc[e],$sphmerc[n])");
    }

    private static function mkgeom(&$coords)
    {
        $first=true;
        $txt = "LINESTRING(";
        foreach($coords as $c)
        {    
            if(! $first)
                $txt .= ",";
            else
                $first=false;
            $sphmerc = ll_to_sphmerc($c[0],$c[1]);
            $txt.="$sphmerc[e] $sphmerc[n]";
        }
        $txt .= ")";
        return $txt;
    }

    static function outputRoutes($routes,$format)
    {
        switch($format)
        {
            case "json":
            case "geojson":
                $json=array();
                $json["type"]="FeatureCollection";
                $json["features"]=array();
                foreach ($routes as $route)
                    $json["features"][]= $route->toSummaryGeoJSON();
                header("Content-type: application/json");
                echo json_encode($json);
                break;

            case "gpx":
                header("Content-type: text/xml");
                echo "<gpx>";
                foreach($routes as $route)
                    echo $route->toSummaryGPX();
                echo "</gpx>";
                break;

            case "html":
                echo "<table>";
                echo "<tr><th>Title</th><th>Description</th><th></th></tr>";
                foreach($routes as $route)
                {
                    echo "<tr>";
                    echo "<td>".$route->getTitle()."</td>";
                    echo "<td>".$route->getDescription()."</td>";
                    echo "<td>";
                    echo "<a href='wr.php?id=".$route->getID().
                        "&action=get&format=gpx'>GPX</a>";
                    echo "</td></tr>";
                }
                echo "</table>";
                break;
        }
    }
}
