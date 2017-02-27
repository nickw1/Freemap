<?php

class DAO {
    protected $id, $conn,$row, $table;

    function __construct($conn, $table=null) {
        $this->conn = $conn;
        $this->table = $table ? $table: strtolower(static::class)."s";
    }
    
    function findById($id) {
        $stmt=$this->conn->prepare
        ("SELECT * FROM {$this->table} WHERE id=?");
        $stmt->bindParam (1, $id);
        $stmt->execute();
        $this->row = $stmt->fetch(PDO::FETCH_ASSOC);
    }

    function create($data) {
        $keys=array_keys($data);
        $sql = "INSERT INTO {$this->table} (".
                  implode(",",$keys) .") VALUES (";    
        
        $first=true;
        foreach($data as $k=>$v) {
            if(!$first) {
                $sql.=",?";
            } else {
                $first=false;
                $sql.="?";
            }    
        }
        $sql.=")";
        $stmt=$this->conn->prepare($sql);
        for($i=0; $i<count($keys); $i++) {
            $stmt->bindParam($i+1,$data[$keys[$i]]);
        }
        $stmt->execute();
        $this->findById($this->conn->lastInsertId());
    }

    function update($data) {
        if($this->isValid()) {
            $sql = "UPDATE {$this->table} SET "; 
            $first=true;
            foreach($data as $k=>$v) {
                if(!$first) {
                    $sql.=",";
                } else {
                    $first=false;
                }
                $sql.="$k=?";
            }    
            $sql .= " WHERE id=?";
            $stmt=$this->conn->prepare($sql);
            $values = array_values($data);
            array_push($values, $this->row["id"]);
            $stmt->execute($values);
            return $stmt->rowCount()==1 ? true:false;
        }
        return false;
    }

    function isValid() {
        return $this->row !== false;
    }

    function remove() {
        if($this->isValid()) {
            $stmt2=$this->conn->prepare
                 ("DELETE FROM {$this->table} WHERE id=?");
            $stmt2->bindParam (1, $this->row["id"]);
            $stmt2->execute();
			$this->row = false;
            return true;
        } else {
            return false;
        }
    }

    function getID() {
        return $this->isValid() ? $this->row["id"]: 0;
    }

    function getRow() {
        return $this->row;
    }

    function setRow($row) {
        $this->row = $row;
    }
    
    function reload() {
        if($this->isValid()) {
            $this->findById($this->row["id"]);
        }
    }
} 
?>
