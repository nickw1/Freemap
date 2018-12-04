<?php

include("/var/www/lib/functionsnew.php");

if(count($argv) < 3)
	die("Usage: php $argv[0] file county [rowtype]\n");
$county=$argv[2];
$rowtype=$argv[3] ? $argv[3]:"Footpath";

$file=file_get_contents($argv[1]);
$json=json_decode($file, true);

$conn=pg_connect("dbname=gis user=gis");

foreach($json["features"] as $feature)
{
	if(count($feature["geometry"]["coordinates"]) >= 2)
	{
	list(,$parish,$no)=explode("|",$feature["properties"]["Name"]);
	$parish=ucwords(strtolower($parish));
	$no=(int)$no;
	$desc = $feature["properties"]["Description"];
	/* works in w sussex nowhere else?
	$dd=explode("|",$desc);
	$ee=explode("@",$dd[3]);
	$district=ucwords(strtolower($ee[0]));
	*/
	$district = "unknown";
	$parish_row = $parish." ".$no;
	//echo "Parish $parish routeno $no dist $district parishrow $parish_row\n";
	$geom=mkgeom($feature["geometry"]["coordinates"],0,1,"MULTILINESTRING");
	//echo $geom;
	$sql = "INSERT INTO hampshire(district,parish,row_type,routeno,parish_row,the_geom,county) VALUES ('$district','$parish','$rowtype',$no,'$parish_row',GeomFromText('$geom',3857),'$county')";
	pg_query($sql) or die("SQL error with: $sql ". pg_last_error());
	echo "Inserted $parish_row\n";
	//echo $sql."\n";
	}
}

pg_close($conn);
?>
