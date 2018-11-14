<?php
    header("Access-Control-Allow-Origin: *");
//    $json = file_get_contents("php://input");
 //   $data = json_decode($json);
    $modified=0;
    if(isset($_POST["id"]) && ctype_digit($_POST["id"])) {

        if(isset($_POST["designation"]) && preg_match("/^[\w_]+$/", $_POST["designation"]) && $_POST["designation"]!="none") {
            $conn = new PDO("pgsql:host=localhost;dbname=gis2;", "gis");
            $stmt = $conn->prepare("UPDATE planet_osm_line SET designation=? WHERE osm_id=?");
            $stmt->bindParam (1, $_POST["designation"]);
            $stmt->bindParam (2, $_POST["id"]);
            $stmt->execute();
            $modified = $stmt->rowCount();
            
            if($modified == 1) {
                $stmt = $conn->prepare("INSERT INTO localDesignations(osm_id, designation) VALUES (?, ?)");
                $stmt->bindParam (1, $_POST["id"]);
                $stmt->bindParam (2, $_POST["designation"]);
                $stmt->execute();
               	 
            }
        }
    }
    echo $modified;
?>
