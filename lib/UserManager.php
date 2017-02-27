<?php

// Moved the static User methods into here, makes sense to have a class so
// we can access the $conn object and perhaps allow a user-defined users table,
// on the other hand User represents a single, defined user with a particular
// ID.

require_once('User.php');
require_once('password.php');

class UserManager
{
    protected $conn, $table;

    public function __construct($conn, $table="users")
    {
        $this->conn = $conn;
        $this->table = $table;
    }

    function isValidLogin($username,$password)
    {
            $stmt=$this->conn->prepare
                ("select * from ".$this->table." where username=?");
                echo ("select * from ".$this->table." where username=$username");
            $stmt->bindParam (1, $username);
            $stmt->execute();
            $row = $stmt->fetch();

            return ($row===false) ?  false:
                    (password_verify($password, $row["password"]) ||
                        $row["active"]==-1) ? $row: false;
    }

    // 251114 this appears not to be used anywhere - so deleted
    //function deprecatedIsValidLogin($username,$password)

    function getUserFromUsername($user)
    {
        if(ctype_alnum($user))
        {
            $stmt=$this->conn->prepare
            ("SELECT id FROM ".$this->table." WHERE username=?");
            $stmt->bindParam (1, $user);
            $stmt->execute();
            $row = $stmt->fetch();
            if($row!==false)
            {
                $u = new User($this->conn, $this->table);
				$u->setRow($row);
				return $u;
            }
        }
        return null;
    }

    function getUserIdFromCredentials()
    {
        $userid=0;
        if(isset($_SERVER['PHP_AUTH_USER']) &&
                isset($_SERVER['PHP_AUTH_PW']))
        {
            $userid=-1;
            if(($row=$this->isValidLogin
                        ($_SERVER['PHP_AUTH_USER'],
                        $_SERVER['PHP_AUTH_PW']))!==false)
            {
                $userid=$row["id"];
            }
        }        
        elseif(isset($_SESSION["gatekeeper"]))
        {
            $userid=$this->getUserFromUsername($_SESSION['gatekeeper'])->
                getID();
            $userid = ($userid>0) ? $userid: -1;
        }
        return $userid;
    }    

    function processSignup($username,$password)
    {
        $stmt=$this->conn->prepare("SELECT * FROM ".$this->table.    
                " WHERE username=?");
        $stmt->bindParam (1, $username);
        if(!ctype_alnum($username))
            return 4;
        $stmt->execute();
        if($row = $stmt->fetch())
        {
            return 1;    
        }
        elseif(strstr($username," "))
        {
            return 2;    
        }
        elseif($username=="" || $password=="")
        {
            return 3;    
        }
        else
        {
            $stmt = $this->conn->prepare ("insert into ".$this->table." (".
                    "username,password)".
                    "values (?,?)");
            $stmt->bindParam (1, $username);
            $hash=password_hash($password, PASSWORD_BCRYPT);
            $stmt->bindParam (2, $hash);
            $stmt->execute();
        }
    }
}
?>
