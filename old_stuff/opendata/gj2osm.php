<?php

$data = json_decode(file_get_contents($argv[1]), true);


$nodeid=isset($argv[3]) ? $argv[3] : -1;
$wayid= isset($argv[4]) ? $argv[4] : -1;
$ways = array();
$whenever ='1970-01-01T00:00:01Z';


echo "<osm version='0.6' generator='gj2osm.php'>\n";
foreach($data["features"] as $row)
{
	$way = array();
	$waynodes = array();
	list(,$parish,$no)=explode("|",$row["properties"]["Name"]);
	$way["tags"]["parish"]=ucwords(strtolower(str_replace("&","and",$parish)));
	$way["tags"]["routeno"]=(int)$no;
	$way["tags"]["parish_row"] = $way["tags"]["parish"].
		" ".$way["tags"]["routeno"];
	$way["tags"]["row_type"] = $argv[2];
	foreach($row["geometry"]["coordinates"] as $point)
	{
		$lon = $point[0];
		$lat = $point[1];
		$id=$nodeid--;
		echo "<node id='$id' timestamp='$whenever' ".
			"version='1' lat='$lat' lon='$lon' />\n";
		$waynodes[] = $id;
	}
	$way["nds"] = $waynodes;
	$ways[$wayid--] = $way;
}

foreach($ways as $id=>$way)
{
	echo "<way version='1' timestamp='$whenever' id='$id'>\n";
	foreach($way["nds"] as $nd)
		echo "<nd ref='$nd' />\n";
	foreach($way["tags"] as $k=>$v)
		echo "<tag k='$k' v='$v' />\n";
	echo "</way>\n";
}

echo "</osm>\n";
?>
