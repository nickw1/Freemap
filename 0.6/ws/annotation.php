<?php
require_once('../../lib/functionsnew.php');
require_once('../../lib/User.php');
require_once('../../lib/UserManager.php');

session_start();

$conn=pg_connect(pgconnstring());
$cleaned = clean_input($_POST,'pgsql');

$inProj = isset($cleaned['inProj']) ? $cleaned['inProj']:'4326';
adjustProj($inProj);

$expected = array ("create" => array("lon","lat","text"),
					"createMulti"=>array("data"),
                    "delete" => array("id"),
                    "move" =>array("id","lat","lon"));

$userid=0; // 0=not supplied; -1=incorrect

// Bleuurrgh!!! Ghastly beyond words I know to have a PDO object and
// an old-fashioned postgres connection, but while the user stuff has been
// PDO-ised and the rest hasn't we have to make do with this horrible
// quick fix.
$pdo = new PDO ("pgsql:host=localhost;dbname=gis;", "gis");
$um = new UserManager($pdo);

if(!isset($expected[$cleaned['action']]))
{
    header("HTTP/1.1 400 Bad Request");
    exit;
}
else
{
    foreach($expected[$cleaned['action']] as $field)
    if(!isset($cleaned[$field]))
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
    $userid=get_user_id ($_SESSION['gatekeeper'],
                                'users','username','id','pgsql');
	$userid=($userid>0) ? $userid: -1;
}

switch($cleaned['action'])
{
    case 'create':
		if($userid>=0)
		{
			list($goog['e'],$goog['n']) = 
				reproject($cleaned['lon'],$cleaned['lat'],$inProj,'900913');
            $q= "INSERT INTO annotations(text,xy,dir,userid,authorised) ".
                "VALUES ('$cleaned[text]',".
                "PointFromText('POINT ($goog[e] $goog[n])',900913)".
                ",0,$userid,".($userid==0 ? 0:1).")";
            pg_query($q);
            $result=pg_query("SELECT currval('annotations_id_seq') AS lastid");
            $row=pg_fetch_array($result,null,PGSQL_ASSOC);
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
					$desc=pg_escape_string($annotation->description);
					list($goog['e'],$goog['n']) = 
						reproject(pg_escape_string($attrs['x']),
								  pg_escape_string($attrs['y']),
								  $inProj,'900913');
					pg_query 
						("INSERT INTO annotations(text,xy,dir,userid,".
						"authorised) VALUES ('$desc',".
						"PointFromText('POINT($goog[e] $goog[n])',900913),".
						"0,$userid,".($userid==0 ? 0:1).")");
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
            $result=pg_query("SELECT * FROM annotations WHERE id=$cleaned[id]");
            if(pg_numrows($result)==1)
                pg_query("DELETE FROM annotations WHERE id=$cleaned[id]");
            else
                header("HTTP/1.1 404 Not Found");
        }
        else
        {
            header("HTTP/1.1 401 Unauthorized");
        }
        break;

    case "move":
        if($userid>0)
        {
            $result=pg_query("SELECT * FROM annotations WHERE id=$cleaned[id]");
            if(pg_numrows($result)==1)
            {
                $goog = ll_to_sphmerc($cleaned['lon'],$cleaned['lat']);
                pg_query("UPDATE annotations SET xy=".
                    "PointFromText('POINT($goog[e] $goog[n])',900913) ".
                    "WHERE id=$cleaned[id]");
            }
            else
                header("HTTP/1.1 404 Not Found");
        }
        else
        {
            header("HTTP/1.1 401 Unauthorized");
        }
                
        break;
        
}

?>
