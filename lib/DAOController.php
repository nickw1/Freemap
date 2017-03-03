<?php

require_once('Controller.php');

class DAOController extends Controller {

	protected $dao;

	public function __construct(View $view, $conn, $table=null, 
			$allowedActions=null) {
		parent::__construct($view, $allowedActions);
		$class = str_replace("Controller","",static::class);
		require_once("{$class}.php");
		$this->dao = new $class($conn, $table); 
		$view->setConn($conn);
	}

	public function actionCreate($inputData) {
		$data = $this->getRelevantData($inputData);
		if(($blanks=$this->checkBlank($data))===[]) {
			$row=$this->dao->create($data);
			return $row["id"];
		} else {
			$this->generateBlankMsg($blanks);
			$this->actionCreateForm($inputData);
			return 0;
		}
	}

	public function actionRetrieve($inputData) {
		$this->dao->findById($inputData["id"]);
		$this->view->outputRecord($this->dao->getRow());
	}

	public function actionRetrieveAll() {
		$this->view->outputAllRecords($this->dao->getAllRows());
	}

	public function actionUpdate($inputData) {
		$this->dao->setId($inputData["id"]);
		$data = $this->getRelevantData($inputData);
		if(($blanks=$this->checkBlank($data))===[]) {
			$this->dao->update($data);
			return true;
		} else {
			$this->generateBlankMsg($blanks);
			$this->actionUpdateForm($inputData);
			return false;
		}
	}

	public function actionDelete($inputData) {
		$this->dao->setId($inputData["id"]);
		$this->dao->remove();
	}

	protected function getRelevantData($inputData) {
		$data = array();
		foreach($inputData as $k=>$v) {
			if($k!="action" && $k!="redirect" && $k!="id" 
				&& !isset($_FILES[$k])) {
				$data[$k] = $v;
			}
		}
		return $data;
	}

	protected function checkBlank($data) {
		$blanks = [];
		foreach($data as $k=>$v) {
			if($v=="") {
				$blanks[] = $k;
			}
		}
		return $blanks;
	}

	protected function generateBlankMsg($blanks) {
		$this->view->outputMsg("The following fields were blank:");
		$this->view->startList();
		foreach($blanks as $blank) {
			$this->view->addListItem($blank);
		}
		$this->view->endList();
	}

	public function actionCreateForm($inputData, $enctype=null) {
		$cols = $this->dao->getCols();
		echo "<form method='post' ";
		if($enctype!==null) {
			echo "enctype='$enctype' ";
		}
		echo "action='$_SERVER[PHP_SELF]'>\n";
		echo "<input type='hidden' name='action' value='create' />";
		$this->view->generateForm($cols);
		echo "</form>";
	}

	public function actionUpdateForm($inputData, $enctype=null) {
		$cols = $this->dao->getCols();
		$this->dao->findById($inputData["id"]);
		echo "<form method='post' ";
		if($enctype!==null) {
			echo "enctype='$enctype' ";
		}
		echo "action='$_SERVER[PHP_SELF]'>\n";
		echo "<input type='hidden' name='action' value='update' />";
		$this->view->generateForm($cols, $this->dao->getRow());
		echo "</form>";
	}
}
?>
