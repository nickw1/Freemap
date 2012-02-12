
function Freemap(lat,lon,zoom)
{
    var tileUrl = 'http://www.free-map.org.uk/0.6/ws/tsvr.php' +
        '?x={x}&y={y}&z={z}&way=all&poi=all&kothic=1&contour=1&coastline=1';

    var kothic=new L.TileLayer.Kothic(tileUrl,{minZoom:14,
            attribution: 'Map data &copy; 2012 OpenStreetMap contributors,'+
                'contours &copy; Crown Copyright and database right '+
                'Ordnance Survey 2011, Rendering by '+
                '<a href="http://github.com/kothic/kothic-js">Kothic JS</a>'} );

    this.geojsonLayer=new L.GeoJSON();
    this.map = new L.Map('map',{layers:[kothic,this.geojsonLayer]});
    var startPos= new L.LatLng(lat,lon);
    this.map.setView(new L.LatLng(lat,lon),zoom).addLayer(kothic);


    this.map.on('dragend',this.onDragEnd.bind(this));

    this.map.addLayer(this.geojsonLayer);

    this.map.on('click',this.onMapClick,this);

    this.geojsonLayer.on("featureparse", function(e)
                        {
                            e.layer.bindPopup
                                (e.properties.text);
                        }
                    );


    this.indexedFeatures = new Array();

    new SearchWidget ('searchdiv',
                        { url: '/0.6/ws/search.php',
                          callback: this.setLocation.bind(this),
                          parameters: 'poi=all&outProj=4326' } );
    this.ajax=new Ajax();
    this.loadAnnotations(this.map.getBounds());

    this.dlg=new Dialog('main',
        { ok: this.dlgOkPressed.bind(this), 
            cancel: this.dlgCancelPressed.bind(this) },
        { backgroundColor: '#8080ff',
            width: '400px' ,
            height: '300px',
            color: 'white' }
            );
    
    this.dlg.setContent 
        ("<p>Please enter details: </p>"+
            "<p><textarea id='annotation' " +
            "style='margin: 10px 10px 10px 10px;width:360px;height:150px'>"+
            "</textarea></p>" );
}

Freemap.prototype.dlgOkPressed = function(e)
{
    e.stopPropagation();
    this.ajax.sendRequest('/0.6/ws/annotation.php',
                            { parameters: 'text=' +
                                document.getElementById('annotation').value+
                                '&lat='+this.mapClickPos.lat+'&lon='+
                                    this.mapClickPos.lng,
                              callback: this.addAnnCallback.bind(this) },
                              this.mapClickPos
                        );
}

Freemap.prototype.dlgCancelPressed=function(e)
{
    this.dlg.hide();
    this.map.on('click',this.onMapClick,this);
    e.stopPropagation();
}

Freemap.prototype.addAnnCallback = function(xmlHTTP,latlng)
{
    alert('annotation added with ID: ' + xmlHTTP.responseText);
    var marker = new L.Marker(latlng);
    marker.bindPopup(document.getElementById('annotation').value);
    this.map.addLayer(marker);
    this.indexedFeatures[xmlHTTP.responseText] = marker;
    this.dlg.hide();
    this.map.on('click',this.onMapClick,this);
}

Freemap.prototype.onMapClick = function(e)
{
    var p = this.map.latLngToLayerPoint(e.latlng);
    var self=this;
    this.mapClickPos=e.latlng;
    this.dlg.setPosition(p.x,(p.y<500 ? p.y:500));
    if(!this.dlg.isVisible())
    {
        this.dlg.show();
        this.map.off('click',this.onMapClick,this);
    }
    document.getElementById('annotation').focus();
}

Freemap.prototype.setLocation = function(x,y)
{
    this.map.panTo(new L.LatLng(y,x));
}

Freemap.prototype.onDragEnd=function(e)
{
    this.loadAnnotations(e.target.getBounds());
}

Freemap.prototype.loadAnnotations=function(bounds)
{
    var url='/0.6/ws/bsvr.php';
    var qs ='bbox=' +
            bounds.getSouthWest().lng+ ',' + 
            bounds.getSouthWest().lat+ ',' + 
            bounds.getNorthEast().lng+ ',' + 
            bounds.getNorthEast().lat + '&inProj=4326&outProj=4326'
            +'&ann=1&format=geojson';
    this.ajax.sendRequest 
        (url,{ parameters: qs, callback: this.processFeatures.bind(this) });
}

Freemap.prototype.processFeatures=function(xmlHTTP)
{
    var json = JSON.parse(xmlHTTP.responseText);
    for(var i=0; i<json.features.length; i++)
    {
        var c = json.features[i].geometry.coordinates;
        if (!this.indexedFeatures[json.features[i].properties.id])
        {
            this.indexedFeatures[json.features[i].properties.id]=
                    json.features[i];
            this.geojsonLayer.addGeoJSON(json.features[i]);
        }
    }
}

function init()
{
    new Freemap(lat,lon,zoom);
}
