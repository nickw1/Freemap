<?php

require_once('View.php');

class DefaultHTMLView extends View {

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
					echo "<input type='text' name='$col[name]' ";
					if($row!=null) {
						echo "value='{$row[$col["name"]]}'";
					}
					echo "/><br />\n";
				}
			} elseif ($row) {
				echo "<input name='id' type='hidden' value='$row[id]' />\n";
			}
		}
		echo "<input type='submit' value='Go!' />\n";
	}
}

?>
