<?php

include('../lib/functionsnew.php');

class PanoView
{
    function outputString($str)
    {
        echo $str;
    }

    function displayUnmoderated($panos)
    {
        ?>
        <!DOCTYPE html>
        <html>
        <head>
        <title>Admin - Moderate Panoramas</title>
        <link rel='stylesheet' type='text/css' href='css/otv.css' />
        <style type='text/css'>
        table, tr, td { border: 1px solid black; }
        </style>
        </head>
        <body>
        <h1 class="admin">Unmoderated Panoramas</h1>
        <table>
        <?php
        foreach($panos as $pano)
        {
            echo "<tr><td>$pano</td>";
            echo "<td><img src='pano.php?action=show&id=$pano' /></td>";
            echo "<td><a href='pano.php?action=authorise&id=$pano'>".
                    "Authorise</a></td>";
            echo "<td><a href='pano.php?action=delete&id=$pano'>".
                    "Delete</a></td>";
            echo "</tr>";
        }
        ?>
        </table>
        <p><a href="index.php">Back to main page</a></p>
        </body></html>
        <?php
    }

    function redirectMsg($msg, $redirect)
    {
        js_msg($msg, $redirect);
    }

    function outputPanosAsJSON($panos)
    {
        $data = array();
        $data["type"] = "FeatureCollection";
        $data["features"] = array();
        foreach($panos as $pano)
        {
            $panoJSON = array();
            $panoJSON["geometry"] = array();
            $panoJSON["geometry"]["type"] = "Point";
            $panoJSON["geometry"]["coordinates"] = array($pano->getLon(),
                                                        $pano->getLat()); 
            $panoJSON["properties"] = array();
            $panoJSON["properties"]["id"] = $pano->getId();

            $panoJSON["type"] = "Feature";

            $data["features"][] = $panoJSON;
        }
        echo json_encode($data);
    }
}
?>
