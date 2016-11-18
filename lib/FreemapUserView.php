<?php

require_once("UserView.php");

class FreemapUserView extends UserView
{
    function __construct($title, $css)
    {
		parent::__construct ($title,$css);
    }

	// Strict Standards !
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
        <div id="content">
        <h1>Signed up!</h1>
        <p>You've successfully signed up for Freemap services
		(Freemap, FixMyPaths and OpenTrailView). 
		You will get an email to confirm
        your registration. 
		<em>Please note that Gmail, and possibly other providers,
		might put it in your spam folder. It will have a title of
		"New Freemap account created".</em>
		Follow the link in this email to activate your account.</p>
        <p><a href='/index.php'>Back to main page</a></p>
        </div>
        <?php
    }

    function displaySignupForm($error=null)
    {
        ?>
        <div id="content">
        <h1>Sign up</h1>
		<p>
		Sign up for all Freemap services: 
		<a href='http://www.free-map.org.uk'>Freemap</a>,
		<a href='http://www.fixmypaths.org'>FixMyPaths</a> and
		<a href='http://www.opentrailview.org'>OpenTrailView</a>.
		</p>
		<p><strong>IMPORTANT!</strong> Please do NOT use the same password
		as you use for security-critical services such as online banking,
		social networks, email etc. Freemap does not currently use an SSL
		server which means the security of your password cannot be
		guaranteed. Therefore please use a UNIQUE password for Freemap.</p>
        <p>Your email is used to send a confirmation message to you, once you
        have signed up. This message will consist of a link,
		to activate your account, which you should follow.
        Your email is NOT used for marketing or other similar activities!</p>
        <?php
		UserView::showSignupError($error);
        ?>
        <div>
        <form method="post" action=
        "<?php echo $this->ctrlScript;?>?action=signup">
        <label for="username">Enter a username</label><br/>
        <input name="username" id="username" /> <br/>
        <label for="password">Enter a password</label> <br/>
        <input name="password" id="password" type="password" /> <br/>
        <label for="email">Enter your email</label> <br/>
        <input name="email" id="email" /> <br/>
        <input type='submit' value='go'/>
        </form>
        </div>
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
        }
    }
}
?>
