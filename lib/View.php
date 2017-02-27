<?php

abstract class View {

	abstract function outputRecord($row);
	abstract function outputAllRecords($rows);
}

?>
