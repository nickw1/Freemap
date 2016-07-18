<?php

class FileUploader {

    private $field, $maxsize;
    const CANT_MOVE = 256, SECURITY_VIOLATION = 257, EXCEEDED_FILESIZE = 258,
            BLANK_FILENAME = 259;

    public function __construct ($field, $maxsize) {
        $this->field = $field;
        $this->maxsize = $maxsize;
    }

    public function uploadFile($uploaddir, $uploadfname) {

        $userfile = $this->getTmpName();
        $userfile_name = $this->getName();
        $userfile_size = $this->getSize();
        $userfile_type = $this->getType();
        $userfile_error = $this->getError();

        if ($userfile_error>0) {
            return $userfile_error;
        }  else {
            $upfile = ($uploadfname) ?
                    "$uploaddir/$uploadfname":
                    "$uploaddir/$userfile_name";

            return $this->safeMove($userfile, $upfile);
        }
    }

    public function safeMove($userfile, $upfile) {
        if(is_uploaded_file($userfile)) {
            if(!move_uploaded_file($userfile,$upfile)) { 
                return FileUploader::CANT_MOVE; // can't move
            }
        } else {
            // file upload security violation
            return FileUploader::SECURITY_VIOLATION; 
        }
    }

    public function getTmpName() {
        return $_FILES[$this->field]['tmp_name'];
    }

    public function getName() {
        return $_FILES[$this->field]['name'];
    }

    public function getSize() {
        return $_FILES[$this->field]['size'];
    }

    public function getType() {
        return $_FILES[$this->field]['type'];
    }

    public function getError() {
        return $_FILES[$this->field]['error'];
    }

    public function isUploadedFile() {
        return is_uploaded_file($this->getTmpName());
    }

    public function sizeOk() {
        return $this->getSize() <= $this->maxsize;
    }
}

?>
