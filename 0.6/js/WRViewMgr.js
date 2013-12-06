
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
    this.ajax=new Ajax();
    this.app=app;
}

WRViewMgr.prototype.sendRequest = function()
{
    this.ajax.sendRequest ( '/0.6/ws/wr.php',
                        { method: 'GET',
                          parameters: 'action=getByUser&format=json',
                          callback: this.receivedRoutes.bind(this),
                          errorCallback: function(){alert('not logged in');} }
                          );
}

WRViewMgr.prototype.receivedRoutes = function(xmlHTTP)
{
    this.data = JSON.parse(xmlHTTP.responseText); 
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
        a.href='#';
        a.id='rte'+i;
        a.onclick = this.displayRoute.bind(this);
        var t = document.createTextNode(this.data.features[i].properties.title);
        a.appendChild(t);
        div.appendChild(a);
        div.appendChild(document.createTextNode(" "));
        var a2 = document.createElement("a");
        a2.href='#';
        a2.id='rte_togpx_' + this.data.features[i].properties.id;
        a2.onclick = (function(e)
            { window.location = 
                '/0.6/ws/wr.php?id='+e.target.id.substr(10)+
                '&action=get&format=gpx'; } ).bind(this);
        var t2 = document.createTextNode("Download GPX");
        a2.appendChild(t2);
        div.appendChild(a2);
        div.appendChild(document.createElement("br"));
    }
    outerDiv.appendChild(div);
    this.myRoutesDlg.setDOMContent(outerDiv);
    this.myRoutesDlg.show();
}

WRViewMgr.prototype.displayRoute  = function(e)
{
    var i = parseInt(e.target.id.substring(3));
    var ll = new L.LatLng(this.data.features[i].geometry.coordinates[1],
                            this.data.features[i].geometry.coordinates[0]);
    this.app.map.setView(ll,this.app.map.getZoom());
    this.loadRoute(this.data.features[i].properties.id);
}

WRViewMgr.prototype.loadRoute = function(i)
{
    this.ajax.sendRequest('/0.6/ws/wr.php',
                    { method : 'GET',
                     parameters: 'id=' + i +
                     '&action=get&format=json',
                     callback: this.doDisplayRoute.bind(this) }
                         );
}

WRViewMgr.prototype.doDisplayRoute = function(xmlHTTP)
{
    this.app.walkrouteLayer.clearLayers();
    var data = JSON.parse(xmlHTTP.responseText);
    //this.walkrouteLayer.addData(data);
    for(var i=0; i<data.features.length; i++)
    {
        if(data.features[i].geometry.type=='Point')
        {
            var WpIcon = L.icon (
            { iconUrl:'http://www.free-map.org.uk/0.6/flag.php?'
            +'n='+data.features[i].properties.id,
            shadowUrl:null,
            iconSize: new L.Point(32,32),
            shadowSize: null,
            iconAnchor: new L.Point(0,32),
            popupAnchor: new L.Point (2,-2)
            } );
            var marker = new L.Marker
                (new L.LatLng(data.features[i].geometry.coordinates[1],
                              data.features[i].geometry.coordinates[0]),
                    { icon: WpIcon }
                );
            var f = data.features[i];
            marker.bindPopup(f.properties.description);
            this.app.walkrouteLayer.addLayer(marker);
        }
        else
        {
            this.app.walkrouteLayer.addData(data.features[i]);
        }
    }
}

WRViewMgr.prototype.closeWindow = function()
{
    this.myRoutesDlg.hide();
}
