
function Login(linkId, parentId, userUrl, styles, callbacks) 
{
    
    this.dialog = new Dialog(parentId,
                            { "ok": this.doLogin.bind(this) , 
							 "cancel": (function()
								{ this.dialog.hide(); }
										).bind(this)  },
							styles);

    this.dialog.setContent
    ("<label for='username'>Username:</label> <input id='username'/><br />"+
    "<label for='password'>Password:</label> "+
    "<input id='password' type='password'/><br />");
    this.dialog.div.addEventListener
        ("click", function(e) { e.stopPropagation(); } , false);
    this.ajax = new Ajax();
    this.link=document.getElementById(linkId);
    this.loggedIn=false;
    this.link.addEventListener ("click", this.clicked.bind(this), false);
    this.callbacks = callbacks;
	this.userUrl = userUrl;
}

Login.prototype.clicked = function()
{
    if(this.loggedIn)
        this.logout();
    else
        this.dialog.show();
}

Login.prototype.doLogin = function(e)
{
    e.stopPropagation();
    this.ajax.sendRequest
        (this.userUrl,
            { method: 'POST',
                parameters: 'action=login&remote=1&username=' +
                    document.getElementById("username").value +
                        '&password=' +
                    document.getElementById("password").value,
                callback: this.loginCallback.bind(this) }
        );
}

Login.prototype.loginCallback = function (xmlHTTP)
{
    if(xmlHTTP.status==200)
    {
        this.dialog.hide();
		var details = JSON.parse(xmlHTTP.responseText);
        this.setLoggedIn(details[0], details[1]);
    }
    else
    {
        alert("Incorrect login");
    }
}

Login.prototype.setLoggedIn = function(username, isadmin)
{
    this.link.innerHTML = "Logout";
    this.loggedIn=true;
    if(this.callbacks && this.callbacks.onLogin)
        this.callbacks.onLogin(username, isadmin);
}

Login.prototype.logout = function()
{
    this.ajax.sendRequest
        (this.userUrl,
            { method: 'POST',
                parameters: 'action=logout',
                callback: this.logoutCallback.bind(this) }
        );
}

Login.prototype.logoutCallback = function(xmlHTTP)
{
    if(xmlHTTP.status==200)
    {
        this.loggedIn = false;
        this.link.innerHTML = "Login";
        if(this.callbacks && this.callbacks.onLogout)
            this.callbacks.onLogout();
    }
    else
    {
        alert("Could not log out - server/network error");
    }
}
