<?php
require_once('../../lib/functionsnew.php');

$conn=pg_connect("dbname=gis user=gis");
$cleaned = clean_input($_REQUEST,'pgsql');

$goog = ll_to_sphmerc($cleaned['lon'],$cleaned['lat']);

switch($_REQUEST['action'])
{
    case 'create':
	default:
        $q= "INSERT INTO annotations(text,xy,dir) ".
                "VALUES ('$cleaned[text]',".
                "PointFromText('POINT ($goog[e] $goog[n])',900913)".
                ",0)";
        pg_query($q);
        $result=pg_query("SELECT currval('annotations_id_seq') AS lastid");
        $row=pg_fetch_array($result,null,PGSQL_ASSOC);
        echo $row['lastid'];
        break;
}

?>
