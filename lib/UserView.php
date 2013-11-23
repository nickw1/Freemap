<?php

class UserView
{
    private $ctrlScript, $title, $css;

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
    function displayLogin($redirect="index.php", $errorRedirect="")
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
		<input type="hidden" name="redirect" value="<?php echo $redirect; ?>"/>
		<input type="hidden" name="action" value="login" />
        <input type="submit" value="Go!" />
        </form>
        </div>
        <div><a href='<?php echo $this->ctrlScript; ?>?action=signup'>
        Sign up</a></div>
        <?php
    }

    function displaySignupConfirmation()
    {
        ?>
        <h1>Signed up!</h1>
        <p>You've successfully signed up for Freemap services
		(Freemap, FixMyPaths and OpenTrailView). 
		You will get an email to confirm
        your registration. 
		<em>Please note that Gmail, and possibly other providers,
		might put it in your spam folder. It will have a title of
		"New Freemap account created".</em>
		Follow the link in this email to activate your account.</p>
        <a href='index.php'>Back to main page</a></p>
        <?php
    }

    function displaySignupForm($error=null)
    {
        ?>
        <h1>Sign up</h1>
		<p>
		Sign up for all Freemap services: 
		<a href='http://www.free-map.org.uk'>Freemap</a>,
		<a href='http://www.fixmypaths.org'>FixMyPaths</a> and
		<a href='http://www.opentrailview.org'>OpenTrailView</a>.
		</p>
        <?php
        if($error==1)
        {
            echo "<p class='error'>Error: Username already taken. ";
            echo "Please choose another one.</p>";
        }
        elseif($error==2)
        {
            echo "<p class='error'>Spaces not allowed in usernames.</p>";
        }
        elseif($error==3)
        {
            echo "<p class='error'>You've got to <em>actually provide</em> ".
            "a username and password!!!!! ;-)</p>";
        }
        ?>
        <div>
        <form method="post" action=
        "<?php echo $this->ctrlScript;?>?action=signup">
        <label for="username">Enter a username</label><br/>
        <input name="username" id="username" /> <br/>
        <label for="password">Enter a password</label> <br/>
        <input name="password" id="password" type="password" /> <br/>
        <label for="email">Enter your email address</label> <br />
        <input name="email" id="email" /> <br />
        <input type='submit' value='go'/>
        </form>
        </div>
        <?php
    }

    function displayAll()
    {
        $result=pg_query("SELECT * FROM users");
        while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
        {
            echo "$row[username] ".
            ($row["active"]==1 ? "Active":
            "<a href='".$this->ctrlScript.
                "?action=activate&id=$row[id]'>Activate</a> ".
            "<a href='".$this->ctrlScript.
                "?action=delete&id=$row[id]'>Delete</a>") . " ";
            $u=new User($row['id']);
        }
    }

    function redirectMsg($msg, $redirect)
    {
        js_msg($msg,$redirect);
    }
}
?>
