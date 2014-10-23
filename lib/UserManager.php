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
        $q="select * from users where username='$username' ".
              "and password='".sha1($password)."'";
        $result=$this->conn->query($q);
        return $result->fetch();
    }

    function getUserFromUsername($user)
    {
        $result=$this->conn->query
            ("SELECT id FROM users WHERE username='$user'");
        $row = $result->fetch();
        if($row!==false)
        {
            return new User($row["id"], $this->conn);
        }
        return null;
    }
}
?>
