<?php

require_once("UserManager.php");

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
        $stmt->bindParam (2, sha1($password));
        $stmt->execute();
        return $stmt->fetch();
    }

    function processSignup($username,$password,$email)
    {
        $stmt=$this->conn->prepare("SELECT * FROM users WHERE username=?");
        $stmt->bindParam (1, $username);
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
            $active=1; // CHANGE!!!
            $stmt = $conn->prepare ("insert into users (email,".
                    "username,password,active,k) ".
                    "values (?,?,?,?,?)");
            $stmt->bindParam (1,$email);
            $stmt->bindParam (2, $username);
            $stmt->bindParam (3, sha1($password));
            $stmt->bindParam (4, $active);
            $stmt->bindParam (5, $random);
            $stmt->execute();

          // doesn't work in postgresql...  annoying
            //$lastid=$this->conn->lastInsertId();
             $result=$this->conn->query("SELECT currval('users_id_seq') AS id");
            $row=$result->fetch();
            $lastid = $row["id"]; 

            mail('nick_whitelegg@yahoo.co.uk',
                    'New Freemap account created', 
                    "New Freemap account created for $username ".
                    "(email $email). ".
                    "<a href=\"". FREEMAP_ROOT.
                    "/0.6/user.php?action=".
                        "delete&id=$lastid\">Delete</a>");
            mail($email, 'New Freemap account created', 
                    "New Freemap account created for $username.".
                    "Please activate by visiting this address: ".
                     FREEMAP_ROOT.
                    "/0.6/user.php?action=activate&id=$lastid".
                "&key=$random");
            return new User($lastid, $this->conn);
        }
    }
}
?>
