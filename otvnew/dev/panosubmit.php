<?php

require_once('../../lib/functionsnew.php');
require_once('../../common/defines.php');
require_once('Photosphere.php');

session_start();

$file=$_FILES["file1"]["tmp_name"];
$imageData = getimagesize($file);

if(!isset($_SESSION["gatekeeper"]))
{
    header("HTTP/1.1 401 Unauthorized");
    echo "ERROR: must be logged in.";
}
elseif($imageData===false || $imageData[2]!=IMAGETYPE_JPEG)
    echo "ERROR: Not a JPEG image!";
else
{
    $photosphere = new Photosphere($file);
    if($photosphere->hasGPano()===false)
    {
        echo "ERROR: This does not look like a photosphere.";
    }
    elseif($photosphere->getLatitude()===false ||
        $photosphere->getLongitude()===false)
    {
        echo "ERROR: Currently, photospheres must have latitude and ".
            "longitude information. Ensure GPS turned on when you take it.";
    }
    else
    {
        $conn=pg_connect("dbname=gis user=gis");
        $id = pg_next_id("panoramas"); 
        $result = upload_file("file1", OTV_UPLOADS, $id.".jpg");
        if($result["error"]!==null)
            echo "ERROR: $result[error]";
        else
        {
            $lat=$photosphere->getLatitude();
            $lon=$photosphere->getLongitude();
            pg_query
                ("INSERT INTO panoramas (authorised,userid,xy) ".
                " VALUES (0,".
				get_user_id($_SESSION["gatekeeper"],"users","username",
							"id","pgsql").",".
                "GeomFromText('POINT($lon $lat)',4326))");
            echo "Successful upload";
        }
    }
}

?>

