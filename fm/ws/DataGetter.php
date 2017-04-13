<?php

//define('MAX_TILE_AGE', 2592000);
define('MAX_TILE_AGE', 86400);
define('TILELIST', '/home/www-data/cron/tilelist.txt');

// change 180815
// add $ext parameter to extend bbox to avoid boundary artefacts
// was originally done if $kg existed, however now we also want to do this
// when calling the web service from the mapsforge client (i.e. a non-kothic
// scenario)
// also turn back on loading from cache if it exists. that's why the site
// has been slow lately! 

class DataGetter
{
    protected $data, $kothic_gran, $wayMustMatch, $poiFilter, $wayFilter,
            $doWays, $doPolygons, $dbq, $conn;

    function __construct($kothic_gran=null, $dbdetails=null, $srid="3857",
						$projSRID="3857",$outSRID="3857",	
                            $dbname="gis2", $user="gis")
    {
        $this->conn = new PDO("pgsql:host=localhost;dbname=$dbname", $user);
        $this->data = array();
        $this->data["features"] = array();
        $this->data["type"] = "FeatureCollection";
        $this->data["properties"]["copyright"] =
            "Map data OpenStreetMap Contributors, Open Database Licence; ".
            "contours Ordnance Survey, OS OpenData licence";
        $this->kothic_gran = $kothic_gran;
        $this->poiFilter=array();
        $this->wayFilter=array();
        $this->doWays=true;
        $this->doPolygons=true;
        $prefix = is_string($dbdetails) ? $dbdetails: "planet_osm";
        $this->dbq = is_object($dbdetails)&&get_class($dbdetails)=="DBDetails"? 
                $dbdetails:
                new DBDetails(
                    array("table"=>"${prefix}_point",
                            "col"=>"way"),
                    array("table"=>"${prefix}_line",
                            "col"=>"way"),
                    array("table"=>"${prefix}_polygon",
                            "col"=>"way"),
                    array("table"=>"contours",
                            "col"=>"way"),
                    array("table"=>"coastlines",
                            "col"=>"the_geom"),
                    array("col"=>"xy"), 
					$srid, $projSRID, $outSRID
                            ); 
        $this->SRID = $srid;
        $this->projSRID = $projSRID;

        // 240913 if not in kothic mode we want the full ways, not just
        // the intersection with the bbox
        
        //$this->dbq->setIntersection($this->kothic_gran !==null);
        //$this->dbq->setIntersection(is_numeric($this->kothic_gran));
    // 280614 make it always intersect to see if it fixes hikar issues
        // 301014 this ensures all ways are retrieved, but screws up when
        // applying DEMs
    }

    function setCopyright($copyright)
    {
        $this->data["properties"]["copyright"] =  $copyright;
    }

    function getData($options)//,$outProj=null)
    {
        $this->doGetData($options);
		/*
        if($outProj!==null)
            $this->reprojectData($outProj);
		*/
        return $this->data;
    }

    protected function doGetData($options)
    {

        $plyrs = isset($options["poi"]) ? explode(",", $options["poi"]):null;
        $wlyrs = isset($options["way"]) ? explode(",", $options["way"]):null;
		//print_r($plyrs);
		//print_r($wlyrs);

        if(isset($options["poi"]))
            $this->getPOIData($plyrs);


        if(isset($options["way"]))
            $this->getWayData($wlyrs);

    }

    static function criteria($lyrs)
    {
        $qry="";
        if (count($lyrs) != 0)
        {
            $qry .= " AND (";
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

    function addPOIFilter($k,$allowedValues)
    {
        $this->poiFilter[$k]=$allowedValues;
    }

    function addWayFilter($k,$allowedValues)
    {
        $this->wayFilter[$k]=$allowedValues;
    }

    function includeWays($w)
    {
        $this->doWays=$w;
    }

    function includePolygons($p)
    {
        $this->doPolygons=$p;
    }

    function applyFilter($type)
    {
        $qry="";
        $filter = ($type=="poi") ? $this->poiFilter: $this->wayFilter;
        if(count($filter) > 0)
        {
        $firsttag=true;
        $qry .= " AND (";
            foreach($filter as $tag=>$valuelist)
            {
        if($firsttag==true)
        {
                    $qry .= "(";
            $firsttag=false;
        }
        else
            $qry .=" OR (";

                $values = explode(",",$valuelist);
                $first=true;
                foreach($values as $value)
                {
                    if($first==false)
                        $qry .= " OR ";
                    else
                        $first=false;
                    $qry .= "$tag='$value'"; 
                }
                $qry .= ")";
            }
        $qry .= ")";
        }
        return $qry;
    }

    function getPOIData($plyrs)
    {

        //$pqry = $this->dbq->getPOIQuery();
        $pqry = $this->getPOIQuery();

//		echo "POI Query is : $pqry<br />";

        if($plyrs[0]!="all")
            $pqry .= DataGetter::criteria($plyrs);

        $pqry .= DataGetter::applyFilter("poi");



        $presult = $this->conn->query($pqry);
		$errorInfo = $this->conn->errorInfo();
//		print_r($errorInfo);
		if($errorInfo[0]!="00000")
			return;

        while($prow=$presult->fetch(PDO::FETCH_ASSOC))
        {
            $feature=array();
            $feature["type"] = $this->kothic_gran===null?"Feature":"Point";
            $f= json_decode($prow["st_asgeojson"],true);
            $counteddata=array();

            foreach($prow as $k=>$v)    
                if($k!='way' && $k!='st_asgeojson' && $k!='tway' && $v!='')
                    $counteddata[$k]=htmlspecialchars
                        (str_replace("&","and",$v));

            $feature["properties"] = $counteddata;
            $feature["properties"]["featuretype"]=get_high_level
                ($feature["properties"]);
            if($this->kothic_gran===null)
            {
                $feature["geometry"]=array();
                $feature["geometry"]["coordinates"] = $f["coordinates"];
                $feature["geometry"]["type"] = $f["type"];
            }
            else
            {
                $feature["coordinates"]= $this->kothicAdjust($f);
            }
            $this->data["features"][] = $feature;
        }    
    }

    function getWayData($wlyrs)
    {
        $arr=array();
        if($this->doWays) 
            $arr[]="way";
        if($this->doPolygons)
            $arr[]= "polygon"; 

        foreach($arr as $table)
        {
			$f = DataGetter::applyFilter("way");
			$criteria = $wlyrs[0]=="all"?"":DataGetter::criteria($wlyrs);
            $wqry = $this->getWayQuery($table,
				$criteria. " ".
				DataGetter::applyFilter("way"));

		
//			echo "FILTER $f";	
			//echo "Way query: $wqry<br />";

			/*
            if($wlyrs[0]!="all")
                $wqry .= DataGetter::criteria($wlyrs);
            $wqry .= DataGetter::applyFilter("way");
		*/

			/*
			echo "CONSTRAINT ".
				DataGetter::criteria($wlyrs). "  ".
				DataGetter::applyFilter("way"). "<br />";
			*/
			//echo "<br />ACTUAL QUERY $wqry<br />";
            $wresult = $this->conn->query($wqry);
			$errorInfo = $this->conn->errorInfo();
			if($errorInfo[0]!="00000")
				return;

            $first=true;

            $excluded_col = ($table=="polygon") ?
                $this->dbq->polygonDetails["col"]:
                $this->dbq->wayDetails["col"];

            while($wrow=$wresult->fetch(PDO::FETCH_ASSOC))
            {
                $feature=array();
				//echo $wrow['st_asgeojson']. "<br />";
                $f = json_decode($wrow['st_asgeojson'],true);
                $tags = array();
         
   
                // Replace ampersands with the word "and".
                foreach($wrow as $k=>$v)
                    if($k!=$excluded_col && $k!='st_asgeojson' && $k!='tway'
					&& $v!='')
                        $tags[$k] = htmlspecialchars(str_replace("&","and",$v));

                $feature["properties"] = $tags;
                if($this->kothic_gran===null)
                {
                    $feature["type"] = "Feature"; 
                    $feature["geometry"]=array();
                    $feature["geometry"]["coordinates"] = $f["coordinates"];
                    $feature["geometry"]["type"] = $f["type"];
                    if(count($feature["geometry"]["coordinates"])>0)
                        $this->data["features"][] = $feature;
                }
                else
                {
                    $feature["coordinates"]= $this->kothicAdjust($f);
                    $feature["type"] = $f["type"];
                    if(count($feature["coordinates"])>0)
                        $this->data["features"][] = $feature;
                }
            }
        }
    }

    function getPOIQuery()
    {
        return $this->dbq->getPOIQuery();
    }

	// 181116 $constraint not needed but need to satisfy Strict Standards
	// still no overloading in php
    function getWayQuery($table, $constraint)
    {
        return ($table=="polygon") ?
            $this->dbq->getPolygonQuery(): 
        $this->dbq->getWayQuery();
    }

		/* now not done - done in DB query
    function reprojectData($outProj)
    {
        for($f=0; $f<count($this->data["features"]); $f++)
        {
            switch($this->data["features"][$f]["geometry"]["type"])
            {
                case "Point":
                    $this->data["features"][$f]["geometry"]["coordinates"]=
                    reproject
                    ($this->data["features"][$f]["geometry"]["coordinates"][0],
                    $this->data["features"][$f]["geometry"]["coordinates"][1],
                    $this->SRID,$outProj);
                    break;

                case "LineString":
                    for($i=0; 
                        $i<count
                        ($this->data["features"][$f]
                        ["geometry"]["coordinates"]); $i++)
                    {
                        $this->data
                        ["features"][$f]["geometry"]["coordinates"][$i]=
                        reproject
                            ($this->data
                            ["features"][$f]["geometry"]["coordinates"]
                            [$i][0],
                            $this->data["features"][$f]["geometry"]
                            ["coordinates"][$i][1],
                            $this->SRID,$outProj);
                    }
                    break;

                case "MultiLineString":
                case "Polygon":
                    for($i=0; $i<count($this->data["features"][$f]["geometry"]
                        ["coordinates"]); $i++)
                    {
                        for($j=0;$j<count($this->data["features"][$f]
                            ["geometry"]["coordinates"][$i]);$j++)
                        {
                            $this->data
                            ["features"][$f]["geometry"]["coordinates"]
                                [$i][$j]=reproject
                                ($this->data["features"][$f]
                                ["geometry"]["coordinates"][$i][$j][0],
                                $this->data["features"][$f]
                                ["geometry"]["coordinates"][$i][$j][1],
                                $this->SRID,$outProj);
                        }
                    }
                    break;

                case "MultiPolygon":
                    for($i=0; $i<count($this->data["features"][$f]["geometry"]
                        ["coordinates"]); $i++)
                    {
                        for($j=0;$j<count($this->data["features"][$f]
                            ["geometry"]["coordinates"][$i]);$j++)
                        {
                            for($k=0; 
                                   $k<count($this->data["features"][$f]
                                ["geometry"]["coordinates"][$i][$j]);
                                $k++)
                            {
                                $this->data["features"][$f]
                                    ["geometry"]["coordinates"][$i][$j][$k]=
                                    reproject
                                        ($this->data["features"][$f]
                                    ["geometry"]["coordinates"][$i][$j][$k][0],
                                    $this->data["features"][$f]
                                    ["geometry"]["coordinates"][$i][$j][$k][1],
                                    $this->SRID,$outProj);
                            }
                        }
                    }
                    break;
            }
        }
    }
		*/
}

class NameSearch extends DataGetter
{
    protected $name;

    function __construct($name,$outProj,$tbl_prefix="planet_osm",$dbname="gis2",
                            $user="gis")
    {
        parent::__construct(null,$tbl_prefix,"3857","3857",$outProj,
								$dbname,$user);
        $this->name=$name;
    }

    function getPOIQuery()
    {
        return parent::getPOIQuery()." AND name ILIKE '%".$this->name."%'";
    }


	// 181116 $constraint not needed but need to satisfy Strict Standards
	// still no overloading in php
    function getWayQuery($table, $constraint)
    {
        return parent::getWayQuery($table)." AND name ILIKE '%".
            $this->name."%'";
    }
}

class BboxGetter extends DataGetter
{
    private $bbox, $forceCache;
	private $ext; // extend bbox to avoid boundary artefacts

    function __construct($bbox,$bboxSRID="4326",$outSRID="3857",$ext=0,
							$kothic_gran=null,$dbdetails=null,$srid="3857",
                            $dbname="gis2", $user="gis")
    {
        parent::__construct($kothic_gran,$dbdetails,$srid,$bboxSRID,$outSRID,
								$dbname,$user);
        $this->bbox = $bbox;
		$this->ext = ($kothic_gran===null) ? $ext*0.01:0.2; // $ext parameter is a % extension
        $this->geomtxt = $this->mkgeom();
        $this->extGeomtxt = $this->mkExtGeom();
        $this->forceCache = false;
    }

    function setForceCache($fc)
    {
        $this->forceCache = $fc;
    }

	// NW 120815 removed $outProj as it's no longer doing anything
	// (This was done as part of the bbox stuff done circa March but
	// never removed until now)
    function getData($options, $contourCache=null, $cache=null, 
                        $x=null, $y=null, $z=null)
    {
        // Only cache if all was requested and we're in kothic mode 
        $all = isset($options["coastline"]) && $options["coastline"]
            && isset($options["poi"]) && $options["poi"]=="all"
            && isset($options["way"]) && $options["way"]=="all"
            && $this->kothic_gran;

		// test: now only cache if were in kothic (only)
		$all = $this->kothic_gran;

        
        if($this->forceCache)
        {
            $this->getDataFromDB($options);
            $this->cacheData($cache);    
        }
        elseif($cache!==null && $all)
        {
            $result = $this->getCachedData($cache);
// $result = false; // never readin from cache for testing - was done during
// change of extended bbox handling to database level
            if($result===false)
            {
                $this->getDataFromDB($options);
                $this->cacheData($cache);    
            }
            /* if cached tile over 1 day old, add to tilelist */
            elseif(time() - $result > MAX_TILE_AGE && $z >= 11)
            {
                $fp = fopen (TILELIST, "a");
                if($fp!==false)
                {
                    fwrite($fp,"$x $y $z\n");
                    fclose($fp);
                }
            }
        }
        else
        {
            $this->getDataFromDB($options);
        }

        if(isset($options["contour"]) && $options["contour"])
            $this->getContourData($contourCache);

		/*
        if($outProj!==null)
            $this->reprojectData($outProj);
		*/

        return $this->data;
    }

    function getDataFromDB($options)
    {
        
        parent::doGetData($options);
        
        if(isset($options["coastline"]) && $options["coastline"])
            $this->getCoastlineData();
            

        if( (isset($options["ann"]) && $options["ann"]) ||
         (isset($options["annotation"]) && $options["annotation"]) ) 
        {
            $this->getAnnotationData();
        }
        elseif(isset($options["overlay"]))
        {
            $this->getOverlayData($options["overlay"]);
        }
    }

    function cacheData($cache)
    {
        file_put_contents($cache,json_encode($this->data["features"]));
    }

    function getCachedData($cache)
    {
        if($this->kothic_gran!==null && $cache!==null)
        {
            if(!file_exists($cache))
            {
                return false;
            }
            else
            {
                $txt=file_get_contents($cache);
                $this->data["features"]=json_decode($txt,true);
                return filemtime($cache); 
            }
        }
        return false;
    }

    function getContourData($contourCache=null)
    {
        if($this->kothic_gran!==null && $contourCache!==null)
        {
            if(!file_exists($contourCache))
            {
                $contourData=$this->doGetContourData();
                file_put_contents($contourCache,json_encode($contourData));
            }
            else
            {
                $txt=file_get_contents($contourCache);
                $contourData=json_decode($txt,true);
            }
        }
        else
        {
            $contourData=$this->doGetContourData();
        }
        $this->data["features"] = array_merge($this->data["features"], 
            $contourData);
    }


    function doGetContourData()
    {
        $features=array();
        $q=$this->dbq->getContourQuery
			($this->ext>0 ? $this->extGeomtxt: $this->geomtxt);
        if($q===null)
            return;
        $result=$this->conn->query($q);
        while($row=$result->fetch(PDO::FETCH_ASSOC))
        {
            $feature=array();
            $f = json_decode($row['st_asgeojson'],true);
            $tags = array();
            $feature["properties"] = array();
            $feature["properties"]["contour"]=$row["height"];
            $feature["properties"]["contourtype"]=$row["height"]%50==0?
					"major":"minor";
            if($this->kothic_gran===null)
            {
                $feature["type"]="Feature";
                $feature["geometry"]=array();
                $feature["geometry"]["coordinates"] = $f["coordinates"];
                $feature["geometry"]["type"] = $f["type"];
                if(count($feature["geometry"]["coordinates"])>0)
                    $features[] = $feature;
            }
            else
            {
                $feature["coordinates"] = $this->kothicAdjust($f);
                $feature["type"] = $f["type"];
                if(count($feature["coordinates"])>0)
                    $features[] = $feature;
            }
        }
        return $features;
    }

    function getCoastlineData()
    {
        if($this->kothic_gran!==null)
        {
            $factor = $this->kothic_gran / ($this->bbox[2]-$this->bbox[0]);
        }
        $q=$this->dbq->getCoastlineQuery($this->geomtxt);
        if($q===null)
            return;
        $result=$this->conn->query($q);
        while($row=$result->fetch(PDO::FETCH_ASSOC))
        {
            $feature=array();
            $f = json_decode($row['st_asgeojson'],true);
            $tags = array();
            $feature["properties"] = array();
            $feature["properties"]["natural"] = "land"; 
            if($this->kothic_gran===null)
            {
                $feature["type"]="Feature";
                $feature["geometry"]=array();
                $feature["geometry"]["coordinates"] = $f["coordinates"];
                $feature["geometry"]["type"] = $f["type"];
                if(count($feature["geometry"]["coordinates"])>0)
                    $this->data["features"][] = $feature;
            }
            else
            {
                $feature["coordinates"] =$this->kothicAdjust($f);
                $feature["type"] = $f["type"];
                if(count($feature["coordinates"])>0)
                    $this->data["features"][] = $feature;
            }
        } 
    }
    
    function getAnnotationData()
    {
        self::getOverlayData("annotation");
    }

    function getOverlayData($type)
    {
        $pqry = $this->dbq->getOverlayQuery($this->geomtxt, $type); 
        if($pqry===null)
            return;

        $presult = $this->conn->query($pqry);
        while($prow=$presult->fetch(PDO::FETCH_ASSOC))
        {
            $feature=array();
            $feature["type"] = "Feature";
            $f= json_decode($prow["st_asgeojson"],true);
            $counteddata=array();
            foreach($prow as $k=>$v)    
                if($k!='authorised' && $k!='st_asgeojson' &&  $k!='tway' &&
                    $k!=$this->dbq->overlayDetails["col"] && $v!='')
                    $counteddata[$k]=$v;
            $feature["properties"] = $counteddata;
            $feature["properties"][$type] = "yes";
            $feature["geometry"]=array();
            $feature["geometry"]["coordinates"] = $f["coordinates"];
            $feature["geometry"]["type"] = $f["type"];
            $this->data["features"][] = $feature;
        }    
    }

    function mkgeom()
    {
        $bbox=$this->bbox;
    /*
        $g="ST_GeomFromText('POLYGON(($bbox[0] $bbox[1],$bbox[2] $bbox[1], ".
            "$bbox[2] $bbox[3],$bbox[0] $bbox[3],$bbox[0] $bbox[1]))',".
            $this->SRID.")";    
    */
	// now use the native srid and transform later
    $g = "ST_SetSRID('BOX3D($bbox[0] $bbox[1],$bbox[2] $bbox[3])'::box3d,".
        $this->SRID.")";
        return $g; 
    }

    function mkExtGeom()
    {
        $bbox=$this->bbox;
        $w = $this->bbox[2] - $this->bbox[0];
        $h = $this->bbox[3] - $this->bbox[1];

        $bbox[0] = $bbox[0] - $w*$this->ext; 
        $bbox[2] = $bbox[2] + $w*$this->ext; 
        $bbox[1] = $bbox[1] - $h*$this->ext; 
        $bbox[3] = $bbox[3] + $h*$this->ext;
    /*
        $g="ST_GeomFromText('POLYGON(($bbox[0] $bbox[1],$bbox[2] $bbox[1], ".
            "$bbox[2] $bbox[3],$bbox[0] $bbox[3],$bbox[0] $bbox[1]))',".
            $this->SRID.")";
    */
    $g = "ST_SetSRID('BOX3D($bbox[0] $bbox[1],$bbox[2] $bbox[3])'::box3d,".
        $this->SRID.")";
        return $g; 
    }

    function kothicAdjust($f)
    {
        $factor = $this->kothic_gran / ($this->bbox[2]-$this->bbox[0]);
        $coords=array();
        switch($f["type"])
        {
            case "Point":
                $x =  (int)    
                    (($f["coordinates"][0] - $this->bbox[0]) * $factor);
                $y =     (int)
                    (($f["coordinates"][1] - $this->bbox[1]) * $factor);
            $coords = array($x,$y);
            break;

            case "LineString":
                for($i=0; $i<count($f["coordinates"]); $i++)
                {
                    $x =  (int)    
                    round(($f["coordinates"][$i][0]-$this->bbox[0]) * $factor);
                    $y =     (int)
                    round(($f["coordinates"][$i][1]-$this->bbox[1]) * $factor);
                    // coords of (0,0) seem to  screw up rendering
			
					
                    $x=($x==0)?1:$x;
                    $y=($y==0)?1:$y;
    
					
                    $x=($x==$this->kothic_gran)?$this->kothic_gran-1:$x;
                    $y=($y==$this->kothic_gran)?$this->kothic_gran-1:$y;
					
					
		

                    if($x>=0 && $y>=0 && $x<=$this->kothic_gran && $y<=$this->kothic_gran)
//            if(true)
                    {
                           $coords[] = array($x,$y);
                    }
                }
                break;

            case "MultiLineString":
            case "Polygon":
                for($i=0; $i<count($f["coordinates"]); $i++)
                {
                    $coords[$i]=array();
                    for($j=0; $j<count($f["coordinates"][$i]); $j++)
                    {
                        $x =  (int)    
                        round(($f["coordinates"][$i][$j][0]-
                        $this->bbox[0]) * $factor);
                        $y =     (int)
                        round(($f["coordinates"][$i][$j][1]-
                        $this->bbox[1]) * $factor);
                        // coords of (0,0) seem to screw up rendering
                        if($f["type"]=="MultiLineString")
                        {
                            $x=($x==0)?1:$x;
                            $y=($y==0)?1:$y;
                            $x=($x==$this->kothic_gran)?$this->kothic_gran-1:$x;
                            $y=($y==$this->kothic_gran)?$this->kothic_gran-1:$y;
                        }
            
                        $coords[$i][] = array($x,$y);

                    }
                }
                break;

            case "MultiPolygon":
                for($i=0; $i<count($f["coordinates"]); $i++)
                {
                    $coords[$i]=array();
                    for($j=0; $j<count($f["coordinates"][$i]); $j++)
                    {
                        $coords[$i][$j]=array();
                        for($k=0; $k<count($f["coordinates"][$i][$j]); $k++)
                        {
                            $x =  (int)    
                            round(($f["coordinates"][$i][$j][$k][0]-
                            $this->bbox[0]) * 
                            $factor);

                            $y =     (int)
                            round(($f["coordinates"][$i][$j][$k][1]-
                            $this->bbox[1]) * 
                            $factor);

                // coords of (0,0) screw up rendering
                            $x=($x==0)?1:$x;
                            $y=($y==0)?1:$y;
                            $x=($x==$this->kothic_gran)?$this->kothic_gran-1:$x;
                            $y=($y==$this->kothic_gran)?$this->kothic_gran-1:$y;
                            $coords[$i][$j][] = array($x,$y);
                        }
                    }
                }
                break;
        }
        return $coords;
    }

    function getPOIQuery()
    {
		return $this->dbq->getBboxPOIQuery
//				($this->kothic_gran===null ? $this->geomtxt: $this->extGeomtxt);
				($this->ext>0 ? $this->extGeomtxt: $this->geomtxt);

    }

    function getWayQuery($table, $constraint)
    {
        return ($table=="polygon") ?
            $this->dbq->getBboxPolygonQuery($this->geomtxt, $constraint):
            $this->dbq->getBboxWayQuery($this->geomtxt, $constraint);
    }

    function getUniqueList($property)
    {
        $values = array();
        foreach($this->data["features"] as $f)
        {
            if(!in_array($f["properties"][$property],$values))
            {
                $values[] = $f["properties"][$property];
            }
        }
        return $values;
    }

    function simpleGetData()
    {
        return $this->data;
    }
}

?>
