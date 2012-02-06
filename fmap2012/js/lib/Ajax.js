// AJAX wrapper - designed to make it easy to code AJAX
// Licence: LGPL
// You can use this in the assignment as long as you credit it.

function Ajax()
{

    this.xmlHTTP = null;

    // The if statement is necessary as Firefox and Internet Explorer have
    // different implementations of AJAX.
    if(window.XMLHttpRequest)
    {
        // Set up the AJAX variable on Firefox
        this.xmlHTTP = new XMLHttpRequest();
    }
    else
    {
        // Set up the AJAX variable on Internet Explorer 
        this.xmlHTTP = new ActiveXObject("Microsoft.XMLHTTP");
    }
}

Ajax.prototype.sendRequest = function(URL,options)
{
	this.callbackObject = options.callbackObject||null;
	this.errorCallback = options.errorCallback || null;
	this.callbackFunction = options.callback || null;
	options.information = options.information||'';

    // This line specifies we are GETing the data (GET request)
    this.xmlHTTP.open('GET',URL+'?'+options.parameters, true);

    // Keep this line in - common to all ajax requests
    this.xmlHTTP.setRequestHeader('Content-Type',
                    'application/x-www-form-urlencoded');


	this.setupHandler(this);


    // Send the data.
    this.xmlHTTP.send('');
}

// Hack to deal with the "this" problem in event handlers
Ajax.prototype.setupHandler = function(self)
{
    // This line specifies the callback function - the code which will run
    // when we receive the response from the server.
    this.xmlHTTP.onreadystatechange =  function()
        {
            if(self.xmlHTTP.readyState==4)
			{
				if(self.xmlHTTP.status!=200 && self.errorCallback)
				{
					self.errorCallback.apply(self.callbackObject,
											[self.xmlHTTP.status]);
				}
                self.callbackFunction.apply(self.callbackObject,
											[self.xmlHTTP]);
			}
        }
}
