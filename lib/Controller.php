<?php
require_once('View.php');

class Controller {
	
    protected $view, $allowedActions; 

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
				echo "ERROR: Unknown action $action";
			} 
		} else {
			echo "ERROR: Disallowed action, ".
				"action must be letters and/or ID must be numeric.";
		}
		return $action;
    }
}
?>
