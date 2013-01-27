<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-07 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################
//header("Content-type: text/xml");

#globals
$inTrkpt =  false;
$inDoc =  false;
$inTrk =  false;
$inWpt = false;
$inWptName=false;
$inTime = false;
$trackpoints = array();
$curWpt = null;
$waypoints = array();
$curPt = null;
$trkName = $trkDesc = "";
#end globals

function parseGPX($gpx)
{
    global $trackpoints, $waypoints, $trkName, $trkDesc;

    $parser = xml_parser_create();
    xml_set_element_handler($parser,"on_start_element_gpx",
                "on_end_element_gpx");
    xml_set_character_data_handler($parser,"on_characters_gpx");

	if(!is_array($gpx))
		$gpx = explode("\n", $gpx);
    foreach($gpx as $line)    
    {
        if (!xml_parse($parser,$line))
            return false;    
    }

    xml_parser_free($parser);
    return array ("trk"=>$trackpoints, "wp"=>$waypoints,
					"name"=>$trkName, "desc"=>$trkDesc);
}

#NB the PHP expat library reads in all tags as capitals - even if they're
#lower case!!!
function on_start_element_gpx($parser,$element,$attrs)
{
    global $inDoc, $inTrk, $inTrkpt, $trackpoints, $inWpt, $curWpt, $inWptName,
            $inTime, $curPt, $inTrkName, $inTrkDesc, $inWptDesc;

    if($element=="GPX")
    {
        $inDoc = true;
    }
    elseif($inDoc)
    {
        if($element=="TRK")
        {
            $inTrk=true;
        }
        elseif($element=="TRKPT" && $inTrk)
        {
            $inTrkpt=true;
			$curPt = array();
            foreach($attrs as $name => $value)
            {
                if($name=="LAT")
                    $curPt["lat"] = $value; 
                elseif($name=="LON")
                    $curPt["lon"] = $value; 
            }
        }
        elseif($element=="TIME" && $inTrkpt)
        {
            $inTime=true;
        }
        elseif($element=="WPT")
        {
            $inWpt = true;
            $curWpt =array();
            foreach($attrs as $name => $value)
            {
                if($name=="LAT")
                    $curWpt["lat"] = $value; 
                elseif($name=="LON")
                    $curWpt["lon"] = $value; 
            }
        }
        elseif($element=="NAME") 
        {
			if($inWpt)
            	$inWptName=true;
			elseif($inTrk)
				$inTrkName=true;
        }
        elseif($element=="DESC") 
        {
			if($inWpt)
            	$inWptDesc=true;
			elseif($inTrk)
				$inTrkDesc=true;
        }
    }
}

function on_end_element_gpx($parser,$element)
{
    global $inDoc, $inTrk, $inTrkpt, $trackpoints, $waypoints, $curWpt, $inWpt,
            $inWptName, $inTime, $curPt, $inWptDesc, $inTrkName, $inTrkDesc;

    if($element=="TRKPT")
    {
        $inTrkpt=false;
		$trackpoints[] = $curPt;
    }
    elseif($inTrk && $element=="TRK")
    {
        $inTrk = false;
    }
    elseif($element=="TIME" && $inTrkpt)
    {
        $inTime=false;
    }
    elseif($inWpt && $element=="WPT")
    {
        $inWpt = false;
        $waypoints[] = $curWpt;
    }
    elseif($element=="NAME")
	{
		if($inWptName)
        	$inWptName = false;
		elseif($inTrkName)
        	$inTrkName = false;
	}
    elseif($element=="DESC")
	{
		if($inWptDesc)
        	$inWptDesc = false;
		elseif($inTrkDesc)
        	$inTrkDesc = false;
	}
    elseif($inDoc && $element=="GPX")
        $inDoc = false;
}

function on_characters_gpx($parser, $characters)
{
    global $inWptName, $curWpt, $inTime, $curPt, $inWptDesc,
		$inTrkName, $inTrkDesc, $trkName, $trkDesc;
	
    if($inWptName==true)
        $curWpt['name'] = $characters;
    elseif($inWptDesc==true)
        $curWpt['desc'] = $characters;
    elseif($inTrkName==true)
        $trkName = $characters;
    elseif($inTrkDesc==true)
        $trkDesc = $characters;
	elseif($inTime==true)
	{
		$curPt['time'] = strtotime($characters);
		$t = date('D d M Y, H:i',$curPt['time']);
		//echo "$t<br/>";
	}
}
//parseGPX(file("070410.gpx"));
?>
