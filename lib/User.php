<?php

require_once('DAO.php');

class User extends DAO {

    function __construct($conn, $table=null) {
        parent::__construct($conn, $table);
    }

    function isAdmin() {
        return $this->isValid() && $this->row["isadmin"]==1;
    }
} 
?>
