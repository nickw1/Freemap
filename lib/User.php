<?php

require_once('DAO.php');

class User extends DAO {

    function __construct($id, $conn, $table="users") {
        parent::__construct($id, $conn, $table);
    }

    function isAdmin() {
        return $this->isValid() && $this->row["isadmin"]==1;
    }
} 
?>
