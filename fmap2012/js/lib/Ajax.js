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

Ajax.prototype.sendRequest = function(URL,options,addData)
{
    //options.callbackObject = options.callbackObject||null;
    options.errorCallback = options.errorCallback || null;
    options.callback = options.callback || null;
    options.parameters = options.parameters||'';
    options.method = options.method || 'GET';
    var postData='';
    if(options.method=='POST')
        postData = options.parameters;
    else
        URL += '?' + options.parameters;
    

    // This line specifies we are GETing the data (GET request)
    this.xmlHTTP.open(options.method,URL, true);

    // Keep this line in - common to all ajax requests
    this.xmlHTTP.setRequestHeader('Content-Type',
                    'application/x-www-form-urlencoded');


    var self=this;

    this.xmlHTTP.onreadystatechange =  function()
        {
            if(self.xmlHTTP.readyState==4)
            {
                if(self.xmlHTTP.status!=200 && options.errorCallback)
                    options.errorCallback(self.xmlHTTP.status);
                else if(options.callback)
                    options.callback(self.xmlHTTP,addData);
            }
        }


    // Send the data.
    this.xmlHTTP.send(postData);
}

// Hack to deal with the "this" problem in event handlers
/*
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
                self.callback.apply(self.callbackObject,
                                            [self.xmlHTTP]);
            }
        }
}
*/
