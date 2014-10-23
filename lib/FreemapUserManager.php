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
        $q="select * from users where username='$username' ".
              "and password='".sha1($password)."' and active=1";
        $result=$this->conn->query($q);
        return $result->fetch();
    }

    function processSignup($username,$password,$email)
    {
        $result=$this->conn->query
        ("SELECT * FROM users WHERE username='$username'"); 
        if($row = $result->fetch())
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
            $q = ("insert into users (email,".
                    "username,password,active,k) ".
                    "values ('$email',".
                    "'$username','".
                    sha1($password).
                    "',$active,$random)");
            $this->conn->query($q); 

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
