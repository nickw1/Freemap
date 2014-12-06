<?php

class UserView
{
    protected $ctrlScript, $title, $css;

    function __construct($title, $css)
    {
        $this->ctrlScript = basename($_SERVER["PHP_SELF"]);
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

    function redirectMsg($msg, $redirect)
    {
        js_msg($msg,$redirect);
    }

    function displayLogin($redirect="index.php", $errorRedirect="",
				$data=array())
    {
        ?>
        <h1>Login</h1>
        <div>
        <?php
        echo "<form method='post' action='".$this->ctrlScript."'>";
        ?>
        <label for="username">Username</label>
        <input name="username" id="username" /> <br />
        <label for="password">Password</label>
        <input name="password" id="password" type="password" /> <br />
		<input type="hidden" name="action" value="login" />
		<?php
		$this->doRedirect($redirect,$errorRedirect,$data);
		?>
        <input type="submit" value="Go!" />
        </form>
        </div>
        <div><a href='<?php echo $this->ctrlScript; ?>?action=signup'>
        Sign up</a></div>
        <?php
    }
	
	function doRedirect($redirect,$errorRedirect,$data)
	{
	?>
	<input type="hidden" name="redirect" value="<?php echo $redirect; ?>"/>
	<?php
	foreach($data as $k=>$v)
		if($k!="redirect")
			echo "<input type='hidden' name='$k' value='$v' /><br />";
	}
}
?>
