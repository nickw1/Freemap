<?php
require_once('/home/www-data/private/defines.php');
require_once('../lib/functionsnew.php');
require_once('../0.6/User.php');

session_start();

$conn=pg_connect("dbname=gis user=gis");
$cleaned=clean_input($_REQUEST,'pgsql');

switch($cleaned["action"])
{
    case "login":
        if(isset($cleaned["remote"]) && $cleaned["remote"])
            User::remoteLogin ($cleaned['username'],$cleaned['password']);
        else
        {
            User::login
                ($cleaned['username'],$cleaned['password'],
                    $cleaned['redirect'],
                    isset($cleaned['errorRedirect']) ?
                        $cleaned['errorRedirect']:$cleaned['redirect']);
        }
        break;

    case "signup":

        if(isset($cleaned["username"]) && isset($cleaned["password"]))
        {
            $res=User::processSignup($cleaned['username'],$cleaned['password'],
                            $cleaned['email'],$cleaned['redirect']);
            if(is_int($res))
            {
                head("Sign up");
                User::displaySignupForm($res);
                ?>
                <p><em>Signing up will also register you for
                <a href='http://www.free-map.org.uk'>Freemap</a>. 
                Signing up does not allow you any extra functionality
                on fixmypaths right now, however this is likely to change.
                </em></p>
                <?php
                links();
                close_page();
            }
        }
        else
        {
            head("Sign up");
            User::displaySignupForm(null,$cleaned["redirect"]);
            ?>
            <p><em>Signing up will also register you for
            <a href='http://www.free-map.org.uk'>Freemap</a>. 
            Signing up does not allow you any extra functionality
            on fixmypaths right now, however this is likely to change.
            </em></p>
            <?php
            links();
            close_page();
        }
        break;

    case "delete":
        if(!isset($cleaned['id']))
        {
            js_msg( "Please specify a user ID.","index.php");
        }
        elseif(!isset($_SESSION['gatekeeper']))
        {
            js_msg("Please login","index.php");
        }
        else if ($_SESSION['level']!=1)
        {
            js_msg("Only the administrator can delete accounts.","index.php");
        }
        else 
        {
            head("Delete user");
            $u=new User($cleaned["id"]);
            if($u->isValid())
            {
                $u->remove();    
            }
            else
            {
                js_msg("Invalid user ID.","index.php");
            }
        }
        links();
        break;

    case "activate":
        head("Activate account");
        if(!isset($cleaned['id']) || !isset($cleaned['key']))
        {
            js_msg("id/key not supplied.","index.php");
        }
        else
        {
            $u=new User($cleaned['id']);
            if($u->isValid())
            {
                if($u->activate($cleaned['key']))
                    js_msg("User activated.","index.php");
            }
            else
            {
                js_msg("Invalid user ID.","index.php");
            }
        }
        links();
        break;

    case "logout":
        session_start();
        session_destroy();
        if(!isset($cleaned['redirect']))
            $cleaned['redirect'] = 'index.php';
        header("Location: $cleaned[redirect]");
        break;

    case "all":
        if(isset($_SESSION['gatekeeper']) && $_SESSION['level']==1)
        {
            head("All users");
            User::displayAll();
            links();
            close_page();
        }
        else
        {
            js_msg("Only administrators can view all users.","index.php");
        }
        break;

    case "displayDetails":
        if(!isset($_SESSION['gatekeeper']))
        {
            js_msg("Must be logged in!","index.php");
        }
        else
        {
            head("$_SESSION[gatekeeper]'s details");
            echo "<h1>$_SESSION[gatekeeper]'s details</h1>";
            $u=User::getUserFromUsername($_SESSION['gatekeeper']);
            $u->displayDetails();
            links();
            close_page();
        }
        break;
}

pg_close($conn);


function head($title="")
{
    ?>
    <html>
    <head>
    <title><?php echo $title; ?></title>
    <link rel='stylesheet' type='text/css' href='css/fixmypaths.css' />
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

function close_page()
{
    echo "</body></html>";
}
?>
