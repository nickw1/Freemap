<?php
class Photosphere
{
    private $lat, $lon, $gpano;

    function __construct($file)
    {
        $this->lat = false; 
        $this->lon = false;
		$this->gpano = false;
        $exif = exif_read_data($file);
        self::getLocation($exif);
        self::getXMP($file);
    }

    private function getXMP($file)
    {
        $fp = fopen ($file, "r");
        if($fp!==false)
        {
            $xmp = "";
            $readingXMP = $xmpStart = $xmpEnd = false;
            while($xmpEnd===false && $line = fread($fp,1024))
            {
                $xmpStart = $readingXMP==true?false:strpos($line, "<x:xmpmeta");
                $xmpEnd=$readingXMP==false?false:strpos($line, "</x:xmpmeta>");

                if($xmpStart!==false)
                    $readingXMP = true;

                if($xmpEnd!==false)
                {
                    $xmpEnd+=12;
                    $readingXMP = false;
                    $xmp .= substr($line,$xmpStart===false?0:$xmpStart,$xmpEnd);
                }
                elseif($readingXMP==true)
                    $xmp.=$xmpStart!==false ?  substr($line,$xmpStart) : $line;

            }
            fclose($fp);
            $xml = simplexml_load_string ($xmp);
            if($xml!==false)
            {
                $rdf = $xml->children("rdf",true);
                if($rdf->count() == 1 && $rdf->RDF->Description)
                {
                    $gpano = $rdf->RDF->Description->attributes("GPano", true);
                    if($gpano->count() > 0)
                    {
                        foreach ($gpano as $k=>$v)
                            $this->gpano[$k] = $v;
                    }
                }
            }
        }
    }

    function hasLocation()
    {
        return $this->lat !== false && $this->lon !== false;
    }

	function hasGPano()
	{
		return $this->gpano!==false;
	}

    function getLatitude()
    {
        return $this->lat;
    }

    function getLongitude()
    {
        return $this->lon;
    }

	function getGPanoAttribute($k)
	{
		return isset($this->gpano[$k]) ? $this->gpano[$k] : false;
	}

    private function getLocation($exif)
    {
        if(isset($exif['GPSLatitude']) && isset($exif['GPSLongitude']))
        {
            $this->lat = self::toDecimalDegrees($exif['GPSLatitude']);
            $this->lon = self::toDecimalDegrees($exif['GPSLongitude']);
            if($exif["GPSLatitudeRef"]=="S")
                $this->lat = -$this->lat;
            if($exif["GPSLongitudeRef"]=="W")
                $this->lon = -$this->lon;
        }
    }

    private function toDecimalDegrees($dms)
    {
        return self::fracToDec($dms[0]) + self::fracToDec($dms[1])/60.0 +
                        self::fracToDec($dms[2])/3600.0;
    }

    private function fracToDec($frac)
    {
        $components = explode("/", $frac);
        return (is_array($components) && count($components)==2) ?
                $components[0]/$components[1] : $frac;
    }
}
?>
