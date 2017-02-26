<?php
class Controller {
	
    protected $view; 

    public function __construct($view) {
		$this->view = $view;
	}

    // WARNING should NOT be used without some form of validation of the
    // input data!!!
    public function execute($rawHTTPdata) {
        $function = "action".ucfirst($rawHTTPdata["action"]);
           if(method_exists($this, $function)) 
        {
            $this->$function($rawHTTPdata);
        } 
    }
}
?>
