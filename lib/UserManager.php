<?php

// Moved the static User methods into here, makes sense to have a class so
// we can access the $conn object and perhaps allow a user-defined users table,
// on the other hand User represents a single, defined user with a particular
// ID.

require_once('User.php');

class UserManager
{
    protected $conn;

    public function __construct($conn)
    {
        $this->conn = $conn;
    }

    function isValidLogin($username,$password)
    {
        if(ctype_alnum($username) && ctype_alnum($password))
        {
		
            $stmt=$this->conn->prepare
                ("select * from users where username=? and password=?");
            $stmt->bindParam (1, $username);
            $stmt->bindParam (2, sha1($password));
            $stmt->execute();
             $row = $stmt->fetch();
			return $row;
        }
        return false;
    }

    function deprecatedIsValidLogin($username,$password)
    {
        if(ctype_alnum($username) && ctype_alnum($password))
        {
		
            $stmt=$this->conn->prepare
                ("select * from users where username=? and password=?");
            $stmt->bindParam (1, $username);
            $stmt->bindParam (2, sha1($password));
            $stmt->execute();
             $row = $stmt->fetch();
			return $row;
        }
        return false;
    }
    function getUserFromUsername($user)
    {
        if(ctype_alnum($user))
        {
            $stmt=$this->conn->prepare
            ("SELECT id FROM users WHERE username=?");
            $stmt->bindParam (1, $user);
            $stmt->execute();
            $row = $stmt->fetch();
            if($row!==false)
            {
                return new User($row["id"], $this->conn);
            }
        }
        return null;
    }
}
?>
