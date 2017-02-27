<?php
require_once('View.php');

class Controller {
	
    protected $view; 

    public function __construct(View $view) {
		$this->view = $view;
	}

    // WARNING should NOT be used without some form of validation of the
    // input data!!!
	// Basic validation of ID (numeric) and action (letters only) 
    public function execute($rawHTTPdata) {
		if(ctype_alpha($rawHTTPdata["action"]) &&
			(!isset($rawHTTPdata["id"]) || is_numeric($rawHTTPdata["id"]))) {
        	$function = "action".ucfirst($rawHTTPdata["action"]);
           	if(method_exists($this, $function)) {
            	$this->$function($rawHTTPdata);
        	} else {
				echo "ERROR: Unknown action $rawHTTPdata[action]";
			} 
		} else {
			echo "ERROR: Action must be letters and/or ID must be numeric.";
		}
    }
}
?>
