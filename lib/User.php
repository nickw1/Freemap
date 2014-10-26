<?php

class User
{
    protected $id,$valid, $conn;

    function __construct($id, $conn, $table="users")
    {
        $this->id=(ctype_digit($id) ? $id: 0);
        $this->conn = $conn;
        $this->table = ctype_alnum($table) ? $table: "users";
        $stmt=$this->conn->prepare
        ("SELECT COUNT(*) AS count FROM {$this->table} WHERE id=?");
        $stmt->bindParam (1, $this->id);
        $stmt->execute();
        $this->valid=$stmt->fetch() != false;
    }

    function isValid()
    {
        return $this->valid;
    }

    function remove()
    {
        $stmt=$this->conn->prepare("SELECT * FROM {$this->table} WHERE id=?");
        $stmt->bindParam (1, $this->id);
        $stmt->execute();
        if($row=$stmt->fetch())
        {
            $stmt2=$this->conn->prepare
                 ("DELETE FROM {$this->table} WHERE id=?");
            $stmt2->bindParam (1, $id);
            $stmt2->execute();
            return true;
        }
        else
        {
            return false;
        }
    }

    function getID()
    {
        return $this->id;
    }

    function isAdmin()
    {
        $stmt=$this->conn->prepare
            ("SELECT isadmin FROM {$this->table} WHERE id=?");
        $stmt->bindParam (1, $this->id);
        $stmt->execute();
        $row=$stmt->fetch();
        return $row["isadmin"]==1;
    }
} 
?>
