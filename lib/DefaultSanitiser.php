<?php
require_once('Sanitiser.php');

class DefaultSanitiser implements Sanitiser {

	public function sanitise($rawData) {
		return array_filter($rawData, 'ctype_alnum');
	}
}
?>
