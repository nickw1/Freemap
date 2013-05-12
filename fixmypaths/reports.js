
function Reports(id,url,days,posCallback)
{
	this.div=document.getElementById(id);
	this.ajax=new Ajax();
	this.url=url;
	this.days=days;
	this.posCallback = posCallback;
}

Reports.prototype.load = function()
{
	this.ajax.sendRequest
		(this.url,
			{ method: 'GET',
			 parameters: 'outProj=4326&action=getProblemsByTime&days='+
			 	this.days,
			 callback: this.processReports.bind(this) } );
}

Reports.prototype.processReports = function(xmlHTTP)
{
	while(this.div.childNodes.length>0)
		this.div.removeChild(this.div.firstChild);
	var json = JSON.parse(xmlHTTP.responseText);
	for(var i=0; i<json.features.length; i++)
	{
		var p = document.createElement("p");
		p.appendChild(document.createTextNode
		 (json.features[i].properties.problem));
		var em = document.createElement("em");
		em.appendChild (document.createTextNode
		("(" + json.features[i].properties.parish + " " +
				json.features[i].properties.row_type + " " +
				json.features[i].properties.routeno + " " +
				"Submitted " + json.features[i].properties.subdate + ")"));
		p.appendChild(em);
		if(json.features[i].properties.status=="Fixed")
		{
			var str = document.createElement("strong");
			str.appendChild(document.createTextNode(" FIXED!"));
			p.appendChild(str);
		}

		var a = document.createElement("a");
		a.href='#';
		var atxt = document.createTextNode("Map");
		a.appendChild(atxt);
		var clickCb = (function(self,i)
		
			{				
				return (function()
				{
					self.posCallback(json.features[i].properties.id);
				}).bind(this);
			}
		) (this,i);
		a.onclick = clickCb;

		p.appendChild(a);
		this.div.appendChild(p);
	}
}
