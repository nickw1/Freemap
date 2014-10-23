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
            $result=$this->conn->query
                ("SELECT * FROM {$this->table} WHERE id=".
            $this->id." AND active=0");
            if($result)
            {
                $row=$result->fetch();
                if($row['k']==$key)
                {
                    $this->conn->query
                        ("UPDATE {$this->table} SET active=1,k=0 WHERE id=".
                        $this->id);
                    return true;
                }
                else
                {
                    $this->conn->query("DELETE FROM {$this->table} WHERE id=".
                        $this->id);
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
