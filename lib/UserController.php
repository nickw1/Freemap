<?php

require_once('User.php');
require_once('UserView.php');
require_once('functionsnew.php');
require_once('UserManager.php');

class UserController
{
    protected $view, $conn, $userSession, $adminSession;

    public function __construct($view, $conn, $userSession, $adminSession)
    {
        $this->view = $view;
        $this->conn = $conn;
    $this->userSession = $userSession;
    $this->adminSession = $adminSession;
    }

    public function login($username,$password,$redirect,$data)
    {
		$qs = "";
		$first=true;
		foreach($data as $k=>$v)
		{
			if($first==false)
				$qs.="&";
			else
				$first=false;
			$qs .= "$k=$v";
		}
        if($this->doLogin($username,$password)==true)
        {
            header("Location: $redirect?$qs");
        }
        else
        {
            $this->view->redirectMsg("Invalid login",
                    basename($_SERVER['PHP_SELF']) . 
                    "?$qs&action=login&redirect=$redirect");
        }
    }
    

    public  function doLogin($username,$password)
    {
        $um = new UserManager($this->conn);
        if($row=$um->isValidLogin($username,$password))
        {
            $_SESSION[$this->userSession] = $row["username"];
            $_SESSION[$this->adminSession] = $row["isadmin"];
            return true;
        }
        return false;
    }

    public function remoteLogin($username,$password)
    {
        if($this->doLogin($username,$password)==true)
        {
            header("Content-type: application/json");
        $um = new UserManager($this->conn);
            $user = $um->getUserFromUsername($_SESSION[$this->userSession]);
            $info = array ($_SESSION[$this->userSession],
                            $user->isAdmin() ? "1":"0");
            echo json_encode($info);
        }
        else
            header("HTTP/1.1 401 Unauthorized");
    }

    public function actionLogin($cleaned)
    {
        if(isset($cleaned["username"]) && isset($cleaned["password"]))
        {
            if(isset($cleaned["remote"]) && $cleaned["remote"])
                $this->remoteLogin 
                    ($cleaned['username'],$cleaned['password']);
            else
            {
				$username = $cleaned["username"];
				$password = $cleaned["password"];
				$redirect = isset($cleaned["redirect"]) ? 
					$cleaned["redirect"]: "index.php";
				unset($cleaned["username"]);
				unset($cleaned["password"]);
				unset($cleaned["redirect"]);
                $this->login($username, $password, $redirect, $cleaned);
            }
        }
        else
        {
	    	unset($cleaned["username"]);
	    	unset($cleaned["password"]);
            $this->view->head();
            $this->view->displayLogin
            (isset($cleaned["redirect"]) ?  
                $cleaned["redirect"] :"index.php", 
                "", $cleaned); 
           $this->view->closePage();
        }
    }

    public function actionLogout($cleaned)
    {
        session_start();
        session_destroy();
        if(!isset($cleaned['redirect']))
            $cleaned['redirect'] = 'index.php';
        header("Location: $cleaned[redirect]");

    }

	// WARNING should NOT be used without some form of validation of the
	// input data!!!
    public function execute($rawHTTPdata)
    {
        
        switch ($rawHTTPdata["action"])
        {
            case "login":
                $this->actionLogin($rawHTTPdata);
                break;

            case "logout":
                $this->actionLogout($rawHTTPdata);
                break;
        }
    }
}

?>
