<?php

abstract class View {
	protected $conn;
	abstract function outputRecord($row);
	abstract function outputAllRecords($rows);
	// ??? is this the best way?
	// might need a db conn so a view can access other models
	function setConn($conn) {
		$this->conn = $conn;
	}
}

?>
