function Waypoint(mLL,text,id,editCallback,context)
{
	id = id || 'WP';
    if( typeof Waypoint.prototype.count == "undefined")
        Waypoint.prototype.count = 0;
    this.n=++Waypoint.prototype.count;
    var WpIcon = L.icon (
        { iconUrl:'/fm/flag.php?n=' + id,
        shadowUrl:null,
        iconSize: new L.Point(32,32),
        shadowSize: null,
        iconAnchor: new L.Point(0,32),
        popupAnchor: new L.Point (2,-2)
        } );
    this.description=text;
	this.edit = editCallback.bind(context,this,true);
    L.Marker.call(this,mLL,
        {icon: WpIcon, clickable:true,draggable:true});
}

Waypoint.prototype = Object.create(L.Marker.prototype); 

Waypoint.prototype.addToDelList = function(deletionList)
{
	deletionList.waypoints.push(this);
}


Waypoint.prototype.addDescription = function(d)
{
    this.description=d;
}

Waypoint.prototype.resetMarkerCount = function()
{
    Waypoint.prototype.count = 0;
}

Waypoint.prototype.toGeoJSONObj = function()
{
    var json=new Object();
    json.geometry=new Object();
    json.geometry.type="Point";
    json.geometry.coordinates = [ this.getLatLng().lng,
                                    this.getLatLng().lat ];
    json.properties = new Object();
    json.properties.id = this.id;
    json.properties.description = this.description;
    json.type = "Feature";
    return json;
}
function WRViewMgr(app)
{
   this.myRoutesDlg = new Dialog
        ( 'main',
            { ok: (function() { this.myRoutesDlg.hide(); } ).bind(this) },
            { backgroundColor: '#8080ff',
            color: 'white',
            width: '400px',
            height: '300px',
            borderRadius: '15px' }
            );
  this.myRoutesDlg.setPosition("100px","100px");
  this.app=app;

  this.sendRequest = function()
  { 
	var xhr2 = new XMLHttpRequest();
	xhr2.addEventListener("load", this.receivedRoutes.bind(this));
	xhr2.open("GET", "/fm/ws/wr.php?action=getByUser&format=json");
	xhr2.send();
  }

  this.receivedRoutes = function(e)
  {
    this.data = JSON.parse(e.target.responseText); 
    var html =  "";
    var outerDiv=document.createElement("div");
	outerDiv.id="myroutesdiv";
    var h3 = document.createElement("h3");
    h3.appendChild(document.createTextNode("My routes"));
    outerDiv.appendChild(h3);
    var div = document.createElement("div");
    div.style.overflow='auto';
    div.style.height = '200px';
    for(var i=0; i<this.data.features.length; i++)
    {
        var a = document.createElement("a");
        a.setAttribute("href","#");
		a.setAttribute("id", "rte"+i);
        a.addEventListener("click",this.displayRoute.bind(this));
        var t = document.createTextNode(this.data.features[i].properties.title);
        a.appendChild(t);
        div.appendChild(a);
        div.appendChild(document.createTextNode(" (" +
				  parseFloat(this.data.features[i].properties.distance).
					toFixed(2) +"km) "));
        var a2 = document.createElement("a");
        a2.setAttribute('href','#');
        a2.setAttribute('id','rte_togpx_'+this.data.features[i].properties.id);
        a2.addEventListener("click",  (function(e)
            { window.location = 
                '/fm/ws/wr.php?id='+e.target.id.substr(10)+
                '&action=get&format=gpx'; } ).bind(this));
        var t2 = document.createTextNode("Download GPX");
        a2.appendChild(t2);
        div.appendChild(a2);
        div.appendChild(document.createElement("br"));
    }
    outerDiv.appendChild(div);
    this.myRoutesDlg.setDOMContent(outerDiv);
    this.myRoutesDlg.show();
  }

  this.displayRoute  = function(e)
  {
    var i = parseInt(e.target.id.substring(3));
    var ll = new L.LatLng(this.data.features[i].geometry.coordinates[1],
                            this.data.features[i].geometry.coordinates[0]);
    this.app.map.setView(ll,this.app.map.getZoom());
    this.loadRoute(this.data.features[i].properties.id);
  }

  this.loadRoute = function(i)
  {
	var xhr2 = new XMLHttpRequest();
	xhr2.addEventListener("load", this.doDisplayRoute.bind(this,i));
	xhr2.open('GET', '/fm/ws/wr.php?id=' + i + '&action=get&format=json');
	xhr2.send();
  }

  this.doDisplayRoute = function(id,e)
  {
    this.app.walkrouteLayer.clearLayers();
    this.app.walkrouteWaypointsLayer.clearLayers();
    var data = JSON.parse(e.target.responseText);

    //this.walkrouteLayer.addData(data);
    for(var i=0; i<data.features.length; i++)
    {
        if(data.features[i].geometry.type=='Point')
        {
            var f = data.features[i];
		
            var marker = new Waypoint 
                (new L.LatLng(data.features[i].geometry.coordinates[1],
                              data.features[i].geometry.coordinates[0]),
					f.properties.description,
					f.properties.stage,
					this.app.saveAnnotationToServer, this.app
                );
            marker.bindPopup(f.properties.description);
			marker.id = f.properties.id;
            marker.walkroute = id;
            this.app.walkrouteWaypointsLayer.addLayer(marker);
			this.app.drawnItems.addLayer(marker);
        }
        else
        {
            this.app.walkrouteLayer.addData(data.features[i]);
        }
    }
  }

  this.closeWindow = function()
  {
    this.myRoutesDlg.hide();
  }
}
