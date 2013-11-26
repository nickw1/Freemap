<?php

require_once('Panorama.php');
require_once('../lib/User.php');

class PanoController
{
    private $view;

    function __construct ($view)
    {
        $this->view = $view;
    }

    function actionGetNearest($input)
    {
        $nearest = Panorama::getNearest($input["lon"], $input["lat"]);
        $this->view->outputString($nearest->getId());
        return 200;
    }

    function actionAuthorise($input)
    {
        if(self::isAdmin())
        {
            $panorama = new Panorama ($input["id"]);
            $panorama->authorise();
            $this->view->redirectMsg
                ("Authorised", "pano.php?action=getUnmoderated");
            return 200;
        }
        return 401;
    }

    function actionGetUnmoderated()
    {
        if(self::isAdmin())
        {
            $panos =  Panorama::getUnmoderated();
            $this->view->displayUnmoderated($panos);
            return 200;
        }
        return 401;
    }

    function actionShow($input)
    {
        if(self::isAdmin())
        {
            $panorama = new Panorama($input["id"]);
            $panorama->show (400, 200);
            return 200;
        }
        return 401;
    }

    function actionDelete($input)
    {
        if(self::isAdmin())
        {
            $panorama = new Panorama($input["id"]);
            $panorama->del();
            $this->view->redirectMsg
                ("Deleted", "pano.php?action=getUnmoderated");
            return 200;
        }
        return 401; 
    }

    function actionGetWithinBbox($input)
    {
        $bbox = explode(",", $input["bbox"]);
        if(count($bbox)==4)
        {
            $numerics=0;
            for($i=0; $i<4; $i++)
            {
                if(is_numeric($bbox[$i]))
                    $numerics++;
            }

            if($numerics==4)
            {
                header("Content-type: application/json");
                $panos = Panorama::getWithinBbox($bbox);
                $this->view->outputPanosAsJSON($panos);
                return 200;
            }
        }
        return 400;
    }

    function action($input)
    {
        $actions = array("getNearest" => array("lon","lat"),
                        "authorise" => array("id"),
                        "show" => array("id"),
                        "getUnmoderated" => false, 
                        "getWithinBbox" => array("bbox"), 
                        "delete" => array("id") );


        if(isset($actions[$input["action"]]) && 
                self::requiredInput($actions, $input, $input["action"]))
        {
            $fname = "action".ucfirst($input["action"]);
            return $this->$fname($input);
        }
        return 400;
    }

    static function isAdmin()
    {
        if(isset($_SESSION["gatekeeper"]))
        {
            $u = User::getUserFromUsername($_SESSION["gatekeeper"]);
            return $u->isAdmin();
        }
        return false;
    }

    static function requiredInput($required, $input, $action)
    {
        if(isset($required[$action]) && $required[$action]!==false)
        {
            foreach($required[$action] as $key)
                if(!isset($input[$key]))
                    return false;
        }
        return true;
    }
}
    

?>
