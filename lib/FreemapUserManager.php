<?php

require_once("UserManager.php");
require_once("password.php");
require_once("defines.php");

class FreemapUserManager extends UserManager
{
    public function __construct ($conn)
    {
        parent::__construct ($conn);
    }

    function isValidLogin($username,$password)
    {
        $stmt=$this->conn->prepare
        ("select * from users where username=? and password=? and active=1");
        $stmt->bindParam (1, $username);
		$hash=password_hash($password, PASSWORD_BCRYPT);
        $stmt->bindParam (2, $hash);
        $stmt->execute();
        return $stmt->fetch();
    }

    function processFreemapSignup($username,$password,$email)
    {
        $stmt=$this->conn->prepare("SELECT * FROM users WHERE username=?");
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
            $random = rand (1000000,9999999);
            $active=0; 
            $stmt = $this->conn->prepare ("insert into users (".
                    "username,password,email,active,k) ".
                    "values (?,?,?,?,?)");
            $stmt->bindParam (1, $username);
			$hash=password_hash($password, PASSWORD_BCRYPT);
            $stmt->bindParam (2, $hash);
			$stmt->bindParam (3, $email);
            $stmt->bindParam (4, $active);
            $stmt->bindParam (5, $random);
            $stmt->execute();

            $result=$this->conn->query("SELECT currval('users_id_seq') AS id");
            $row=$result->fetch();
            $lastid = $row["id"]; 

            mail('freemapinfo@gmail.com',
                    'New Freemap account created', 
                    "New Freemap account created for $username ".
                    "Delete by following link: ". FREEMAP_ROOT.
                    "/fm/user.php?action=delete&id=$lastid");
            mail($email, 'New Freemap account created', 
                    "New Freemap account created for $username.".
                    "Please activate by visiting this address: ".
                     FREEMAP_ROOT.
                    "/fm/user.php?action=activate&id=$lastid".
                "&key=$random");
			
           $u = new User($this->conn);
		   $u->findById($lastid);
		   return $u;
        }
    }
}
?>
