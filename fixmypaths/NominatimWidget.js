

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
	var attrib = document.createElement("p");
	var em = document.createElement("em");
	em.appendChild(document.createTextNode
				("Nominatim Search provided by MapQuest using OpenStreetMap "+
				"data"));
	attrib.appendChild(em);
  	holder.appendChild(attrib); 
    div.appendChild(holder);
    div.appendChild(results);
    handleEvent("click",btn,this.sendRequest,this);
}

NominatimWidget.prototype.sendRequest  =function()
{
    var p = 'q='+document.getElementById('searchterm').value+',gb&format=json&'
            +this.options.parameters;

    new Ajax().sendRequest (this.options.url,
                                { parameters: p,
                                method : 'GET',
                                callback: this.resultsReturned.bind(this) } );
}

NominatimWidget.prototype.resultsReturned = function(xmlHTTP)
{
    var results=document.getElementById('results');
    while(results.childNodes.length>0)
        results.removeChild(results.firstChild);
    this.json = JSON.parse(xmlHTTP.responseText);
    if(this.json && this.json.length > 0)
    {
        if(this.json.length==1)
        {
            this.options.callback(this.json[0].lon,this.json[0].lat);
       	} 
        else
        {
			/*
            var resultsHeading=document.createElement("h2");
            resultsHeading.appendChild(document.createTextNode
                    ("Search results"));
            resultsHeading.style.marginLeft="auto";
            resultsHeading.style.marginRight="auto";
            results.appendChild(resultsHeading);
			*/
			var ul = document.createElement("ul");
            for(var i=0; i<this.json.length; i++)
            {
				if(this.json[i]['class']=='place' || 
					this.json[i]['class']=='amenity'
					||this.json[i]['class']=='railway'
					|| this.json[i]['class']=='natural') 
				{
					var li = document.createElement("li");
                	var t = document.createTextNode(
							 (this.json[i].address[this.json[i].type]  ?
							 this.json[i].address[this.json[i].type] : "") + 
							"," + this.json[i].address.county
							+ " (" + this.json[i].type+")");    
                	var b  = document.createElement("input");
                	var a = document.createElement("a");
                	a.href='#';
                	a.id='result'+i;
                	handleEvent("click",a,this.btnClick,this);
                	a.appendChild(t);
					li.appendChild(a);
                	ul.appendChild(li);
				}
            }
			results.appendChild(ul);
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
    this.options.callback (this.json[id].lon,this.json[id].lat);
}
