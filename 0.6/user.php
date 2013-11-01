<?php
require_once('../lib/UserController.php');
require_once('../lib/UserView.php');
require_once('../lib/functionsnew.php');

session_start();

$conn=pg_connect(pgconnstring());

$controller = new UserController(new UserView("User Control", "css/style.css"));
$controller->execute ($_REQUEST);

pg_close($conn);

?>
