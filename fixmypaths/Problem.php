<?php
require_once('../lib/conversions.class.php');
require_once('../lib/conversionslatlong.class.php');
require_once('../lib/functionsnew.php');

class Problem
{
    private $details;
    private static $statuses = array ("Reported", "Fixed");

    public function  __construct($input)
    {

        if (!is_array($input))
        {
            $result = pg_query("SELECT * FROM hampshire_problems WHERE ".
                                "id=$input");
            $this->details = pg_fetch_array($result,NULL,PGSQL_ASSOC);
        }
        // NB is_int() doesn't seem to work as expected, assume an int if here
        else
        {
            $this->details = $input;
        }
            
    }

    public function output($format,$outProj)
    {
        if($format=="xml")
        {
            $coords=reproject($this->details["x"],$this->details["y"],
                "3857",$outProj);
            return "<annotation x='$coords[0]' y='$coords[1]' ".
                        "id='".$this->details["id"]."'>".
                        "<description>".
                        $this->details["problem"]."</description>".
                        "</annotation>";
        }
        else
        {
            $f=array();
            $f["type"]="Feature";
            $f["geometry"]=array();
               $f["geometry"]["type"] = "Point";
            $f["geometry"]["coordinates"] = reproject
                ($this->details["x"],$this->details["y"],"3857",$outProj);
            $f["properties"]=array();
            foreach($this->details as $k=>$v)
            {
                if($k!="x" && $k!="y")
                    $f["properties"][$k]=($k=="status") ? 
                        Problem::$statuses[$v]:
                    $v;
            }
            $ll = reproject($this->details["x"],$this->details["y"],
                900913,4326);
            $c = new ConversionsLatLong();
            $osgb = $c->wgs84_to_osgb36($ll[1],$ll[0]);
            $f["properties"]["gridref"] = $c->osgb36_to_gridref
                ($osgb[0],$osgb[1]);
            return $f;
        }
    }

    public function getLocation()
    {
        return reproject($this->details["x"],$this->details["y"],
            "3857",$outProj);
    }

    public function fix()
    {
        $q="UPDATE hampshire_problems SET status=1 WHERE id=".
            $this->details["id"];
        pg_query($q);
    }

    public function remove()
    {
        pg_query("DELETE FROM hampshire_problems WHERE id=".
            $this->details["id"]);
        $this->details = null;
    }

    public function addToLog($log)
    {
        $q = "INSERT INTO problems_log (problemid, log, subdate) ".
                "VALUES (".$this->details["id"].",'$log',NOW())";
        pg_query($q);
    }

    public function getLog()
    {
        $q = "SELECT log,to_char(subdate,'Mon dd YYYY,HH24:MI') AS subdate ".
                "FROM problems_log WHERE problemid=". 
                    $this->details["id"]. " ORDER BY subdate";
        $result=pg_query($q);
        $log=array();
        while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
        {
            $log[] = $row;
        }
        return $log;
    }

    public static function getAllProblems($bbox=null)
    {
        $prob=array();
        $q="SELECT hp.id,hp.problem,hp.x,hp.y,hp.status,row.parish_row,".
			"hp.status,".
            "row.county,row.district,row.parish,row.routeno,row.row_type,".
           "to_char(hp.subdate,'Mon dd YYYY, HH24:MI') AS subdate ".
        "FROM hampshire_problems hp, hampshire row WHERE hp.row_gid=row.gid ";
        if(is_array($bbox) && count($bbox)==4)
            $q .= " AND x BETWEEN ".  $bbox[0]. " AND  ". $bbox[2].
                "AND y BETWEEN ". $bbox[1] . " AND ". $bbox[3];
        $q .=" ORDER BY hp.id DESC";
        $result=pg_query($q);
        while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
            $prob[] = new Problem($row);
        return $prob;
    }

    public static function getProblemsByTime($days)
    {
        $prob=array();
        $q="SELECT hp.id,hp.problem,hp.x,hp.y,hp.status,row.parish_row,".
			"hp.status,".
            "row.county,row.district,row.parish,row.routeno,row.row_type,".
           "to_char(hp.subdate,'Mon dd YYYY, HH24:MI') AS subdate,".
            "row.parish FROM hampshire_problems hp, hampshire row ".
            "WHERE ".
            "NOW()-subdate < INTERVAL '$days days' ".
            "AND hp.row_gid=row.gid ".
            "ORDER BY NOW()-subdate";
        $result=pg_query($q);
        while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
            $prob[] = new Problem($row);
        return $prob;
    }

    public static function getProblemsByAdmUnit($admunits)
    {
        $prob=array();
        $qry="SELECT hp.id,hp.problem,hp.x,hp.y,hp.status,row.parish_row, ".
			"hp.status,".
            "row.county,row.district,row.parish,row.routeno,row.row_type,".
           "to_char(hp.subdate,'Mon dd YYYY, HH24:MI') AS subdate,".
            "row.parish FROM hampshire_problems hp, hampshire row ".
            "WHERE hp.row_gid=row.gid";
        foreach($admunits as $admunit=>$value)
        {    
            if($value!="all" && $value!="")
                $qry .= " AND $admunit='$value'";
        }
        $result=pg_query($qry);
        while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
            $prob[] = new Problem($row);
        return $prob;
    }
}
