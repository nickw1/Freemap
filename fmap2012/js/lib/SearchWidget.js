

function handleEvent(eventType,el,cb,scope)
{
    el.addEventListener(eventType,function(e) { cb.apply(scope,[e]); },false);
}


function SearchWidget(divId,options,resultsDivStyle)
{
    this.options=new Object();
    for(k in options)
        this.options[k] = options[k];

    var lbl = document.createElement("label"),
            txt = document.createElement("input"),
            btn = document.createElement("input"),
            results = document.createElement("div"),
            holder = document.createElement("div"),
            div = document.getElementById(divId);
        
    txt.id="searchterm";
    btn.type="button";
    btn.value="Go!";
    results.id="results";
    results.style.overflow = 'auto';
    if(resultsDivStyle)
    {
        for(k in resultsDivStyle)
            results.style[k]=resultsDivStyle[k];
    }

    holder.appendChild(txt);
    holder.appendChild(document.createElement("br"));
    holder.appendChild(btn);
    var heading=document.createElement("h1");
    heading.appendChild(document.createTextNode("Search"));
    heading.style.marginLeft='auto';
    heading.style.marginRight='auto';
    div.appendChild(heading);
    div.appendChild(holder);
    div.appendChild(results);
   
    handleEvent("click",btn,this.sendRequest,this);
}

SearchWidget.prototype.sendRequest  =function()
{
    var p = 'q='+document.getElementById('searchterm').value+'&format=json&'
            +this.options.parameters;

    new Ajax().sendRequest (this.options.url,
                                { parameters: p,
                                callback: this.resultsReturned,
                                callbackObject: this });
}

SearchWidget.prototype.resultsReturned = function(xmlHTTP)
{
    var results=document.getElementById('results');
    while(results.childNodes.length>0)
        results.removeChild(results.firstChild);
    this.json = JSON.parse(xmlHTTP.responseText);
    if(this.json.features && this.json.features.length > 0)
    {
        if(this.json.features.length==1)
        {
            this.options.callback
                    (this.json.features[0].geometry.coordinates[0],
                    this.json.features[0].geometry.coordinates[1]);
        }
        else
        {
            var resultsHeading=document.createElement("h2");
            resultsHeading.appendChild(document.createTextNode
                    ("Search results"));
            resultsHeading.style.marginLeft="auto";
            resultsHeading.style.marginRight="auto";
            results.appendChild(resultsHeading);
            for(var i=0; i<this.json.features.length; i++)
            {
                var name = (this.json.features[i].properties.name) ?
                    this.json.features[i].properties.name: "unnamed";
                var t = document.createTextNode(name + "(" +
                        this.json.features[i].properties.featuretype+")");    
                var b  = document.createElement("input");
                var a = document.createElement("a");
                a.href='#';
                a.id='result'+i;
                handleEvent("click",a,this.btnClick,this);
                a.appendChild(t);
                results.appendChild(a);
                results.appendChild(document.createElement("br"));
            }
        }
    }
}

SearchWidget.prototype.btnClick = function(e)
{
    var id = e.target.id.substring(6);
    this.options.callback
            (this.json.features[id].geometry.coordinates[0],
            this.json.features[id].geometry.coordinates[1]);
}


