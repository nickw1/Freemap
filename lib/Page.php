<?php

require_once("functions.php");

class Page {

    public function writePage($title, $scripts, $css) {
        $this->openPage();
        $this->writeHead($title, $scripts, $css);
        $this->writeBody();
        $this->closePage();
    }

    public function writeHead($title, $scripts, $css) {

        $this->openHead($title);
        $this->writeScripts($scripts);
        $this->writeCss($css);
        $this->closeHead();
    }

    public function writeBody() {
        echo "<body>\n</body>\n";
    }

    public function openPage() {
        echo "<!DOCTYPE html>\n<html>\n";    
    }

    public function openHead($title) {
        echo "<head>\n";
        echo "<title>$title</title>\n";
    }

    public function writeScripts($scripts) {
        foreach($scripts as $script) {
            echo "<script type='text/javascript' src='$script'></script>\n";
        }
    }

    public function writeCss($css) {
        foreach($css as $stylesheet) {
            echo "<link rel='stylesheet' type='text/css' href='$stylesheet' />";
        }
    }

    public function closeHead() {
        echo "</head>\n";    
    }

    public function closePage() {
        echo "</html>\n";
    }
}

?>
