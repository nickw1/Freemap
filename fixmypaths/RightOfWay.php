<?php
define('HANTS_EMAIL','countryside@hants.gov.uk');

require_once('Problem.php');
require_once('../lib/conversions.class.php');
require_once('../lib/conversionslatlong.class.php');

class RightOfWay
{
    private $the_data, $id;

    public function  __construct($id=0)
    {
        $this->the_data = null; 
        if($id>0)
        {
            $result=pg_query("SELECT *,ST_AsGeoJSON(the_geom) AS geojson FROM ".
                        "hampshire WHERE gid=$id");
            if(pg_numrows($result)==1)
            {
                $this->the_data = array();
                $this->the_data["type"] = "Feature";
                $row=pg_fetch_array($result,null,PGSQL_ASSOC);
                $this->loadFromRow($row);
            }
        }
    }

    public function isValid()
    {
        return $this->the_data!==null;
    }

    public function loadFromRow($row)
    {
        if($this->the_data===null)
            $this->the_data = array();
        foreach($row as $k=>$v)
        {
            if($k!="the_geom" && $k!="geojson")
                $this->the_data["properties"][$k] = $v;
            elseif($k=="geojson")
            {
                $this->the_data["geometry"]=json_decode($v,true);    
            }
        }
    }

    public function output($format="geojson",$segtag="rightofway") 
    {
        switch($format)
        {
            case "json":
            case "geojson":
                echo json_encode($this->the_data);
                break;

            case "xml":
                echo "<$segtag>";
                foreach($this->the_data["properties"] as $k=>$v)
                    echo "<$k>$v</$k>";
                echo "</$segtag>";
                break;
        }
    }

    public function addProblem($problem,$category,$name,$email,$x,$y)
    {
        if($this->isValid())
        {
            $xy = reproject($x,$y,"4326","3857");
            $q="INSERT INTO hampshire_problems(row_gid,problem,category,x,y,".
                "subdate) VALUES (".  
                $this->the_data["properties"]["gid"].
                ",'$problem','$category',".$xy[0].",".$xy[1].",NOW()) ";
            pg_query($q);

            $msg = "Parish: ".$this->the_data["properties"]["parish"]."\n".
                    "ROW number: ".$this->the_data["properties"]["routeno"].
                    "\nROW type: ".$this->the_data["properties"]["row_type"].
                    "\n\nCategory: $category\n\nProblem: $problem\n\n";
            $c = new ConversionsLatLong();
            $osgb = $c->wgs84_to_osgb36($y,$x);
            //$gr = $c->osgb36_to_gridref($osgb[0],$osgb[1]);
            $msg .= "Grid ref: easting ".round($osgb[0])." northing ".
                round($osgb[1])."\n\n"; 
            $msg .= "Reported By: $name (email $email)";
            if(this->the_data["properties"]["county"]=="Hampshire")
            {
                mail(HANTS_EMAIL, 
                    "Right Of Way problem reported via FixMyPaths",$msg);
            }
        }
    }

    public function getProblems()
    {
        if($this->isValid())
        {
            $json=array();
            $result=pg_query
                ("SELECT * FROM hampshire_problems WHERE row_gid=".
                    $this->the_data["properties"]["gid"]);
            while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
            {
                if(!isset($json["row_gid"]))
                    $json["row_gid"] = $row["row_gid"];
                $problem=array();
                $problem["text"] = $row["problem"];
                $problem["x"] = $row["x"];
                $problem["y"] = $row["y"];
                $json["problems"][]=$problem;
            }
            return $json;
        }
    }


    public static function getParishes($county)
    {
        $parishes=array();
        $q = "SELECT DISTINCT parish FROM hampshire ORDER BY parish";
        $result=pg_query($q);
        while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
            $parishes[] = $row["parish"];
        return $parishes;
    }

    public static function findClosest($x,$y,$dist)
    {
        $geom="GeomFromText('POINT($x $y)',3857)";
        $result=pg_query
            ("SELECT *,ST_AsGeoJSON(the_geom) AS geojson FROM hampshire WHERE ".
                "Distance(the_geom,$geom) < $dist ".
                "ORDER BY Distance(the_geom,$geom) LIMIT 1");
        $rightofway=null;
        if(pg_numrows($result)>0)
        {    
            $rightofway=new RightOfWay();
            $rightofway->loadFromRow(pg_fetch_array($result,null,PGSQL_ASSOC));
        }
        return $rightofway; 
    }

    public static function findFromRouteNo($parish,$routeno)
    {
        $ROWs=array();
        $result=pg_query("SELECT *,ST_AsGeoJSON(the_geom) AS geojson ".
                "FROM hampshire WHERE parish='$parish' ".
                " AND routeno='$routeno'");
        while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
        {
            $rightofway = new RightOfWay();
            $rightofway->loadFromRow($row);
            $ROWs[] = $rightofway;
        }
        return $ROWs;
    }
}

?>
