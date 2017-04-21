<?php
require_once('../../lib/functionsnew.php');
require_once('../../lib/User.php');
require_once('../../lib/UserManager.php');

session_start();

$cget = clean_input($_GET, null);
$cpost = clean_input ($_POST, null);

$inProj = isset($cpost['inProj']) && ctype_alnum($cpost['inProj'])
     ? $cpost['inProj']:'4326';
adjustProj($inProj);

$expected = array ("create" => array("lon","lat","text"),
                    "createMulti"=>array("data"),
                    "delete" => array("id"),
                    "deleteMulti" => array("ids"),
                    "move" =>array("id","lat","lon"));

$userid=0; // 0=not supplied; -1=incorrect

$conn = new PDO ("pgsql:host=localhost;dbname=gis2;", "gis");
$um = new UserManager($conn);
$action = isset($cpost["action"]) && ctype_alpha($cpost["action"]) ?
            $cpost["action"] : "";

if(!isset($expected[$action]))
{
    header("HTTP/1.1 400 Bad Request");
    exit;
}
else
{
    foreach($expected[$action] as $field)
    if(!isset($cpost[$field]))
    {
        header("HTTP/1.1 400 Bad Request");
        exit;
    }
}

if(isset($_SERVER['PHP_AUTH_USER']) &&
        isset($_SERVER['PHP_AUTH_PW']))
{
    $row=$um->isValidLogin($_SERVER['PHP_AUTH_USER'],
                                $_SERVER['PHP_AUTH_PW']);
    if($row!==false)
    {
        $userid=$row["id"];
    }
    else
    {
        $userid = -1;
    }
}    
elseif(isset($_SESSION["gatekeeper"]))
{
    $userid=$um->getUserFromUsername($_SESSION['gatekeeper'])->getID();
    $userid=($userid>0) ? $userid: -1;
}

switch($cpost['action'])
{
    case 'create':
        if(!preg_match("/^-?[\d\.]+$/", $cpost["lat"]) ||
           !preg_match("/^-?[\d\.]+$/", $cpost["lon"]))
        {
            header("HTTP/1.1 400 Bad Request");
            echo "Invalid format for lat/lon";
        }
        elseif($userid>=0)
        {
            list($goog['e'],$goog['n']) = 
                reproject($cpost['lon'],$cpost['lat'],$inProj,'3857');
			$annotationType = isset($cpost['annotationType']) &&
				ctype_digit($cpost['annotationType']) ? 
				$cpost['annotationType'] : 1;
            $q= "INSERT INTO annotations(text,xy,dir,userid,annotationType,".
				"authorised) ".
                "VALUES (?,".
                "ST_PointFromText('POINT ($goog[e] $goog[n])',3857)".
                ",0,$userid,$annotationType,".($userid==0 ? 0:1).")";
            $stmt = $conn->prepare($q);
            $stmt->bindParam (1, $cpost['text']);
            // Note you can't use prepared statements for params to the 
            // POINT in the call to the ST_PointFromText function as it seems to 
            // go into an infinite loop
            $stmt->execute();
            $result=$conn->query
                ("SELECT currval('annotations_id_seq') AS lastid");
            $row=$result->fetch(PDO::FETCH_ASSOC);
            echo $row['lastid'];
        }
        else
            header("HTTP/1.1 401 Unauthorized");
        break;

    case 'createMulti':
        if($userid>=0)
        {
            $valid=false;
            $data = @simplexml_load_string(stripslashes($cpost['data']));
            if($data)
            {
                foreach($data->annotation as $annotation)
                {
                    $attrs = $annotation->attributes();
                    if(preg_match("/^-?[\d\.]+$/", $attrs['x']) &&
                        preg_match("/^-?[\d\.]+$/", $attrs['y']))
                    {
                        $desc=$annotation->description;
                        list($goog['e'],$goog['n']) = 
                            reproject($attrs['x'], $attrs['y'],
                                  $inProj,'3857');
						$annotationType = isset($annotation->annotationType) &&
							ctype_digit($annotation->annotationType) ? 
							$annotation->annotationType : 1;
                        // HERE 1
                        $stmt = $conn->prepare
                        ("INSERT INTO annotations(text,xy,dir,userid,".
                        "annotationType,authorised) VALUES (?,".
                        "ST_PointFromText('POINT($goog[e] $goog[n])',3857),".
                        "0,$userid,$annotationType,".($userid==0 ? 0:1).")");
                        $stmt->bindParam (1, $desc);
                        $stmt->execute();
                    }
                }
            }
            else
            {
                header("HTTP/1.1 400 Bad Request");
                echo "Unexpected format";
            }
        }
        else
            header("HTTP/1.1 401 Unauthorized");
        break;
    case "delete":
        if($userid>0)
        {
            if(ctype_digit($cget["id"]))
            {
                $stmt=$conn->prepare
                    ("DELETE FROM annotations WHERE id=$cget[id]");
                $stmt=$conn->prepare("DELETE FROM annotations WHERE id=?");
                $stmt->bindParam (1, $cget["id"]);
                $stmt->execute();
                if($stmt->rowCount()==0)
                    header("HTTP/1.1 404 Not Found");
            }
            else
                header("HTTP/1.1 400 Bad Request");
        }
        else
        {
            header("HTTP/1.1 401 Unauthorized");
        }
        break;

    case "deleteMulti":
        if($userid>0) 
        {
            echo $cpost["ids"]. " ";
            $ids = json_decode($cpost["ids"], true);
            foreach($ids as $id)
            {
                // 20/11/16 if $id is of type int, ctype_digit returns false.
                if(ctype_digit("$id"))
                {
                    $stmt=$conn->prepare("DELETE FROM annotations WHERE id=?");
                    $stmt->bindParam (1, $id);
                    $stmt->execute();
                } 
            }    
        }
        else
        {
            header("HTTP/1.1 401 Unauthorized");
        }
        break;

    case "move":
        if($userid>0) 
        {
            if(ctype_digit($cpost["id"]) &&
                preg_match("/^-?[\d\.]+$/", $cpost["lon"]) &&
                preg_match("/^-?[\d\.]+$/", $cpost["lat"]))
            {
                $goog = ll_to_sphmerc($cpost['lon'],$cpost['lat']);
                $stmt=$conn->prepare
                        ("UPDATE annotations SET xy=".
                        "ST_PointFromText('POINT($goog[e] $goog[n])',3857) ".
                        "WHERE id=?");
                $stmt->bindParam (1, $cpost["id"]);
                $stmt->execute();
                if($stmt->rowCount()==0)
                    header("HTTP/1.1 404 Not Found");
            }
            else
            {
                header("HTTP/1.1 400 Bad Request");
            }
        }
        else
        {
            header("HTTP/1.1 401 Unauthorized");
        }
                
        break;
        
}

?>
