<?php

require_once('View.php');

class JsonView extends View {

    public function outputJson($data) {
        header("Content-type: application/json");
        echo json_encode($data);
    }
}

?>
