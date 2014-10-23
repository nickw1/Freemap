<?php

require_once('UserController.php');
require_once('FreemapUserManager.php');

class FreemapUserController extends UserController
{

    function __construct($view, $conn, $userSession, $adminSession)
    {
		parent::__construct($view,$conn,$userSession,$adminSession); 
    }


    function actionSignup($cleaned)
    {
        $this->view->head();
        if(isset($cleaned["username"]) && isset($cleaned["password"]))
        {
	    $um = new FreemapUserManager($this->conn);
            $res=$um->processSignup
                ($cleaned['username'],$cleaned['password'],$cleaned['email']);
            if(is_int($res))
                $this->view->displaySignupForm($res);
            else
                $this->view->displaySignupConfirmation();
	    echo "done";
        }
        else
        {
            $this->view->displaySignupForm();
        }
        $this->view->closePage();
    }

    function actionDelete($cleaned)
    {
        if(!isset($cleaned['id']))
        {
            $this->view->redirectMsg("Please specify a user ID.","index.php");
        }
        elseif(!isset($_SESSION[$this->userSession]))
        {
            $this->view->redirectMsg("Please login","index.php");
        }
        else if ($_SESSION[$this->adminSession]!=1)
        {
            $this->view->redirectMsg
                ("Only the administrator can delete accounts.","index.php");
        }
        else 
        {
            $u=new User($cleaned["id"], $this->conn);
            if($u->isValid())
            {
                $u->remove();    
            }
            else
            {
                $this->view->redirectMsg("Invalid user ID.","index.php");
            }
        }
    }

    function actionActivate($cleaned)
    {
        if(!isset($cleaned['id']) || !isset($cleaned['key']))
        {
            $this->view->redirectMsg("id/key not supplied.","index.php");
        }
        else
        {
            $u=new User($cleaned['id'], $this->conn);
            if($u->isValid())
            {
                if($u->activate($cleaned['key']))
                    $this->view->redirectMsg("User activated.","index.php");
                else
                    $this->view->redirectMsg("Already activated","index.php");
            }
            else
            {
                $this->view->redirectMsg("Invalid user ID.","index.php");
            }
        }
    }

    function actionLogout($cleaned)
    {
        session_start();
        session_destroy();
        if(!isset($cleaned['redirect']))
            $cleaned['redirect'] = 'index.php';
        header("Location: $cleaned[redirect]");

    }

    function actionViewAll()
    {
        if(isset($_SESSION[$this->userSession]) && 
			$_SESSION[$this->adminSession]==1)
        {
            $this->view->head();
            $this->view->displayAll();
            $this->view->closePage();
        }
        else
        {
            $this->view->redirectMsg
                ("Only administrators can view all users.","index.php");
        }
    }

    function execute($rawHTTPdata)
    {
        $data = clean_input($rawHTTPdata, 'pgsql');
        
        switch ($data["action"])
        {
            case "login":
                $this->actionLogin($data);
                break;

            case "logout":
                $this->actionLogout($data);
                break;

            case "signup":
                $this->actionSignup($data);
                break;

            case "delete":
                $this->actionDelete($data);
                break;
            
            case "activate":
                $this->actionActivate($data);
                break;

            case "viewAll":
                $this->actionViewAll();
                break;
        }
    }
}

?>
