<?php


class User
{
    private $id,$valid;

    function __construct($id)
    {
        $this->id=$id;
        $result=pg_query("SELECT COUNT(*) AS count FROM users WHERE id=$id");
        $this->valid=pg_numrows($result)!=0;
    }

    function isValid()
    {
        return $this->valid;
    }

    static function isValidLogin($username,$password)
    {
        $q="select * from users where username='$username' ".
              "and password='".sha1($password)."' and active=1";
        $result=pg_query($q);
        return (pg_numrows($result)==1) ? $result:null;
    }
   



    function remove()
    {
        $result=pg_query("SELECT * FROM users WHERE id=".$this->id);
        if($row=pg_fetch_array($result,null,PGSQL_ASSOC))
        {
            pg_query("DELETE FROM users WHERE id=".$this->id);
			return true;
        }
        else
        {
			return false;
        }
    }


    static function getUserFromUsername($user)
    {
        $result=pg_query("SELECT id FROM users WHERE username='$user'");
        if(pg_numrows($result)==1)
        {
            $row=pg_fetch_array($result,null,PGSQL_ASSOC);
            return new User($row["id"]);
        }
        return null;
    }

    function getID()
    {
        return $this->id;
    }

	function isAdmin()
	{
		$result=pg_query("SELECT isadmin FROM users WHERE id=".$this->id);
		$row=pg_fetch_array($result,null,PGSQL_ASSOC);
		return $row["isadmin"]==1;
	}

    function activate($key)
    {
        if($this->valid)
        {
            $result=pg_query
                ("SELECT * FROM users WHERE id=".$this->id." AND active=0");
            if($result)
            {
                $row=pg_fetch_array($result,null,PGSQL_ASSOC);
                if($row['k']==$key)
                {
                    pg_query("UPDATE users SET active=1,k=0 WHERE id=".
                        $this->id);
                    return true;
                }
                else
                {
                    pg_query("DELETE FROM users WHERE id=".$this->id);
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        return false;
    }

    static function processSignup($username,$password,$email)
    {
        $result=pg_query("SELECT * FROM users WHERE username='$username'"); 
        if(pg_numrows($result)>0)
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
            $q = ("insert into users (email,".
                    "username,password,active,k) ".
                    "values ('$email',".
                    "'$username','".
                    sha1($password).
                    "',0,$random)");
            pg_query($q); 
            $lastid=pg_insert_id("users");
            mail('nick_whitelegg@yahoo.co.uk',
                    'New Freemap account created', 
                    "New Freemap account created for $username ".
                    "(email $email). ".
                    "<a href=".
                    "\"http://www.free-map.org.uk/0.6/user.php?action=".
                        "delete&id=$lastid\">Delete</a>");
            mail($email, 'New Freemap account created', 
                    "New Freemap account created for $username.".
                    "Please activate by visiting this address: ".
                    "http://www.free-map.org.uk".
                    "/0.6/user.php?action=activate&id=$lastid".
                "&key=$random");
            return new User($lastid);
        }
    }
} 
?>
