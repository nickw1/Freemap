<?php

require_once('User.php');
require_once('UserView.php');
require_once('functionsnew.php');

class UserController
{
    private $view;

    function __construct($view)
    {
        $this->view = $view;
    }

    private function login($username,$password,$redirect)
    {
        if(self::doLogin($username,$password)==true)
        {
            if(strpos($redirect,"?")!==false)
            {
                list($redirect,$redirqs) = explode("?", $redirect);
                $qs = $redirqs."&".$qs;
            }
            header("Location: $redirect?$qs");
        }
        else
        {
            $this->view->redirectMsg("Invalid login",
                    basename($_SERVER['PHP_SELF']) . 
                    "?action=login&redirect=$redirect");
        }
    }
    
    private function remoteLogin($username,$password)
    {
        if(self::doLogin($username,$password)==true)
        {
            header("Content-type: application/json");
            $user = User::getUserFromUsername($_SESSION["gatekeeper"]);
            $info = array ($_SESSION["gatekeeper"],
                            $user->isAdmin() ? "1":"0");
            echo json_encode($info);
        }
        else
            header("HTTP/1.1 401 Unauthorized");
    }

    private  function doLogin($username,$password)
    {
        if($result=User::isValidLogin($username,$password))
        {
            $row=pg_fetch_array($result,null,PGSQL_ASSOC);
            $_SESSION["gatekeeper"] = $row["username"];
            $_SESSION["level"] = $row["isadmin"];
            return true;
        }
        return false;
    }

    function actionLogin($cleaned)
    {
        if(isset($cleaned["username"]) && isset($cleaned["password"]))
        {
            if(isset($cleaned["remote"]) && $cleaned["remote"])
                self::remoteLogin 
                    ($cleaned['username'],$cleaned['password']);
            else
            {
                self::login
                    ($cleaned['username'],$cleaned['password'],
                        $cleaned['redirect']);
            }
        }
        else
        {
            $this->view->head();
            $this->view->displayLogin($cleaned["redirect"]);
            $this->view->closePage();
        }
    }

    function actionSignup($cleaned)
    {
        $this->view->head();
        if(isset($cleaned["username"]) && isset($cleaned["password"]))
        {
            $res=User::processSignup
                ($cleaned['username'],$cleaned['password'],$cleaned['email']);
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

    function actionDelete($cleaned)
    {
        if(!isset($cleaned['id']))
        {
            $this->view->redirectMsg("Please specify a user ID.","index.php");
        }
        elseif(!isset($_SESSION['gatekeeper']))
        {
            $this->view->redirectMsg("Please login","index.php");
        }
        else if ($_SESSION['level']!=1)
        {
            $this->view->redirectMsg
                ("Only the administrator can delete accounts.","index.php");
        }
        else 
        {
            $u=new User($cleaned["id"]);
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
            $u=new User($cleaned['id']);
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
        if(isset($_SESSION['gatekeeper']) && $_SESSION['level']==1)
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
