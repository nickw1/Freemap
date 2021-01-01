


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
    holder.id="searchwidget";
    results.id="results";
    results.style.overflow = 'auto';
    if(resultsDivStyle)
    {
        for(k in resultsDivStyle)
            results.style[k]=resultsDivStyle[k];
    }

    holder.appendChild(txt);
    if(typeof(options.heading)=="undefined" || options.heading===true)
    {
//        holder.appendChild(document.createElement("br"));
        var heading=document.createElement("h1");
        heading.appendChild(document.createTextNode("Search"));
        heading.style.marginLeft='auto';
        heading.style.marginRight='auto';
        div.appendChild(heading);
    }
    holder.appendChild(btn);
    div.appendChild(holder);
    div.appendChild(results);
   
    btn.addEventListener('click', this.sendRequest.bind(this));
}

SearchWidget.prototype.sendRequest  =function()
{
    var p = 'q='+document.getElementById('searchterm').value+'&format=json&'
            +this.options.parameters;

    var xhr2 = new XMLHttpRequest();
    xhr2.addEventListener ("load", this.resultsReturned.bind(this));
    xhr2.open('GET', this.options.url + '?' + p);
    xhr2.send();
}

SearchWidget.prototype.resultsReturned = function(e)
{
    var results=document.getElementById('results');
    while(results.childNodes.length>0)
        results.removeChild(results.firstChild);
    this.json = JSON.parse(e.target.responseText);
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
                var nameContainer = document.createElement("strong");
                var name = (this.json.features[i].properties.name) ?
                    this.json.features[i].properties.name: "unnamed";
                nameContainer.appendChild(document.createTextNode(name));
                var is_in = (this.json.features[i].properties.is_in) ?
                    this.json.features[i].properties.is_in: "";
                var t = document.createTextNode(
                    (this.json.features[i].properties.is_in ?
                    ","+this.json.features[i].properties.is_in : "")
                        +"(" +
                        this.json.features[i].properties.featuretype+")");    
                var b  = document.createElement("input");
                var a = document.createElement("a");
                a.href='#';
                a.addEventListener('click', this.btnClick.bind(this, i));
                a.appendChild(nameContainer);
                a.appendChild(t);
                results.appendChild(a);
                results.appendChild(document.createElement("br"));
            }
        }
    }
    else
    {
        alert('No matches!');
    }
}

SearchWidget.prototype.btnClick = function(id, e)
{
    this.options.callback
            (this.json.features[id].geometry.coordinates[0],
            this.json.features[id].geometry.coordinates[1]);
}


