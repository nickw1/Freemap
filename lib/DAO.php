<?php

class DAO {
    protected $id, $conn,$row;

    function __construct($id, $conn, $table) {
        $this->id=(ctype_digit($id) ? $id: 0);
        $this->conn = $conn;
        $this->table = ctype_alnum($table) ? $table: "users";
        $stmt=$this->conn->prepare
        ("SELECT COUNT(*) AS count FROM {$this->table} WHERE id=?");
        $stmt->bindParam (1, $this->id);
        $stmt->execute();
        $this->row = $stmt->fetch();
    }

    function isValid() {
        return $this->row != false;
    }

    function remove() {
        if($this->isValid()) {
            $stmt2=$this->conn->prepare
                 ("DELETE FROM {$this->table} WHERE id=?");
            $stmt2->bindParam (1, $this->id);
            $stmt2->execute();
            return true;
        } else {
            return false;
        }
    }

    function getID() {
        return $this->id;
    }
} 
?>
