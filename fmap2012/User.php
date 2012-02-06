<?php


class User
{
    private $id,$valid;

    function __construct($id)
    {
        $this->id=$id;
        $result=pg_query("SELECT COUNT(*) AS count FROM users WHERE id=$id");
        $this->valid=pg_numrows($result)!=0;
    }

    function isValid()
    {
        return $this->valid;
    }

    static function displayLogin($redirect="index.php")
    {
        ?>
        <h1>Login</h1>
        <div>
        <form method="post" action=
        "user.php?action=login&redirect=<?php echo $redirect; ?>">
        <label for="username">Username</label>
        <input name="username" id="username" /> <br />
        <label for="password">Password</label>
        <input name="password" id="password" type="password" /> <br />
        <input type="submit" value="Go!" />
        </form>
        </div>
        <div><a href='user.php?action=signup'>Sign up</a></div>
        <?php
    }


    static function login($username,$password,$redirect)
    {
        $q="select * from users where username='$username' ".
              "and password='".sha1($password)."' and active=1";
        $result=pg_query($q);
        if(pg_numrows($result)==1)
        {
            $row=pg_fetch_array($result,null,PGSQL_ASSOC);
            $_SESSION["gatekeeper"] = $row["username"];
            $_SESSION["level"] = $row["isadmin"];
            $qs="";
            if(strpos($redirect,"?")!==false)
            {
                list($redirect,$redirqs) = explode("?", $redirect);
                $qs = $redirqs."&".$qs;
            }
            header("Location: $redirect?$qs");
        }
        else
        {
            js_msg('Invalid login',$redirect);
        }
    }

    static function processSignup($username,$password,$email)
    {
        $result=pg_query("SELECT * FROM users WHERE username='$username'"); 
        if(pg_numrows($result)>0)
        {
            return 1;    
        }
        elseif(strstr($username," "))
        {
            return 2;    
        }
        elseif($username=="" || $password=="")
        {
            return 3;    
        }
        else
        {
            $random = rand (1000000,9999999);
            $q = ("insert into users (email,".
                    "username,password,active,k) ".
                    "values ('$email',".
                    "'$username','".
                    sha1($password).
                    "',0,$random)");
            pg_query($q); 
            $lastid=pg_insert_id("users");
            mail('nick_whitelegg@yahoo.co.uk',
                    'New Freemap account created', 
                    "New Freemap account created for $username ".
                    "(email $email). ".
                    "<a href=".
                    "\"http://www.free-map.org.uk/common/user.php?action=".
                        "delete&id=$lastid\">Delete</a>");
            mail($email, 'New Freemap account created', 
                    "New Freemap account created for $username.".
                    "Please activate by visiting this address: ".
                    "http://www.free-map.org.uk".
                    "/common/user.php?action=activate&id=$lastid".
                "&key=$random");
            ?>
            <h1>Signed up!</h1>
            <p>You've successfully 
                signed up. You will get an email to confirm
            your registration. Follow the link in this email to
            activate your account.</p>
            <a href='index.php'>Back to main page</a></p>
            <?php
            close_page();
            return new User($lastid);
        }
    }


    static function displaySignupForm($error=null)
    {
        head("Sign up");
        ?>
        <h1>Sign up</h1>
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
            "a username and password!!!!!</p>";
        }
        ?>
        <div>
        <form method="post" action="?action=signup">
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
        close_page();
    }

    function remove()
    {
        $result=pg_query("SELECT * FROM users WHERE id=".$this->id);
        if($row=pg_fetch_array($result,null,PGSQL_ASSOC))
        {
            pg_query("DELETE FROM users WHERE id=".$this->id);
            js_msg("User deleted","index.php");    
        }
        else
        {
            echo "Invalid user ID";
        }
    }

    function activate($key)
    {
        if($this->valid)
        {
            $result=pg_query
                ("SELECT * FROM users WHERE id=".$this->id." AND active=0");
            if($result)
            {
                $row=pg_fetch_array($result,null,PGSQL_ASSOC);
                if($row['k']==$key)
                {
                    pg_query("UPDATE users SET active=1,k=0 WHERE id=".
                        $this->id);
                    echo "You have now activated your account and may login.";
                }
                else
                {
                    pg_query("DELETE FROM users WHERE id=".$this->id);
                    echo "Invalid key. ";
                    echo "This account has now been deleted, you'll need ".
                         "to sign up again.";
                }
            }
            else
            {
                echo "Invalid id/key.";
            }
        }
    }

    function displayAll()
    {
        $result=pg_query("SELECT * FROM user");
        while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
        {
            echo "$row[username] ".
            ($row["active"]==1 ? "Active":
            "<a href='user.php?action=activate&id=$row[id]'>Activate</a> ".
            "<a href='user.php?action=delete&id=$row[id]'>Delete</a>") . " ";
            $u=new User($row['id']);
        }
    }

    function getUserFromUsername($user)
    {
        $result=pg_query("SELECT id FROM users WHERE username='$user'");
        if(pg_numrows($result)==1)
        {
            $row=pg_fetch_array($result,null,PGSQL_ASSOC);
            return new User($row["id"]);
        }
        return null;
    }

    function getID()
    {
        return $this->id;
    }
} 
?>
