<?php
require_once('View.php');
require_once('Sanitiser.php');

class Controller {
    
    protected $view, $allowedActions, $sanitiser;

    public function __construct(View $view, $allowedActions=null) {
        $this->view = $view;
        $this->allowedActions = $allowedActions;
    }

    // WARNING should NOT be used without some form of validation of the
    // input data!!!
    // Basic validation of ID (numeric) and action (letters only) 
    public function execute($inputData) {
        $action = isset($inputData["action"]) ? $inputData["action"] :
            strtolower($_SERVER["REQUEST_METHOD"]);
        if($action=="get"||$action=="post" ||
            ($this->allowedActions===null || in_array($action,
                $this->allowedActions)) && 
            ctype_alpha($action) &&
            (!isset($inputData["id"]) || is_numeric($inputData["id"]))) {
            $function = "action".ucfirst($action);
               if(method_exists($this, $function)) {
                $this->$function($inputData);
            } else {
                $this->actionDefault($inputData);
            } 
        } else {
            echo "ERROR: Disallowed action, action must be letters and/or ID, if supplied, must be numeric.";
        }
        return $action;
    }

    public function rawExecute() {
        $httpData  = $_SERVER['REQUEST_METHOD']=='POST' ? $_POST: $_GET;
        if($this->sanitiser) {
            $this->execute($this->sanitiser->sanitise($httpData));
        } else {
            throw new Exception("No sanitiser... refusing to process raw data");
        }
    }

    public function actionDefault($inputData) {
        echo "ERROR: Unknown action";
    }

    public function setSanitiser(Sanitiser $s) {
        $this->sanitiser = $s;
    }
}
?>
