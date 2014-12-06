<?php

// Moved the static User methods into here, makes sense to have a class so
// we can access the $conn object and perhaps allow a user-defined users table,
// on the other hand User represents a single, defined user with a particular
// ID.

require_once('User.php');
require_once('password.php');

class UserManager
{
    protected $conn;

    public function __construct($conn)
    {
        $this->conn = $conn;
    }

    function isValidLogin($username,$password)
    {
		
            $stmt=$this->conn->prepare
                ("select * from users where username=?");
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
}
?>
