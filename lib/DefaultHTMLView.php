<?php

require_once('DBView.php');

class DefaultHTMLView extends DBView {

    function outputRecord($row) {
        foreach($row as $k=>$v) {
            echo "<strong>".ucfirst($k)."</strong>: $v<br />";
        }
    }

    function outputAllRecords($rows) {
        foreach($rows as $row) {
            echo "<p>";
            $this->outputRecord($row);
            echo "</p>";
        }
    }

    function generateForm($coldata, $row=null) {
        foreach($coldata as $col) {
            if($col["name"]!="id") {
                echo ucfirst($col["name"])."<br />";
                if($col["type"]=="BLOB") {
                    echo "<textarea name='$col[name]'>";
                    if($row!=null) {
                        echo $row[$col["name"]];
                    }
                    echo "</textarea> <br />\n";
                } else {
		    $type = $col["name"]=="password"?"password":"text";
                    echo "<input type='$type' name='$col[name]' ";
                    if($row!=null) {
                        echo "value='{$row[$col["name"]]}'";
                    }
                    echo "/><br />\n";
                }
            } elseif ($col["name"]=="id") {
                echo "<input name='id' type='hidden' value='$row[id]' />\n";
            } 
        }
        echo "<input type='submit' value='Go!' />\n";
    }

    function outputMsg($msg) {
        echo "<p class='msg'>$msg</p>";
    }
	
	function outputHeading($level, $text) {
		if(is_numeric($level) && $level>=1 && $level<=6) {
			echo "<h{$level}>$text</h${level}>\n";
		}
	}

	function outputHTML($html) {
		echo $html;
	}

	function startList() {
		echo "<ul>";
	}

	function addListItem($item) {
		echo "<li>$item</li>";
	}

	function endList() {
		echo "</ul>";
	}
}

?>
