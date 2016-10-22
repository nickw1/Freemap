<?php

// 250215 reproject ways from DB using ST_Transform if desired
// (e.g. reduce boundary artefacts on hikar)

// SRID: the native SRID of the database
// tSRID: the transformed SRID, to transform the output data and bounding
// box to

class DBDetails
{
    public $poiDetails, $wayDetails, $polygonDetails, $contourDetails, 
            $coastlineDetails, $overlayDetails;

    public function __construct($poi, $way, $poly, $contour, 
                                    $coast, $ann, $SRID, $tSRID, $outSRID)
    {
        $this->poiDetails = $poi;
        $this->wayDetails = $way;
        $this->polygonDetails = $poly;
        $this->contourDetails = $contour;
        $this->coastlineDetails = $coast;
        $this->overlayDetails = $ann;
        $this->intersection = true;
        $this->tSRID = $tSRID;
        $this->SRID = $SRID;
		$this->outSRID = $outSRID;
    }

    public function setIntersection($i)
    {
        $this->intersection = $i;
    }

    public function getPOIQuery()
    {
            return ($this->poiDetails) ?
            $this->trWayQuery($this->poiDetails["table"],
                            $this->poiDetails["col"]) : null;
    }

    public function getWayQuery()
    {
            return ($this->wayDetails) ?
            $this->trWayQuery($this->wayDetails["table"],
                            $this->wayDetails["col"]) : null;
    }

    public function getPolygonQuery()
    {
            return ($this->polygonDetails) ?
            $this->trWayQuery($this->polygonDetails["table"],
                            $this->polygonDetails["col"]) : null;
    }

    // 031113 altered getAnnotationQuery() to getOverlayQuery() so that
    // the same code can be used to load any point overlay e.g. panoramas

	
    public function getOverlayQuery($geomtxt, $type)
    {
        $q = ($this->overlayDetails) ?
                $this->trWayIntersectQuery("${type}s",
                                    $this->overlayDetails["col"], $geomtxt):
                null;
		return $q;
    }
	
    // 020315
    // ASSUMPTION for all of these
    // $geomtxt is already in tSRID

    public function getContourQuery($geomtxt, $constraint="")
    {
        return ($this->contourDetails) ?
                $this->trWayIntersectQuery($this->contourDetails["table"],
                                    $this->contourDetails["col"], $geomtxt,
                                    $constraint) :
                null;
    }

    public function getCoastlineQuery($geomtxt, $constraint="")
    {
        return ($this->coastlineDetails) ?
                $this->trWayIntersectQuery($this->coastlineDetails["table"],
                                    $this->coastlineDetails["col"], $geomtxt,
                                    $constraint) :
                null;
    }



    public function getBboxWayQuery($geomtxt, $constraint="")
    {
        return ($this->wayDetails) ?
                $this->trWayIntersectQuery($this->wayDetails["table"],
                                    $this->wayDetails["col"], $geomtxt,
                                    $constraint) :
                null;
    }

    public function getBboxPolygonQuery($geomtxt, $constraint="")
    {
        return ($this->polygonDetails) ?
                $this->trWayIntersectQuery($this->polygonDetails["table"],
                                    $this->polygonDetails["col"], $geomtxt,
                                    $constraint) :
                null;
    }

    public function getBboxPOIQuery($geomtxt, $constraint="")
    {
/*
        $tway = $this->tSRID != $this->SRID ?
            "ST_Transform(".$this->poiDetails["col"].
            ",$this->tSRID)":
            $this->poiDetails["col"];

        return $this->trWayQuery($this->poiDetails["table"],
                            $this->poiDetails["col"]).
                 " AND $tway && $geomtxt";
*/
        return ($this->poiDetails) ?
                $this->trWayIntersectQuery($this->poiDetails["table"],
                                    $this->poiDetails["col"], $geomtxt,
                                    $constraint) :
                null;
    }


    // approach: find all ways in the untransformed bbox (expanded to ensure
    // it covers the transformed bbox) then find all transformed ways in the
    // transformed bbox from this set of ways
    private function trWayIntersectQuery($tbl,$col,$geomtxt,$constraint="",
                            $outputFunc="ST_AsGeoJSON")
    {
		$outway = "ST_Intersection(t.tway,".
					"ST_Transform($geomtxt,$this->tSRID))";
		$outway_nt = "ST_Intersection($col,$geomtxt)";
        return $this->tSRID != $this->SRID ?
            "SELECT *,$outputFunc(".
			(($this->tSRID == $this->outSRID) ?
				$outway : "ST_Transform($outway, $this->outSRID)") .
            ") FROM ".
            "(SELECT *, ST_Transform(s.$col,$this->tSRID) AS tway FROM ".
            "(SELECT * FROM $tbl WHERE true $constraint AND $col && ".
            "$geomtxt) s) t WHERE t.tway && ".
            "ST_Transform($geomtxt,$this->tSRID)":
            "SELECT *,$outputFunc(".
			(($this->tSRID == $this->outSRID) ?
				$outway_nt : "ST_Transform($outway_nt, $this->outSRID)") .
			") FROM $tbl WHERE ".
            "$col && $geomtxt $constraint";

		/* SELECT *, geojson ( transform
				(intersection(tway,transform(Geomtxt,srid)), outsrid)
			from ... */
    }    

    private function trWayQuery($tbl,$col,$constraint="",
                        $outputFunc="ST_AsGeoJSON")
    {
		$tr = $this->outSRID == $this->SRID ?
				$col: "ST_Transform($col,$this->outSRID)";
        return $this->tSRID != $this->SRID ? 
            "SELECT *,$outputFunc(t.tway) FROM ".
            "(SELECT *, ST_Transform(s.$col,$this->tSRID) AS tway FROM ".
            "(SELECT * FROM $tbl WHERE true $constraint ".
            ") s) t WHERE true ":
            "SELECT *,$outputFunc($tr) FROM $tbl WHERE true "; 
    }    
}
