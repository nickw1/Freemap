<?php
require_once('../../lib/functionsnew.php');
require_once('../User.php');

session_start();

$conn=pg_connect("dbname=gis user=gis");
$cleaned = clean_input($_POST,'pgsql');

$expected = array ("create" => array("lon","lat","text"),
                    "delete" => array("id"),
                    "move" =>array("id","lat","lon"));
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
    if(($result==User::isValidLogin
                        ($_SERVER['PHP_AUTH_USER'],
                        $_SERVER['PHP_AUTH_PW']))!==null)
    {
        $row=pg_fetch_array($result,null,PGSQL_ASSOC);
        $userid=$row["id"];
    }
}    
elseif(isset($_SESSION["gatekeeper"]))
{
    $userid=get_user_id ($_SESSION['gatekeeper'],
                                'users','username','id','pgsql');
}

switch($cleaned['action'])
{
    case 'create':
        if($userid>0)
        {
            $goog = ll_to_sphmerc($cleaned['lon'],$cleaned['lat']);
            $q= "INSERT INTO annotations(text,xy,dir,userid,authorised) ".
                "VALUES ('$cleaned[text]',".
                "PointFromText('POINT ($goog[e] $goog[n])',900913)".
                ",0,$userid,1)";
            pg_query($q);
            $result=pg_query("SELECT currval('annotations_id_seq') AS lastid");
            $row=pg_fetch_array($result,null,PGSQL_ASSOC);
            echo $row['lastid'];
        }
        else
        {
            $goog = ll_to_sphmerc($cleaned['lon'],$cleaned['lat']);
            $q= "INSERT INTO annotations(text,xy,dir,userid,authorised) ".
                "VALUES ('$cleaned[text]',".
                "PointFromText('POINT ($goog[e] $goog[n])',900913)".
                ",0,$userid,0)";
            pg_query($q);
            $result=pg_query("SELECT currval('annotations_id_seq') AS lastid");
            $row=pg_fetch_array($result,null,PGSQL_ASSOC);
            echo $row['lastid'];
        }
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
