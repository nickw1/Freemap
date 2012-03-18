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
    options.async = options.async || true;
    var postData='';
    if(options.method=='POST')
        postData = options.parameters;
    else
        URL += '?' + options.parameters;
    

    // This line specifies we are GETing the data (GET request)
    this.xmlHTTP.open(options.method,URL, options.async);

    // Keep this line in - common to all ajax requests
    this.xmlHTTP.setRequestHeader('Content-Type',
                    'application/x-www-form-urlencoded');


    if(options.async===true)
    {
        var self=this;

        this.xmlHTTP.onreadystatechange =  function(e)
        {
            if(self.xmlHTTP.readyState==4)
            {
                if(self.xmlHTTP.status!=200 && options.errorCallback)
                    options.errorCallback(self.xmlHTTP.status);
                else if(options.callback)
                    options.callback(self.xmlHTTP,addData);
            }
        }
    }

    // Send the data.
    this.xmlHTTP.send(postData);
}

