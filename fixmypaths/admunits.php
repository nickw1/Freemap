<?php

header("Content-type: application/json");

require_once('../lib/functionsnew.php');

$cleaned = clean_input($_GET);

$conn=pg_connect("dbname=gis user=gis");

$json=array();

$q="SELECT DISTINCT $cleaned[cat] AS name FROM hampshire ";
$first=true;
$searchtypes = array ("county","district");
foreach($cleaned as $k=>$v)
{
	if(in_array($k,$searchtypes) && $v!="all")
	{
		if($first==true)
		{
			$q .= " WHERE ";	
			$first=false;
		}
		else
			$q .= " AND ";
		$q .= "$k='$v'";
	}
}

$q .=	" ORDER BY $cleaned[cat]";
$result=pg_query($q);
while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
	$json[] = $row["name"]; 

echo json_encode($json);

pg_close($conn);

?>
