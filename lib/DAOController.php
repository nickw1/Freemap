<?php

require_once('Controller.php');

class DAOController extends Controller {

	protected $dao;

	public function __construct(View $view, $conn, $table=null) {
		parent::__construct($view);
		$class = str_replace("Controller","",static::class);
		require_once("{$class}.php");
		echo "Creating class $class<br />\n";
		$this->dao = new $class($conn, $table); 
	}

	public function actionCreate($rawHTTPData) {
		$this->dao->create($this->getRelevantData($rawHTTPData));
	}

	public function actionRetrieve($rawHTTPData) {
		$this->dao->findById($rawHTTPData["id"]);
		$this->view->outputRecord($this->dao->getRow());
	}

	public function actionRetrieveAll() {
		$this->view->outputAllRecords($this->dao->getAllRows());
	}

	public function actionUpdate($rawHTTPData) {
		$this->dao->setId($rawHTTPData["id"]);
		$this->dao->update($this->getRelevantData($rawHTTPData));
	}

	public function actionDelete($rawHTTPData) {
		$this->dao->setId($rawHTTPData["id"]);
		$this->dao->remove();
	}

	protected function getRelevantData($rawHTTPData) {
		$data = array();
		foreach($rawHTTPData as $k=>$v) {
			if($k!="action" && $k!="redirect" && $k!="id") {
				$data[$k] = $v;
			}
		}
		return $data;
	}

	public function actionCreateAddForm($rawHTTPData) {
		$cols = $this->dao->getCols();
		echo "<form method='post' action='$_SERVER[PHP_SELF]'>\n";
		echo "<input type='hidden' name='action' value='create' />";
		$this->view->generateForm($cols);
		echo "</form>";
	}

	public function actionCreateUpdateForm($rawHTTPData) {
		$cols = $this->dao->getCols();
		$this->dao->findById($rawHTTPData["id"]);
		echo "<form method='post' action='$_SERVER[PHP_SELF]'>\n";
		echo "<input type='hidden' name='action' value='update' />";
		$this->view->generateForm($cols, $this->dao->getRow());
		echo "</form>";
	}
}
?>
