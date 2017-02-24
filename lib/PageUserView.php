<?php

require_once('UserView.php');

class PageUserView extends UserView
{
    protected $title, $css;

    function __construct($title, $css)
    {
		parent::__construct();
        $this->title = $title;
        $this->css = $css;
    }

    function head()
    {
        ?>
        <!DOCTYPE html>
        <html>
        <head>
        <title><?php echo $this->title; ?></title>
        <link rel='stylesheet' type='text/css' 
        href='<?php echo $this->css; ?>' />
        </head>
        <body>
        <?php
    }

    function links()
    {
        ?>
        <p><a href='index.php'>Main page</a></p>
        <?php
    }

    function closePage()
    {
        echo "</body></html>";
    }


    function outputBasicPage($heading, $msg)
    {
        $this->head();
        echo "<h1>$heading</h1>";
        echo "<p>$msg</p>";
        $this->links();
        $this->closePage();
    }
}
?>
