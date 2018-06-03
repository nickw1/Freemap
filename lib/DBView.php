<?php

require_once('View.php');

abstract class DBView extends View {
    protected $conn;
    abstract function outputRecord($row);
    abstract function outputAllRecords($rows);

    // ??? is this the best way?
    function setConn($conn) {
        $this->conn = $conn;
    }
}

?>
