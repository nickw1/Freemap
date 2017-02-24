<?php

class UserView 
{
    protected $ctrlScript; 

    function __construct()
    {
        $this->ctrlScript = basename($_SERVER["PHP_SELF"]);
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
        <label for="username">Username</label> <br />
        <input name="username" id="username" /> <br />
        <label for="password">Password</label> <br />
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
    
    function displaySignupConfirmation()
    {
        ?>
        <div id="content">
        <h1>Signed up!</h1>
        <p>You've successfully signed up.</p>
        </div>
        <?php
    }

    function displaySignupForm($error=null)
    {
        ?>
        <div id="content">
        <h1>Sign up</h1>
        <p><strong>IMPORTANT!</strong> Please do NOT use the same password
        as you use for security-critical services such as online banking,
        social networks, email etc. The site does not currently use an SSL
        server which means the security of your password cannot be
        guaranteed. Therefore please use a UNIQUE password.</p>
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
        <input type='submit' value='go'/>
        </form>
        </div>
        </div>
        <?php
    }

    static function showSignupError($error)
    {
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
        elseif($error==4)
        {
            echo "<p class='error'>Invalid username - letters and numbers ".
                "only!</p>";
        }
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
