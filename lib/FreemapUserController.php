<?php

require_once('UserController.php');
require_once('FreemapUserManager.php');
require_once('FreemapUser.php');
require_once('functionsnew.php');

class FreemapUserController extends UserController
{

    function __construct($view, $conn, $userSession, $adminSession)
    {
        parent::__construct($view,$conn,$userSession,$adminSession); 
    }


    function actionSignup($httpData)
    {
        $this->view->head();
        if(isset($httpData["username"]) && isset($httpData["password"]))
        {
        $um = new FreemapUserManager($this->conn);
            $res=$um->processFreemapSignup
                ($httpData['username'],$httpData['password'],
                $httpData['email']);
            if(is_int($res))
                $this->view->displaySignupForm($res);
            else
                $this->view->displaySignupConfirmation();
        }
        else
        {
            $this->view->displaySignupForm();
        }
        $this->view->closePage();
    }

    function actionDelete($httpData)
    {
        if(!isset($httpData['id']))
        {
            $this->view->outputBasicPage("Error", "Please specify a user ID.");
        }
        elseif(!isset($_SESSION[$this->userSession]))
        {
            $this->view->redirectMsg("Please login",
                                "user.php?action=login&redirect=/index.php");
        }
        else if ($_SESSION[$this->adminSession]!=1)
        {
            $this->view->outputBasicPage
                ("Error", "Only the administrator can delete accounts.");
        }
        else 
        {
            $u=new User($httpData["id"], $this->conn);
            if($u->isValid())
            {
                $status = $u->remove();
                $msg = $status ? "Deleted successfully":
                            "No user with that ID!";
                $heading = $status ? "Deleted": "Error";
                $this->view->outputBasicPage($heading, $msg);
            }
            else
            {
                $this->view->outputBasicPage("Error", "Invalid user ID.");
            }
        }
    }

    function actionActivate($httpData)
    {
        if(!isset($httpData['id']) || !isset($httpData['key']))
        {
            $this->view->redirectMsg("id/key not supplied.","/index.php");
        }
        else
        {
            $u=new FreemapUser($httpData['id'], $this->conn);
            if($u->isValid())
            {
                if($u->activate($httpData['key']))
                    $this->view->redirectMsg("User activated.","/index.php");
                else
                    $this->view->redirectMsg("Already activated","/index.php");
            }
            else
            {
                $this->view->redirectMsg("Invalid user ID.","/index.php");
            }
        }
    }

    function actionLogout($httpData)
    {
        session_start();
        session_destroy();
        if(!isset($httpData['redirect']))
            $httpData['redirect'] = 'index.php';
        header("Location: $httpData[redirect]");

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
        $data = clean_input ($rawHTTPdata, null); 
        
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
