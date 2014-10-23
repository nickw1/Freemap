<?php

class User
{
    protected $id,$valid, $conn;

    function __construct($id, $conn, $table="users")
    {
        $this->id=$id;
        $this->conn = $conn;
        $this->table = $table;
        $result=$this->conn->query
        ("SELECT COUNT(*) AS count FROM {$this->table} WHERE id=$id");
        $this->valid=$result->fetch() != false;
    }

    function isValid()
    {
        return $this->valid;
    }


    function remove()
    {
        $result=$this->conn->query("SELECT * FROM {$this->table} WHERE id=".
                                    $this->id);
        if($row=$result->fetch())
        {
            $this->conn->query
                 ("DELETE FROM {$this->table} WHERE id=".$this->id);
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
        $result=$this->conn->query
            ("SELECT isadmin FROM {$this->table} WHERE id=".$this->id);
        $row=$result->fetch();
        return $row["isadmin"]==1;
    }
} 
?>
