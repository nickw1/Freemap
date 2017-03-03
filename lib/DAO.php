<?php

class DAO {
    protected $id, $conn,$row, $table;

    function __construct($conn, $table=null) {
        $this->conn = $conn;
        $this->table = $table ? $table: strtolower(static::class)."s";
    }

	// Sets the ID without loading the row
	// e.g. if we want to delete a record
	function setId($id) {
		$this->id = $id;
	}    

    function findById($id) {
		$this->setId($id);
        $stmt=$this->conn->prepare($this->getFindByIdSQL());
        $stmt->bindParam (1, $id);
        $stmt->execute();
        $this->row = $stmt->fetch(PDO::FETCH_ASSOC);
		return $this->row;
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
            array_push($values, $this->id);
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
            $stmt2->bindParam (1, $this->id);
            $stmt2->execute();
			$this->row = false;
            return true;
        } else {
            return false;
        }
    }

    function getID() {
        return $this->id;
    }

    function getRow() {
        return $this->row;
    }

    function setRow($row) {
        $this->row = $row;
		if($row!==false) {
			$this->id = $this->row["id"];
		}
    }

	function getAllRows($orderby=null) {
		$result=$this->conn->query ($this->getRetrieveSQL().
				($orderby==null ? "":" ORDER BY $orderby"));
		return $result->fetchAll(PDO::FETCH_ASSOC);	
	}
    
    function reload() {
        if($this->isValid()) {
            $this->findById($this->id);
        }
    }
	
	function getConn() {
		return $this->conn;
	}

	function getTable() {
		return $this->table;
	}

	function getCols() {
		$coldata = [];
		$result = $this->conn->query($this->getRetrieveSQL());
		for($i=0; $i<$result->columnCount(); $i++) {
			$thisCol = $result->getColumnMeta($i);
			// type is BLOB for text
			$coldata[] = [ "name"=>$thisCol["name"],
							"type"=>$thisCol["native_type"]];
		}
		return $coldata;
	}

	// WHRER 1 to avoid tedious logic of working out whether there's a 
	// WHERE or not, so extra conditions can always use AND
	protected function getRetrieveSQL() {
		return "SELECT * FROM {$this->table} WHERE 1 ";
	}

	protected function getFindByIdSQL() {
        return $this->getRetrieveSQL() ." AND id=?";
	}
} 
?>
