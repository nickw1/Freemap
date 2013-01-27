
function OpenHants(lat,lon,zoom)
{

    var tileUrl = 'http://www.free-map.org.uk/0.6/ws/tsvr.php'+
        '?x={x}&y={y}&z={z}&way=all&poi=place,natural&kothic=1&contour=1'+
        '&coastline=1&tbl_prefix=hampshire';

    var kothic = new L.TileLayer.Kothic(tileUrl,
        {minZoom:11,
        attribution: 'Map data &copy; 2012 OpenStreetMap contributors,'+
        'CC-by-SA, contours &copy; Crown Copyright and database right ' +
        'Ordnance Survey 2011, Rendering by Kothic-JS, ROW overlay '+
        '(c) Hampshire County Council, Open Government licence'});

    this.row = new L.GeoJSON();
	this.reportedProblems = new L.GeoJSON();
    this.map = new L.Map("map", {layers:[kothic,this.row,this.reportedProblems]});


    this.map.setView(new L.LatLng(lat,lon), zoom);

    this.wgs84=new Proj4js.Proj('EPSG:4326');
    this.sphmerc=new Proj4js.Proj('EPSG:900913');

    this.ajax = new Ajax(); 
	this.markersAjax = new Ajax();

    this.map.on("dragend", this.loadGeoJSON.bind(this));
    this.map.on("zoomend", 
		(function()
			{
				if(this.map.getZoom()>=13)
				{
					this.map.addLayer(this.row);
					this.loadGeoJSON();
				}
				else
				{
					this.map.removeLayer(this.row);
				}
			}
		).bind(this) );
    
    this.row.on("featureparse", (function(e)
        { 
			try
			{
            var colours = { "Footpath" : "#ff00ff",
                    "Bridleway" : "#00ff00",
                    "BOAT" : "#aa5500",
                    "Restricted Byway" : "#0000ff" };
			var p = document.createElement("p");
			p.appendChild(document.createTextNode(e.properties.parish_row));
			var a =document.createElement("a");
			a.href ='#';
			a.id='gid_' + e.properties.gid;
			a.appendChild(document.createTextNode(" Report problem"));
			a.onclick =this.showProblemDialog.bind(this,e.properties.gid);
			p.appendChild(a);
			// won't work in IE 9 with vanilla Leaflet
			// see https://github.com/CloudMade/Leaflet/issues/695
            e.layer.bindPopup(p);
			e.layer.on("click", (function(e) 
				{ this.rowClickedPos = e.latlng; }).bind(this) );
            var style  = { color:  (colours[e.properties.row_type] ?
                colours[e.properties.row_type] : 'blue') };
            e.layer.setStyle(style);
			}
			catch(e) { alert(e) ; }
        } ).bind(this)
            );

	this.reportedProblems.on("featureparse", function(e)
		{
			e.layer.bindPopup(e.properties.text);
		}
		);

    this.indexedFeatures = new Array();
	this.indexedProblems = new Array();
    this.loadGeoJSON();

    new SearchWidget ('search',
                        { url: 'freemap/ws/search.php',
                          callback: this.setLocation.bind(this),
                          parameters: 'tbl_prefix=hampshire&poi=all&outProj=4326' });
}

OpenHants.prototype.loadGeoJSON = function()
{

	document.getElementById('permalink').href=
		'/index.php?lat=' + this.map.getCenter().lat+'&lon='+
		this.map.getCenter().lng + '&zoom=' + this.map.getZoom();

    var w=this.map.getBounds().getSouthWest().lng,
         s=this.map.getBounds().getSouthWest().lat,
        e=this.map.getBounds().getNorthEast().lng,
         n=this.map.getBounds().getNorthEast().lat;

    var sw=new Proj4js.Point(w,s), ne=new Proj4js.Point(e,n);
    Proj4js.transform(this.wgs84,this.sphmerc,sw);
    Proj4js.transform(this.wgs84,this.sphmerc,ne);


    this.ajax.sendRequest('hampshire.php',
                    { method: 'GET',
                        parameters: 'bbox='+sw.x+','+sw.y+','+ne.x+','+ne.y+
                                    '&inProj=900913&outProj=4326',
                                callback: this.addGeoJSON.bind(this) }
                            );

	this.markersAjax.sendRequest('row.php',
					{ method: 'GET',
					  parameters: 'action=getAllProblems&inProj=3857&bbox='+
					  		sw.x+','+sw.y+','+ne.x+','+ne.y+'&outProj=4326',
								callback: this.addProblemGeoJSON.bind(this) }
						);
}

OpenHants.prototype.addGeoJSON = function(xmlHTTP)
{
	var json = JSON.parse(xmlHTTP.responseText);
	for(var i=0; i<json.features.length; i++)
	{
		if(!(this.indexedFeatures[json.features[i].properties.gid]))
		{
			this.row.addGeoJSON(json.features[i]);
			this.indexedFeatures[json.features[i].properties.gid] =
                        json.features[i];

		}
	}
}

OpenHants.prototype.addProblemGeoJSON = function(xmlHTTP)
{
	try
	{
	var json = JSON.parse(xmlHTTP.responseText);
	//alert(xmlHTTP.responseText);
	for(var i=0; i<json.features.length; i++)
	{
		if(!this.indexedProblems[json.features[i].properties.gid])
		{
			this.reportedProblems.addGeoJSON(json.features[i]);
			this.indexedProblems[json.features[i].properties.gid]=
				json.features[i];
		}
	}
	}
	catch(e) { 
	/* weird error - response sometimes blank yet when you copy paste the url
	   reported here you get a sensible response. Unreproducible. seems to
	   happen randomly. doesn't affect how application works.
	alert(e+' returned:' + xmlHTTP.responseText + ' url was:' + xmlHTTP.url +
		 ' responseText was: ' + xmlHTTP.responseText + 
		 ' readyState was: ' + xmlHTTP.readyState);
	*/
	}
}

OpenHants.prototype.showProblemDialog = function(gid)
{
	var problemTypes = ["Structure - Stile",
						"Structure - Gate",
						"Structure - Squeeze Gap",
						"Structure - Steps",
						"Structure - Bridge",
						"Structure - Boardwalk",
						"Obstruction - Fence",
						"Obstruction - Locked Gate",
						"Obstruction - Seasonal Growth",
						"Obstruction - Fallen Tree",
						"Obstruction - Headland Path",
						"Obstruction - Crossfield Path",
						"Sign Posting - Fingerpost",
						"Sign Posting - Waymarking",
						"Surface",
						"Other"];
	this.dlg = new Dialog('main',
			{ ok: this.doSendProblem.bind(this,gid),
			cancel: this.cancelSendProblem.bind(this) },
		{ backgroundColor: '#8080ff',
			width: '400px',
			height: '480px',
			color: 'white',
			borderRadius: '15px' } );
	this.dlg.setPosition(100,100);
	var content="<p>Please enter the problem:<br />" +
				"<textarea id='theproblem' " +
				"style='margin:10px 10px 10px 10px; width:360px;height:150px'>"+
				"</textarea></p><p>Please select the problem type:<br />";
	content += "<select id='category'>";
	for(var i=0; i<problemTypes.length; i++)
		content += "<option>"+problemTypes[i]+"</option>";
	content += "</select></p>";
	content += "<p>Your name: <br /><input id='reporter_name' /><br /> "+
			"Your email: <br /><input id='reporter_email' /></p>";
	this.dlg.setContent(content);
	this.dlg.show();
}

OpenHants.prototype.cancelSendProblem = function()
{
	this.dlg.hide();
}


OpenHants.prototype.doSendProblem = function(gid)
{
	if(document.getElementById('reporter_name').value.length==0 ||
	   document.getElementById('reporter_email').value.length==0)
	{
		alert("Please enter a name and email!");
	}
	else
	{
		this.ajax.sendRequest ( 'row.php',
						{ method: 'POST',
						 parameters: 'action=addProblem&x=' + 
						 	this.rowClickedPos.lng
						 		+'&y=' +this.rowClickedPos.lat + '&problem=' +
								document.getElementById('theproblem').value +
								'&id=' + gid + '&category=' +
								document.getElementById('category').value +
								'&reporter_name='+
								document.getElementById('reporter_name').value +
								'&reporter_email='+
								document.getElementById('reporter_email').value,
						callback: (function(xmlHTTP) 
							{ alert(xmlHTTP.responseText); this.dlg.hide();
							  this.loadGeoJSON();}).bind(this)
						}
					);
	}
}

OpenHants.prototype.setLocation = function(x,y)
{
	this.map.panTo(new L.LatLng(y,x));
}

function init()
{
	new OpenHants(lat,lon,zoom);
}
