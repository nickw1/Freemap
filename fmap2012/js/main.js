var map,  geojsonLayer, indexedFeatures;

function init()
{
    var tileUrl = 'http://www.free-map.org.uk/0.6/ws/tsvr.php' +
		'?x={x}&y={y}&z={z}&way=all&poi=all&kothic=1&contour=1&coastline=1';
	var kothic=new L.TileLayer.Kothic(tileUrl,{minZoom:14,

        	attribution: 'Map data &copy; 2012 OpenStreetMap contributors, contours &copy; Crown Copyright and database right Ordnance Survey 2011, Rendering by <a href="http://github.com/kothic/kothic-js">Kothic JS</a>'} );
    geojsonLayer=new L.GeoJSON();
     map = new L.Map('map',{layers:[kothic,geojsonLayer]});
    var startPos= new L.LatLng(lat,lon);
	map.setView(new L.LatLng(lat,lon),zoom).addLayer(kothic);

    map.on('dragend',onDragEnd);

    map.addLayer(geojsonLayer);

    geojsonLayer.on("featureparse", function(e)
                        {
                            e.layer.bindPopup
                                (e.properties.text);
                        }
                    );


    indexedFeatures = new Array();

	new SearchWidget ('searchdiv',
						{ url: '/0.6/ws/search.php',
						  callback: setLocation,
						  parameters: 'poi=all&outProj=4326' } );

	loadAnnotations(map.getBounds());
}

function onMapClick(e)
{
    alert(e.latlng.lat+' ' + e.latlng.lng);
}

function setLocation(x,y)
{
	map.panTo(new L.LatLng(y,x));
}

function onDragEnd(e)
{
	loadAnnotations(e.target.getBounds());
}

function loadAnnotations(bounds)
{
    var url='/0.6/ws/bsvr.php';
    var qs ='bbox=' +
        bounds.getSouthWest().lng+ ',' + 
        bounds.getSouthWest().lat+ ',' + 
        bounds.getNorthEast().lng+ ',' + 
        bounds.getNorthEast().lat + '&inProj=4326&outProj=4326'
		+'&ann=1&format=geojson';
   

    new Ajax().sendRequest (url,{ parameters: qs, callback: processFeatures });
}

function processFeatures(xmlHTTP)
{
    var json = JSON.parse(xmlHTTP.responseText);
    for(var i=0; i<json.features.length; i++)
    {
        var c = json.features[i].geometry.coordinates;
        if (!indexedFeatures[json.features[i].properties.id])
        {
            indexedFeatures[json.features[i].properties.id]=
                json.features[i];
            geojsonLayer.addGeoJSON(json.features[i]);
        }
    }
}
