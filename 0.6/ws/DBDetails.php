<?php
class DBDetails
{
    public $poiDetails, $wayDetails, $polygonDetails, $contourDetails, 
            $coastlineDetails, $annotationDetails;

    public function __construct($poi, $way, $poly, $contour, 
                                    $coast, $ann)
    {
        $this->poiDetails = $poi;
        $this->wayDetails = $way;
        $this->polygonDetails = $poly;
        $this->contourDetails = $contour;
        $this->coastlineDetails = $coast;
        $this->annotationDetails = $ann;
        $this->intersection = true;
    }

    public function setIntersection($i)
    {
        $this->intersection = $i;
    }

    public function getPOIQuery()
    {
        return ($this->poiDetails) ? 
            "SELECT *,ST_AsGeoJSON(".$this->poiDetails["col"].
            ") AS geojson ".
        " FROM ".$this->poiDetails["table"]." WHERE true" : null;
    }

    public function getWayQuery()
    {
        return ($this->wayDetails) ?
            "SELECT *,ST_AsGeoJSON(".$this->wayDetails["col"].
            ") AS geojson FROM ".$this->wayDetails["table"]. " WHERE true" :
            null;
    }

    public function getPolygonQuery()
    {
        return ($this->polygonDetails) ?
            "SELECT *,ST_AsGeoJSON(".$this->polygonDetails["col"].
            ") AS geojson FROM ".$this->polygonDetails["table"]. " WHERE true" :
            null;
    }

    public function getContourQuery($geomtxt)
    {
        return ($this->contourDetails) ?
           "SELECT ST_AsGeoJSON(ST_Intersection($geomtxt,".
                $this->contourDetails["col"].  ")) ".
                    "AS geojson,height ".
                    "FROM ".
                    $this->contourDetails["table"].
                    " WHERE ".
                    $this->contourDetails["col"] ." && $geomtxt": null;
    }

    public function getCoastlineQuery($geomtxt)
    {
        return ($this->coastlineDetails) ?
        "SELECT ST_AsGeoJSON".
            "(ST_Intersection($geomtxt,".
                $this->coastlineDetails["col"].")) ".
                    "AS geojson ".
                    "FROM ". $this->coastlineDetails["table"].
                    " WHERE ".
                    $this->coastlineDetails["col"] . "&& $geomtxt" : null;
    }

    public function getAnnotationQuery($geomtxt)
    {
        return ($this->annotationDetails) ?
        "SELECT *,ST_AsGeoJSON(".$this->annotationDetails["col"].
        ") AS geojson ".
        " FROM ".$this->annotationDetails["table"].
        " WHERE ".$this->annotationDetails["col"].
            "&& $geomtxt" : null;
    }


    public function getBboxWayQuery($geomtxt)
    {
        $q = ($this->intersection==true) ?

        "SELECT *,ST_AsGeoJSON(ST_Intersection($geomtxt,".
            $this->wayDetails["col"].")) AS geojson ".
            "FROM ".$this->wayDetails["table"]. 
            " WHERE ".
            $this->wayDetails["col"] .
            "&& $geomtxt AND ST_IsValid(".$this->wayDetails["col"].")" 

            :

        "SELECT *,ST_AsGeoJSON(".$this->wayDetails["col"].") AS geojson ".
            "FROM ".$this->wayDetails["table"]. 
            " WHERE ".
            $this->wayDetails["col"] .
            "&& $geomtxt AND ST_IsValid(".$this->wayDetails["col"].")" ;

        return ($this->wayDetails) ? $q:null;
    }

    public function getBboxPolygonQuery($geomtxt)
    {
        return ($this->polygonDetails) ?
        "SELECT *,ST_AsGeoJSON(ST_Intersection($geomtxt,".
            $this->polygonDetails["col"].")) AS geojson ".
            "FROM ".$this->polygonDetails["table"]. 
            " WHERE ".
            $this->polygonDetails["col"] .
            "&& $geomtxt AND ST_IsValid(".$this->polygonDetails["col"].")" :
            null;
    }
}
