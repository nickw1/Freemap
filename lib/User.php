<?php

require_once('DAO.php');

class User extends DAO {

    function __construct($conn, $table=null) {
        parent::__construct($conn, $table);
    }

    function isAdmin() {
        return $this->isValid() && $this->row["isadmin"]==1;
    }

    function findUserByLogin($username,$password) {
		$stmt=$this->conn->prepare
			("select * from ".$this->table." where username=?");
		$stmt->bindParam (1, $username);
		$stmt->execute();
		$row = $stmt->fetch();

		$this->setRow(  ($row===false) ?  false:
                    (password_verify($password, $row["password"]) ||
                        $row["active"]==-1) ? $row: false);

		
    }

    // 251114 this appears not to be used anywhere - so deleted
    //function deprecatedIsValidLogin($username,$password)

    function findUserByUsername($user) {
        if(ctype_alnum($user)) {
            $stmt=$this->conn->prepare
            ("SELECT id FROM ".$this->table." WHERE username=?");
            $stmt->bindParam (1, $user);
            $stmt->execute();
            $row = $stmt->fetch();
			$this->setRow($row);
        }
    }

    function findUserFromCredentials($userSession=null) {
        $userid=0;
        if(isset($_SERVER['PHP_AUTH_USER']) &&
                isset($_SERVER['PHP_AUTH_PW'])) {
            $this->findUserByLogin
                        ($_SERVER['PHP_AUTH_USER'],
                        $_SERVER['PHP_AUTH_PW']);
		} elseif($userSession!=null && isset($_SESSION[$userSession])) {
            $this->findUserByUsername($_SESSION[$userSession]);
        }
    }    

    function processSignup($username,$password) {
        $stmt=$this->conn->prepare("SELECT * FROM ".$this->table.    
                " WHERE username=?");
        $stmt->bindParam (1, $username);
        if(!ctype_alnum($username))
            return 4;
        $stmt->execute();
        if($row = $stmt->fetch()) {
            return 1;    
        } elseif(strstr($username," ")) {
            return 2;    
        } elseif($username=="" || $password=="") {
            return 3;    
        } else {
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
