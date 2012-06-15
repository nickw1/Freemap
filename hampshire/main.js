
function OpenHants(lat,lon,zoom)
{

    var tileUrl = 'http://www.free-map.org.uk/0.6/ws/tsvr.php'+
        '?x={x}&y={y}&z={z}&way=all&poi=place,natural&kothic=1&contour=1'+
        '&coastline=1';

    var kothic = new L.TileLayer.Kothic(tileUrl,
        {minZoom:11,
        attribution: 'Map data &copy; 2012 OpenStreetMap contributors,'+
        'CC-by-SA, contours &copy; Crown Copyright and database right ' +
        'Ordnance Survey 2011, Rendering by Kothic-JS, ROW overlay '+
        '(c) Hampshire County Council, Ordnance Survey OpenData licence'});

    this.row = new L.GeoJSON();
    this.map = new L.Map("map", {layers:[kothic,this.row]});


    this.map.setView(new L.LatLng(lat,lon), zoom);

    this.wgs84=new Proj4js.Proj('EPSG:4326');
    this.sphmerc=new Proj4js.Proj('EPSG:900913');

    this.ajax = new Ajax();

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
            var colours = { "Footpath" : "#00ff00",
                    "Bridleway" : "#aa5500",
                    "BOAT" : "#ff0000",
                    "Restricted Byway" : "magenta" };
			var p = document.createElement("p");
			p.appendChild(document.createTextNode(e.properties.parish_row));
			var a =document.createElement("a");
			a.href ='#';
			a.appendChild(document.createTextNode(" Report problem"));
			a.id='rplink_' + e.properties.gid;
			a.onclick = this.rp.bind(this);
			p.appendChild(a);
            e.layer.bindPopup(p);
			e.layer.on("click", (function(e) 
				{ this.rowClickedPos = e.latlng; }).bind(this) );
            var style  = { color:  (colours[e.properties.row_type] ?
                colours[e.properties.row_type] : 'blue') };
            e.layer.setStyle(style);
        } ).bind(this)
            );

    this.indexedFeatures = new Array();
    this.loadGeoJSON();
}

OpenHants.prototype.loadGeoJSON = function()
{
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
}

OpenHants.prototype.addGeoJSON = function(xmlHTTP)
{

	var json = JSON.parse(xmlHTTP.responseText);
	for(var i=0; i<json.features.length; i++)
	{
		if(!this.indexedFeatures[json.features[i].properties.gid])
		{
			this.row.addGeoJSON(json.features[i]);
			this.indexedFeatures[json.features[i].properties.gid] =
                        json.features[i];

		}
	}
}

OpenHants.prototype.rp = function(e)
{
	this.gid=e.target.id.substring(7);
	this.dlg = new Dialog('main',
			{ ok: this.doSendProblem.bind(this),
			cancel: this.cancelSendProblem.bind(this) },
		{ backgroundColor: '#8080ff',
			width: '400px',
			height: '300px',
			color: 'white',
			borderRadius: '15px' } );
	this.dlg.setPosition(100,100);
	this.dlg.setContent("<p>Please enter the problem:</p>" +
				"<p><textarea id='theproblem' " +
				"style='margin:10px 10px 10px 10px; width:360px;height:150px'>"+
				"</textarea></p>");
	this.dlg.show();
}

OpenHants.prototype.cancelSendProblem = function()
{
	this.dlg.hide();
}


OpenHants.prototype.doSendProblem = function()
{
	this.ajax.sendRequest ( 'row.php',
						{ method: 'POST',
						 parameters: 'action=addProblem&x=' + 
						 	this.rowClickedPos.lng
						 		+'&y=' +this.rowClickedPos.lat + '&problem=' +
								document.getElementById('theproblem').value +
								'&id=' + this.gid,
						callback: (function(xmlHTTP) 
							{ alert(xmlHTTP.responseText); this.dlg.hide(); })
								.bind(this)
						}
					);
}

function init()
{
	new OpenHants(lat,lon,zoom);
}
