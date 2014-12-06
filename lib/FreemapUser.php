<?php

require_once("User.php");
class FreemapUser extends User
{
    function __construct($id, $conn, $table="users")
    {
        parent::__construct ($id, $conn, $table);
    }

    function activate($key)
    {
        if($this->valid)
        {
            $stmt = $this->conn->prepare
                ("SELECT * FROM {$this->table} WHERE id=? AND active=0");
            $stmt->bindParam (1, $this->id);
            $stmt->execute();
            $row = $stmt->fetch();
            if($row)
            {
                if($row['k']==$key)
                {
                    $stmt = $this->conn->prepare
                        ("UPDATE {$this->table} SET active=1,k=0 WHERE id=?");
                    $stmt->bindParam (1, $this->id);
                    $stmt->execute();
                    return true;
                }
                else
                {
                    $stmt = $this->conn->prepare
                        ("DELETE FROM {$this->table} WHERE id=?");
                    $stmt->bindParam (1, $this->id);
                    $stmt->execute();
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        return false;
    }
}
?>
