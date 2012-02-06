<?php
function getData($bbox,$options,$kts=null)
{
    $json=array();
    $conn=pg_connect("dbname=gis user=gis");


    $plyrs = isset($options["poi"]) ? explode(",", $options["poi"]):null;
    $wlyrs = isset($options["way"]) ? explode(",", $options["way"]):null;
    $contour = isset($options["contour"]) && $options["contour"]!=0?true:false;
    $coastline = isset($options["coastline"]) && $options["coastline"]!=0?
        true:false;

    $geomtxt=mkgeom($bbox);


    $json["features"] = array();

    if($plyrs!==null)
        $json["features"]=getPOIData($geomtxt,$plyrs,$bbox,$kts);


    if($wlyrs!==null)
        $json["features"]=array_merge($json["features"],
            getWayData($geomtxt,$wlyrs,$bbox,$kts));

    if($coastline===true)
        $json["features"]=array_merge($json["features"],
            getCoastlineData($geomtxt,$bbox,$kts));

    if($contour===true)
    {
        $x=$options['x'];
        $y=$options['y'];
        $z=$options['z'];
        if($kts!==null && $z<14)
        {
            $file="/var/www/images/contours/$kts/$z/$x/$y.json";
            if(!file_exists($file))
            {
                $contourData=getContourData($geomtxt,$bbox,$kts);
                if(!file_exists("/var/www/images/contours/$kts/$z"))
                    mkdir("/var/www/images/contours/$kts/$z",0755);

                if(!file_exists("/var/www/images/contours/$kts/$z/$x"))
                    mkdir("/var/www/images/contours/$kts/$z/$x",0755);

                file_put_contents($file,json_encode($contourData));
            }
            else
            {
                $txt=file_get_contents($file);
                $contourData=json_decode($txt);
            }
        }
        else
        {
            $contourData=getContourData($geomtxt,$bbox,$kts);
        }
        $json["features"] = array_merge($json["features"], $contourData);

    }

    

    pg_close($conn);
       return $json; 
}


function mkgeom($bbox)
{
    $g="GeomFromText('POLYGON(($bbox[0] $bbox[1],$bbox[2] $bbox[1], ".
            "$bbox[2] $bbox[3],$bbox[0] $bbox[3],$bbox[0] $bbox[1]))',900913)";
    return $g; 
}

function getPOIData($geomtxt,$plyrs,$bbox,$kts)
{
    if($kts!==null)
    {
        $factor = $kts / ($bbox[2]-$bbox[0]);
    }
    $features=array();
    $pqry = "SELECT *,ST_AsGeoJSON(way) AS geojson ".
        " FROM planet_osm_point WHERE way && $geomtxt ";

    if($plyrs[0]!="all")
        $pqry .= criteria($plyrs);

    $presult = pg_query($pqry);

    while($prow=pg_fetch_array($presult,null,PGSQL_ASSOC))
    {
        $feature=array();
        $feature["type"] = $kts===null?"Feature":"Point";
        $f= json_decode($prow["geojson"]);
        $counteddata=array();
        foreach($prow as $k=>$v)    
            if($k!='way' && $k!='geojson' && $v!='')
                $counteddata[$k]=$v;
        $feature["properties"] = $counteddata;
        if($kts===null)
        {
            $feature["geometry"]=array();
            $feature["geometry"]["coordinates"] = $f->coordinates;
            $feature["geometry"]["type"] = $f->type;
        }
        else
        {
            $feature["coordinates"]= getAdjustedCoords($f,$bbox,$kts,$factor);
        }
        $features[] = $feature;
    }    
    pg_free_result($presult);

    return $features; 
}

function getWayData($geomtxt,$wlyrs,$bbox,$kts)
{
    if($kts!==null)
    {
        $factor = $kts / ($bbox[2]-$bbox[0]);
    }
    $features=array();
    $arr=array("way"=>"planet_osm_line","polygon"=>"planet_osm_polygon");

    foreach($arr as $type=>$table)
    {
        $wqry = "SELECT * ,ST_AsGeoJSON(ST_Intersection($geomtxt,way)) ".
                "AS geojson ".
                "FROM $table WHERE way && $geomtxt  AND ".
                "ST_IsValid(way)";

        if($wlyrs[0]!="all")
            $wqry .= criteria($wlyrs);

        $wresult = pg_query($wqry);

        $first=true;

        while($wrow=pg_fetch_array($wresult,null,PGSQL_ASSOC))
        {
            $feature=array();
            $f = json_decode($wrow['geojson']);
            $tags = array();
            foreach($wrow as $k=>$v)
                if($k!='way' && $k!='geojson' && $v!='')
                    $tags[$k] = $v;
            $feature["properties"] = $tags;
            if($kts===null)
            {
                $feature["type"] = "Feature"; 
                $feature["geometry"]=array();
                $feature["geometry"]["coordinates"] = $f->coordinates;
                $feature["geometry"]["type"] = $f->type;
                if(count($feature["geometry"]["coordinates"])>0)
                    $features[] = $feature;
            }
            else
            {
                $feature["coordinates"]=
                    getAdjustedCoords($f,$bbox,$kts,$factor); 
                $feature["type"] = $f->type;
                if(count($feature["coordinates"])>0)
                    $features[] = $feature;
            }
        }
        pg_free_result($wresult);
    }
    return $features;
}

function getContourData($geomtxt,$bbox,$kts)
{
    if($kts!==null)
    {
        $factor = $kts / ($bbox[2]-$bbox[0]);
    }
    $features=array();
    $result=pg_query("SELECT ST_AsGeoJSON(ST_Intersection($geomtxt,way)) ".
                    "AS geojson,height ".
                    "FROM contours WHERE way && $geomtxt");
    while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
    {
        $feature=array();
        $f = json_decode($row['geojson']);
        $tags = array();
        $feature["properties"] = array();
        $feature["properties"]["contour"]=$row["height"];
        if($kts===null)
        {
            $feature["type"]="Feature";
            $feature["geometry"]=array();
            $feature["geometry"]["coordinates"] = $f->coordinates;
            $feature["geometry"]["type"] = $f->type;
            if(count($feature["geometry"]["coordinates"])>0)
                $features[] = $feature;
        }
        else
        {
            $feature["coordinates"] = getAdjustedCoords($f,$bbox,$kts,$factor);
            $feature["type"] = $f->type;
            if(count($feature["coordinates"])>0)
                $features[] = $feature;
        }
    }
    return $features;
}

function getCoastlineData($geomtxt,$bbox,$kts)
{
    if($kts!==null)
    {
        $factor = $kts / ($bbox[2]-$bbox[0]);
    }
    $features=array();
    $result=pg_query("SELECT ST_AsGeoJSON(ST_Intersection($geomtxt,the_geom)) ".
                    "AS geojson ".
                    "FROM coastlines WHERE the_geom && $geomtxt");
    while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
    {
        $feature=array();
        $f = json_decode($row['geojson']);
        $tags = array();
        $feature["properties"] = array();
        $feature["properties"]["natural"] = "land"; 
        if($kts===null)
        {
            $feature["type"]="Feature";
            $feature["geometry"]=array();
            $feature["geometry"]["coordinates"] = $f->coordinates;
            $feature["geometry"]["type"] = $f->type;
            if(count($feature["geometry"]["coordinates"])>0)
                $features[] = $feature;
        }
        else
        {
            $feature["coordinates"] = getAdjustedCoords($f,$bbox,$kts,$factor);
            $feature["type"] = $f->type;
            if(count($feature["coordinates"])>0)
                $features[] = $feature;
        }
    }
    return $features;
}

function criteria($lyrs)
{
    $qry="";
    if (count($lyrs) != 0)
    {
        $qry .= "AND (";
        for($i=0; $i<count($lyrs); $i++)
        {
            if($i!=0)
                $qry.=" OR ";
            if ($lyrs[$i] == "natural")
                $lyrs[$i] = "\"natural\"";
            $qry .= $lyrs[$i] . " <> '' ";
        }
        $qry .= ")";
    }
    return $qry;
}

function getAdjustedCoords($f,$bbox,$kts,$factor)
{
    $coords=array();
    switch($f->type)
    {
        case "Point":
            $x =  (int)    
                    (($f->coordinates[0] - $bbox[0]) * $factor);
            $y =     (int)
                    (($f->coordinates[1] - $bbox[1]) * $factor);
            $coords = array($x,$y);
            break;

        case "LineString":
            for($i=0; $i<count($f->coordinates); $i++)
            {
                $x =  (int)    
                    round(($f->coordinates[$i][0] - $bbox[0]) * $factor);
                $y =     (int)
                    round(($f->coordinates[$i][1] - $bbox[1]) * $factor);
                // coords of (0,0) seem to  screw up rendering
                $x=($x==0)?1:$x;
                $y=($y==0)?1:$y;
                $x=($x==$kts)?$kts-1:$x;
                $y=($y==$kts)?$kts-1:$y;
                if($x>=0 && $y>=0 && $x<=$kts && $y<=$kts)
                   $coords[] = array($x,$y);
            }
            break;

        case "MultiLineString":
        case "Polygon":
            for($i=0; $i<count($f->coordinates); $i++)
            {
                $coords[$i]=array();
                for($j=0; $j<count($f->coordinates[$i]); $j++)
                {
                    $x =  (int)    
                        round(($f->coordinates[$i][$j][0]-$bbox[0]) * $factor);
                    $y =     (int)
                        round(($f->coordinates[$i][$j][1]-$bbox[1]) * $factor);
                    // coords of (0,0) seem to screw up rendering
                    if($f->type=="MultiLineString")
                    {
                        $x=($x==0)?1:$x;
                        $y=($y==0)?1:$y;
                        $x=($x==$kts)?$kts-1:$x;
                        $y=($y==$kts)?$kts-1:$y;
                    }
                    $coords[$i][] = array($x,$y);

                }
            }
            break;

        case "MultiPolygon":
            for($i=0; $i<count($f->coordinates); $i++)
            {
                $coords[$i]=array();
                for($j=0; $j<count($f->coordinates[$i]); $j++)
                {
                    $coords[$i][$j]=array();
                    for($k=0; $k<count($f->coordinates[$i][$j]); $k++)
                    {
                        $x =  (int)    
                        round(($f->coordinates[$i][$j][$k][0]-$bbox[0]) * 
                        $factor);

                        $y =     (int)
                        round(($f->coordinates[$i][$j][$k][1]-$bbox[1]) * 
                        $factor);

                        $x=($x==0)?1:$x;
                        $y=($y==0)?1:$y;
                        $x=($x==$kts)?$kts-1:$x;
                        $y=($y==$kts)?$kts-1:$y;

                        $coords[$i][$j][] = array($x,$y);
                    }
                }
            }
            break;
            
    }
    return $coords;
}

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
    echo "<poi x='$x' y='$y'>";
    foreach($poi["properties"] as $k=>$v)
        echo "<tag k=\"$k\" v=\"$v\" />";
    echo "</poi>";
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

function reprojectData(&$data,$outProj)
{
    for($f=0; $f<count($data["features"]); $f++)
    {
        switch($data["features"][$f]["geometry"]["type"])
        {
            case "Point":
                $data["features"][$f]["geometry"]["coordinates"]=reproject
                    ($data["features"][$f]["geometry"]["coordinates"][0],
                    $data["features"][$f]["geometry"]["coordinates"][1],
                    '900913',$outProj);
                break;

            case "LineString":
                for($i=0; 
                    $i<count($data["features"][$f]["geometry"]["coordinates"]); 
                    $i++)
                {
                    $data["features"][$f]["geometry"]["coordinates"][$i]=
                    reproject
                        ($data["features"][$f]["geometry"]["coordinates"]
                        [$i][0],
                        $data["features"][$f]["geometry"]["coordinates"][$i][1],
                        '900913',$outProj);
                }
                break;

            case "Polygon":
                for($i=0; $i<count($data["features"][$f]["geometry"]
                ["coordinates"]); $i++)
                {
                    for($j=0;$j<count($data["features"][$f]
                    ["geometry"]["coordinates"][$i]);$j++)
                    {
                        $data["features"][$f]["geometry"]["coordinates"]
                        [$i][$j]=reproject
                            ($data["features"][$f]
                            ["geometry"]["coordinates"][$i][$j][0],
                            $data["features"][$f]
                            ["geometry"]["coordinates"][$i][$j][1],
                            '900913',$outProj);
                    }
                }
                break;

            case "MultiPolygon":
                for($i=0; $i<count($data["features"][$f]["geometry"]
                ["coordinates"]); $i++)
                {
                    for($j=0;$j<count($data["features"][$f]
                    ["geometry"]["coordinates"][$i]);$j++)
                    {
                        for($k=0; 
                            $k<count($data["features"][$f]
                            ["geometry"]["coordinates"][$i][$j]);
                            $k++)
                        {
                            $data["features"][$f]
                            ["geometry"]["coordinates"][$i][$j][$k]=
                                reproject
                                ($data["features"][$f]
                                ["geometry"]["coordinates"][$i][$j][$k][0],
                                $data["features"][$f]
                                ["geometry"]["coordinates"][$i][$j][$k][1],
                                '900913',$outProj);
                        }
                    }
                }
                break;
        }
    }
}

function adjustProj(&$proj)
{
    $proj=str_replace("EPSG:","",strtoupper($proj));

    $projAliases = array
        ("OSGB" => "27700",
         "GOOGLE" => "900913",
         "3785" => "900913",
         "3857" => "900913",
         "WGS84" => "4326" );

    foreach($projAliases as $alias=>$srs)
    {
        if($proj==$alias)
        {
            $proj=$srs;
            break;
        }
    }
}
?>
