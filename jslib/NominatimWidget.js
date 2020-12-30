

function handleEvent(eventType,el,cb,scope)
{
    el.addEventListener(eventType,function(e) { cb.apply(scope,[e]); },false);
}


function NominatimWidget(divId,options,resultsDivStyle)
{
    this.options=new Object();
    for(k in options)
        this.options[k] = options[k];

    var lbl = document.createElement("label"),
            txt = document.createElement("input"),
            btn = document.createElement("input"),
            holder = document.createElement("div"),
            div = document.getElementById(divId);
    this.results = document.createElement("div"); 
    txt.id="searchterm";
    txt.type="search";
    btn.type="button";
    btn.value="search";
    this.results.id="nom_results";
    this.results.style.overflow = 'auto';
    this.results.style.zIndex = 999;
    this.results.style.visibility = "hidden";

    if(resultsDivStyle)
    {
        for(k in resultsDivStyle)
            this.results.style[k]=resultsDivStyle[k];
    }

    holder.appendChild(txt);
    holder.appendChild(btn);
    div.appendChild(holder);
    div.appendChild(this.results);
    handleEvent("click",btn,this.sendRequest,this);
    this.ajax = new Ajax();
}

NominatimWidget.prototype.sendRequest  =function()
{
    var p = 'q='+document.getElementById('searchterm').value+'&format=json&'
            +this.options.parameters;

    this.ajax.sendRequest (this.options.url,
                                { parameters: p,
                                method : 'GET',
                                callback: this.resultsReturned.bind(this) } );
}

NominatimWidget.prototype.resultsReturned = function(xmlHTTP)
{
    while(this.results.childNodes.length>0)
        this.results.removeChild(this.results.firstChild);
    this.json = JSON.parse(xmlHTTP.responseText);
    if(this.json && this.json.length > 0)
    {
        if(true)
        {
            this.results.style.visibility = 'visible';
            var p = document.createElement("p");
            p.innerHTML = "Search results provided by " +
							"<a href='http://wiki.openstreetmap.org"+
							"/wiki/Nominatim'>Nominatim</a> search from " +
                            "<a href='http://open.mapquestapi.com/nominatim'>"+
                            "Mapquest</a> " + 
                            "using <a href='http://www.openstreetmap.org'>"+
                            "OpenStreetMap</a> data";
            var ul = document.createElement("ul");
            for(var i=0; i<this.json.length; i++)
            {
                if(this.json[i]['class']=='place' 
                    || this.json[i]['class']=='natural') 
                {
                    var li = document.createElement("li");
                    var t = 
                             (this.json[i].address[this.json[i].type]  ?
                             this.json[i].address[this.json[i].type] : "") + 
                            "," + this.json[i].address.county
                            + " (" + this.json[i].type+")";
                    var tt = document.createTextNode(t);
                    var a = document.createElement("a");
                    a.href='#';
                    a.id='result'+i;
                    handleEvent("click",a,this.btnClick,this);
                    a.appendChild(tt);
                    li.appendChild(a);
                    ul.appendChild(li);
                }
            }
            this.results.appendChild(ul);
            this.results.appendChild(p);
            var p2 = document.createElement("p");
            p2.style.textAlign = "center";
            var close = document.createElement("input");
            close.type="button";
            close.value="close";
            close.onclick = (function(e)
            {
                this.results.style.visibility="hidden";
            }).bind(this);
            p2.appendChild(close);
            this.results.appendChild(p2);
        }
    }
    else
    {
        alert('No matches!');
    }
}

NominatimWidget.prototype.btnClick = function(e)
{
    var id = e.target.id.substring(6);
    this.results.style.visibility = "hidden";    
    this.options.callback (this.json[id].lon,this.json[id].lat);
}
