<?php
require_once('../../lib/functionsnew.php');
require_once('../../lib/User.php');
require_once('../../lib/UserManager.php');

session_start();

$inProj = isset($_POST['inProj']) && ctype_alnum($_POST['inProj'])
	 ? $_POST['inProj']:'4326';
adjustProj($inProj);

$expected = array ("create" => array("lon","lat","text"),
					"createMulti"=>array("data"),
                    "delete" => array("id"),
                    "deleteMulti" => array("ids"),
                    "move" =>array("id","lat","lon"));

$userid=0; // 0=not supplied; -1=incorrect

$conn = new PDO ("pgsql:host=localhost;dbname=gis;", "gis");
$um = new UserManager($conn);
$action = isset($_POST["action"]) && ctype_alpha($_POST["action"]) ?
			$_POST["action"] : "";

if(!isset($expected[$action]))
{
    header("HTTP/1.1 400 Bad Request");
    exit;
}
else
{
    foreach($expected[$action] as $field)
    if(!isset($_POST[$field]))
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

switch($_POST['action'])
{
    case 'create':
		if(!preg_match("/^-?[\d\.]+$/", $_POST["lat"]) ||
		   !preg_match("/^-?[\d\.]+$/", $_POST["lon"]))
		{
			header("HTTP/1.1 400 Bad Request");
			echo "Invalid format for lat/lon";
		}
		elseif($userid>=0)
		{
			list($goog['e'],$goog['n']) = 
				reproject($_POST['lon'],$_POST['lat'],$inProj,'900913');
            $q= "INSERT INTO annotations(text,xy,dir,userid,authorised) ".
                "VALUES (?,".
                "PointFromText('POINT ($goog[e] $goog[n])',900913)".
                ",0,$userid,".($userid==0 ? 0:1).")";
			$stmt = $conn->prepare($q);
			$stmt->bindParam (1, $_POST['text']);
			// Note you can't use prepared statements for params to the 
			// POINT in the call to the PointFromText function as it seems to 
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
			$data = @simplexml_load_string(stripslashes($_POST['data']));
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
								  $inProj,'900913');
						// HERE 1
						$stmt = $conn->prepare
						("INSERT INTO annotations(text,xy,dir,userid,".
						"authorised) VALUES (?,".
						"PointFromText('POINT($goog[e] $goog[n])',900913),".
						"0,$userid,".($userid==0 ? 0:1).")");
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
			if(ctype_digit($_GET["id"]))
			{
				$stmt=$conn->prepare("DELETE FROM annotations WHERE id=?");
				$stmt->bindParam (1, $_GET["id"]);
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
			$ids = json_decode($_POST["ids"], true);
			foreach($ids as $id)
			{
				if(ctype_digit($id))
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
			if(ctype_digit($_POST["id"]) &&
				preg_match("/^-?[\d\.]+$/", $_POST["lon"]) &&
				preg_match("/^-?[\d\.]+$/", $_POST["lat"]))
			{
				$goog = ll_to_sphmerc($_POST['lon'],$_POST['lat']);
				$stmt=$conn->prepare
                		("UPDATE annotations SET xy=".
                    	"PointFromText('POINT($goog[e] $goog[n])',900913) ".
                    	"WHERE id=?");
				$stmt->bindParam (1, $_POST["id"]);
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
