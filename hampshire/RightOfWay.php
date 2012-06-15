<?php

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

    public function output() 
    {
        echo json_decode($this->the_data);
    }

    public function addProblem($problem,$x,$y)
    {
		if($this->isValid())
		{
        	$q=("INSERT INTO hampshire_problems(row_gid,problem,x,y) VALUES (".
            $this->the_data["properties"]["gid"].",'$problem',$x,$y) ");
			pg_query($q);
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
				$json[] = $row;
			return json_encode($json);
		}
	}

	public static function getAllProblems($bbox=null)
	{
		$prob=array();
		$q="SELECT hp.id,hp.problem,hp.x,hp.y,row.parish_row ".
		"FROM hampshire_problems hp, hampshire row WHERE hp.row_gid=row.gid ";
		if(is_array($bbox) && count($bbox)==4)
			$q .= " AND x BETWEEN ".  $bbox[0]. " AND  ". $bbox[2].
				"AND y BETWEEN ". $bbox[1] . " AND ". $bbox[3];
		$q .=" ORDER BY hp.id DESC";
		$result=pg_query($q);
		while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
			$prob[] = $row;
		return $prob;
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
}

?>
