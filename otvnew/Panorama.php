<?php
// Class to represent a submitted panorama
// A submitted panorama differs from a plain photosphere:
// it is a specific entity for opentrailview and includes associated database 
// data, etc.

require_once('../common/defines.php');

class Panorama
{
    private $id, $row;

    function __construct ($id)
    {
        $this->id = $id;    
    }


    function getRawImageData()
    {
        if($this->getDBData()!==false)
        {
            $file = OTV_UPLOADS . "/". $this->id.".jpg";
            return file_exists($file) ? file_get_contents($file) : false;
        }
        return false;
    }
    
    function getDBData()
    {
        $result=pg_query
            ("SELECT * FROM panoramas WHERE id=".$this->id . 
                " AND authorised=1");
        return pg_num_rows($result)==1 ?
            pg_fetch_array ($result, null, PGSQL_ASSOC) : false;
    }

    function getId()
    {
        return $this->id;
    }

    static function getNearest ($lon, $lat)
    {
        $sm = reproject ($lon, $lat, "4326", "900913");
        $result=pg_query
            ("SELECT * FROM panoramas ORDER BY ".
                "Distance(GeomFromText('POINT($sm[0] $sm[1])',900913),xy) ".
                "LIMIT 1");
        if(pg_num_rows($result)==1)
        {
            $row=pg_fetch_array($result, null, PGSQL_ASSOC);
            return new Panorama($row["id"]);
        }
        return false;
    }

}
