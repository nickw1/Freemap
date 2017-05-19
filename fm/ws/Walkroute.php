<?php

require_once('../../lib/gpx.php');

class Walkroute
{
    private $id, $points, $waypoints, $title, $description, $valid, $distance,
            $annotations;

    public function __construct($conn, $id=null)
    {
        $this->valid = false;
        $this->points=array();
        $this->waypoints=array();
        $this->annotations=array();
        $this->conn = $conn;
        if($id!==null && ctype_digit("$id"))
        {
            $stmt = $this->conn->prepare
            ("SELECT id,title,description,startlat,startlon,userid,".
                "distance,ST_AsText(the_geom) FROM walkroutes WHERE id=?");
            $stmt->bindParam (1, $id);
            $stmt->execute();
            if($stmt->rowCount()==1)
            {
                $row=$stmt->fetch(PDO::FETCH_ASSOC);
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

    public function getUserId()
    {
        return $this->userid;
    }

    public function loadFromRow($row)
    {
        $this->valid=true;
        $this->title = $row["title"];
        $this->description = $row["description"];
        $this->distance = $row["distance"];
        $this->id=$row["id"];
        $this->userid=$row["userid"];
        $m = array(); 
        preg_match("/LINESTRING\((.+)\)/",$row['st_astext'],$m);
        $p = explode(",", $m[1]);
        foreach($p as $pt)
        {
            $this->points[] = explode(" ", $pt);
        }

        $stmt=$this->conn->prepare("SELECT ST_AsText(w.xy),w.description,w.id,".
                                "ST_Line_Locate_Point(r.the_geom, w.xy) AS llp".
                                " FROM wr_waypoints w, walkroutes r ".
                                " WHERE r.id=? AND r.id=w.wrid ORDER BY llp");
        $stmt->bindParam (1, $this->id);
        $stmt->execute();
        $stage=1;
        while($row2=$stmt->fetch(PDO::FETCH_ASSOC))
        {
            $m=array();
            preg_match("/POINT\((.+)\)/",$row2['st_astext'],$m);
            list($x,$y) = explode(" ", $m[1]);
            $ll=sphmerc_to_ll($x,$y);
            $this->waypoints[] = array("lon"=>$ll["lon"],
                                        "lat"=>$ll["lat"],
                                        "stage"=>$stage++,
                                        "id"=>$row2["id"],
                                        "description"=>$row2["description"]); 
        }
        $this->findAnnotations();
    }

    public function findAnnotations()
    {
        $stmt = $this->conn->prepare
            ( "SELECT ST_AsText(a.xy), a.text, ".
            "ST_Line_Locate_Point(r.the_geom,a.xy)".
            " AS llp FROM annotations a, walkroutes r ".
            " WHERE r.id=? AND st_distance(r.the_geom,a.xy) < 100 ".
            "ORDER BY llp");
        $stmt->bindParam (1, $this->id);
        $stmt->execute();
        while($row=$stmt->fetch(PDO::FETCH_ASSOC))
        {
            $m = array(); 
            preg_match("/POINT\((.+)\)/",$row['st_astext'],$m);
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
        $json["features"][0]["properties"]["id"] =$this->id;
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
            $f["properties"]["stage"] = $wpt["stage"];
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
        $json["properties"]["distance"] = $this->distance;
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
                "<extensions><distance>$this->distance</distance></extensions>".
                "<cmt>$this->id</cmt></wpt>";
    }

    // To minimise faffing for the moment, only GeoJSON is accepted as
    // update format.
    public function updateRoute($txtdata)
    {
      $data = json_decode($txtdata,true);
        $f = $data["features"][0];
        $sql = "UPDATE walkroutes SET ";
        $first=true;
        $values = array    
            ("title" => $f["properties"]["title"],
             "description" => $f["properties"]["description"],
             "distance" => $f["properties"]["distance"],
             "startlon" => $f["geometry"]["coordinates"][0][0],
             "startlat" => $f["geometry"]["coordinates"][0][1]);
        foreach($values as $k=>$v)
        {
            if($v!==null)
            {
                if($first==false)
                    $sql.=",";
                else
                    $first=false;
                $sql .="$k=?";
            }
        }

        if(isset($f["geometry"]["coordinates"]))
        {
            if($first==false)
                $sql.=",";
            $sql.= "the_geom=ST_GeomFromText(?,3857) ";
        }
    
        $sql.=" WHERE id=?";    
        $stmt=$this->conn->prepare($sql);
          $p=1; 
          if (isset($f["properties"]["title"]))
              $stmt->bindParam($p++,$f["properties"]["title"]);
          if (isset($f["properties"]["description"]))
              $stmt->bindParam($p++,$f["properties"]["description"]);
          if (isset($f["properties"]["distance"]))
              $stmt->bindParam($p++,$f["properties"]["distance"]);
          if (isset($f["properties"]["description"]))
              $stmt->bindParam($p++,$f["properties"]["description"]);
        if (isset($f["geometry"]["coordinates"][0][0]))
        {
              $stmt->bindParam($p++,$f["geometry"]["coordinates"][0][0]);
        }
        if (isset($f["geometry"]["coordinates"][0][1]))
        {
              $stmt->bindParam($p++,$f["geometry"]["coordinates"][0][1]);
        }
        if (isset($f["geometry"]["coordinates"]))
        {
              $stmt->bindParam($p++,Walkroute::mkgeom
                            ($f["geometry"]["coordinates"]));
        }
        $stmt->bindParam($p++, $this->id);

        $stmt->execute();
    }

    public function delete()
    {
        $stmt = $this->conn->prepare
            ("DELETE FROM walkroutes WHERE id=?");
        $stmt->bindParam (1, $this->id);
        $stmt->execute();
        $stmt = $this->conn->prepare
            ("DELETE FROM wr_waypoints WHERE wrid=?");
        $stmt->bindParam (1, $this->id);
        $stmt->execute();
            
    }

    public static function addWR($conn,$txtdata,$userid,$format="geojson")
    {
        $data = $format=="gpx" ? parseGPX($txtdata):json_decode($txtdata,true);
        $d = $format=="gpx" ? $data:$data["features"][0];
        $id=Walkroute::doAddRoute ($conn,$d,$userid,$format);
        switch($format)
        {
            case "geojson":
                for($i=1; $i<count($data["features"]); $i++)
                {
                    $f=$data["features"][$i];
                    if(preg_match("/^-?[\d\.]+$/",
                            $f["geometry"]["coordinates"][0]) &&
                        preg_match("/^-?[\d\.]+$/",
                            $f["geometry"]["coordinates"][1]) &&
                        ctype_digit ($f["properties"]["id"]))

                    {
                        Walkroute::addRouteWaypoint($conn,
                                        $id,$f["geometry"]["coordinates"][0],
                                            $f["geometry"]["coordinates"][1],
                                            $f["properties"]["id"],
                                            htmlentities
                                            ($f["properties"]["description"]));
                    }
                }
                break;

            case "gpx":
                for($i=0; $i<count($data["wp"]); $i++)
                {
                    $f=$data["wp"][$i];
                    if(preg_match("/^-?[\d\.]+$/", $f["lat"]) &&
                        preg_match("/^-?[\d\.]+$/", $f["lon"]))
                    {
                        Walkroute::addRouteWaypoint($id,$f["lon"],$f["lat"],
                                            $i+1, htmlentities($f["desc"]));
                    }
                }
                break;
        }
        return $id;
    }

    public static function getRoutesByBbox($conn,$w,$s,$e,$n)
    {
        $stmt = $conn->prepare
            ("SELECT *,ST_AsText(the_geom) FROM walkroutes WHERE ".
                            "authorised=1 AND ". 
                             "startlat ".
                            "BETWEEN ? AND ? AND startlon ".
                            "BETWEEN ? AND ?");
        $stmt->bindParam (1, $s);
        $stmt->bindParam (2, $n);
        $stmt->bindParam (3, $w);
        $stmt->bindParam (4, $e);
        return Walkroute::getRoutesFromQuery ($conn, $stmt);
    }

    // TODO startlon and startlat really needs to be a geometry then we
    // can just use postgis distance
    public static function getRoutesByRadius($conn,$lon,$lat,$radius)
    {
        
        $routes=array();
        $result=$conn->query("SELECT *,ST_AsText(the_geom) FROM walkroutes ".
                            "WHERE authorised=1");
        while($row=$result->fetch(PDO::FETCH_ASSOC))
        {
            if(haversine_dist($row["startlon"],$row["startlat"],$lon,$lat)
                < $radius)
            {
                $wr=new Walkroute($conn);
                $wr->loadFromRow($row);
                $routes[] = $wr; 
            }
        }
        return $routes;
    }

    public static function getRoutesByUser($conn, $userid)
    {
        $stmt=$conn->prepare
            ("SELECT *,ST_AsText(the_geom) FROM walkroutes WHERE userid=?"); 
        $stmt->bindParam (1, $userid);
        return Walkroute::getRoutesFromQuery($conn, $stmt);
    }

    private static function getRoutesFromQuery($conn, $stmt)
    {
        $routes=array();
        $stmt->execute();
        while($row=$stmt->fetch(PDO::FETCH_ASSOC))
        {
            $wr=new Walkroute($conn);
            $wr->loadFromRow($row);
            $routes[] = $wr; 
        }
        return $routes;
    }

    private static function doAddRoute($conn,&$f,$userid,$format="geojson")
    {
      $i=0;
      $stmt=$conn->prepare
            ("INSERT INTO walkroutes(title,description,distance,the_geom,".
                "startlon,startlat,userid,authorised) VALUES ".
                "(?,?,?,ST_GeomFromText(?,3857),?,?,?,?)");
      switch($format)
      {
       case "geojson":
        $stmt->bindParam (1, $f["properties"]["title"]);
        $stmt->bindParam (2, $f["properties"]["description"]);
        $stmt->bindParam (3, $f["properties"]["distance"]);
        // 19/11/16 Strict Standards fix - only variables passed by reference
        $coords = Walkroute::mkgeom($f["geometry"]["coordinates"]);
        $stmt->bindParam (4, $coords);
        $stmt->bindParam (5, $f["geometry"]["coordinates"][0][0]);
        $stmt->bindParam (6, $f["geometry"]["coordinates"][0][1]);
        $stmt->bindParam (7, $userid);
        $status = $userid>0 ? 1:0;
        $stmt->bindParam (8, $status);
        break;
       case "gpx":
        $stmt->bindParam (1, $f["name"]);
        $stmt->bindParam (2, $f["desc"]);
        $stmt->bindParam (3, $f["distance"]);
        // 19/11/16 Strict Standards fix - only variables passed by reference
        // 19/05/17 somehow the 2nd and 3rd arguments were removed here for
        // GPX - don't ask me how - anyway they are now back again
        $trk = Walkroute::mkgeom($f["trk"], "lon", "lat");
        $stmt->bindParam (4, $trk);
        $stmt->bindParam (5, $f["trk"][0]["lon"]);
        $stmt->bindParam (6, $f["trk"][0]["lat"]);
        $stmt->bindParam (7, $userid);
        $status = $userid>0 ? 1:0;
        $stmt->bindParam (8, $status);
        break;
      }
      $stmt->execute();
      $result=$conn->query
                ("SELECT currval('walkroutes_id_seq') AS lastid");
      $row=$result->fetch(PDO::FETCH_ASSOC);
      return $row['lastid'];
    }

    public static function addRouteWaypoint
            ($conn,$rteid,$lon,$lat,$wpid,$wpdesc)
    {
        $sphmerc = ll_to_sphmerc($lon,$lat);

        // At some point the x,y columns will go and we'll just have xy
        $stmt=$conn->prepare
            ("INSERT INTO wr_waypoints(wrid,wpid,description,x,y,xy) ".
            "VALUES (?,?,?,?,?,ST_GeomFromText(?,3857))");
        $geom = "POINT($sphmerc[e] $sphmerc[n])";
        $stmt->bindParam (1, $rteid);
        $stmt->bindParam (2, $wpid);
        $wpdesc = str_replace("'","",$wpdesc);
        $stmt->bindParam (3, $wpdesc);
        $stmt->bindParam (4, $sphmerc["e"]);
        $stmt->bindParam (5, $sphmerc["n"]);
        $stmt->bindParam (6, $geom);
        $stmt->execute();
          $result=$conn->query
                ("SELECT currval('wr_waypoints_id_seq') AS lastid");
          $row=$result->fetch(PDO::FETCH_ASSOC);
          return $row['lastid'];
    }

    private static function mkgeom(&$coords,$lonidx=0,$latidx=1)
    {
        $first=true;
        $txt = "LINESTRING(";
        foreach($coords as $c)
        {    
            print_r($c);
            echo "\n";
            if(! $first)
                $txt .= ",";
            else
                $first=false;
            echo "Info :" . $c[$lonidx]. " ".$c[$latidx]."\nSphmerc:";
            $sphmerc = ll_to_sphmerc($c[$lonidx],$c[$latidx]);
            print_r($sphmerc);
            echo "\n";
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

    static function moveWaypoint ($conn, $wpid, $lon, $lat)
    {
        $sphmerc = ll_to_sphmerc($lon,$lat);

        // At some point the x,y columns will go and we'll just have xy
        $stmt=$conn->prepare ("UPDATE wr_waypoints SET xy=".
                    "ST_GeomFromText(?,3857) WHERE id=?");
        $geom = "POINT($sphmerc[e] $sphmerc[n])";
        $stmt->bindParam (1, $geom);
        $stmt->bindParam (2, $wpid);
        $stmt->execute();
    }

    static function getWalkrouteFromWaypoint($conn, $wpid)
    {
        $stmt = $conn->prepare
            ("SELECT wrid FROM wr_waypoints WHERE id=?");
        $stmt->bindParam (1, $wpid);
        $stmt->execute();
        if($row = $stmt->fetch())
        {
            return new Walkroute($conn, $row["wrid"]);
        }
        return null;
    }

    static function deleteWaypoint ($conn, $wpid)
    {
        $stmt=$conn->prepare ("DELETE FROM wr_waypoints WHERE id=?"); 
        $stmt->bindParam (1, $wpid);
        $stmt->execute();
    }
}
