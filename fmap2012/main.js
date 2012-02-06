var map, wgs84, goog, geojsonLayer, indexedFeatures;

function init()
{
    var tileUrl = 'http://www.free-map.org.uk/0.6/ws/tsvr.php' +
		'?x={x}&y={y}&z={z}&way=all&poi=all&contour=1&kothic=1&coastline=1';
	var kothic=new L.TileLayer.Kothic(tileUrl,{minZoom:14,

        	attribution: 'Map data &copy; 2011 OpenStreetMap contributors, contours &copy; Crown Copyright and database right Ordnance Survey 2011, Rendering by <a href="http://github.com/kothic/kothic-js">Kothic JS</a>'} );
    geojsonLayer=new L.GeoJSON();
     map = new L.Map('map',{layers:[kothic,geojsonLayer]});
    var startPos= new L.LatLng(lat,lon);
	map.setView(new L.LatLng(lat,lon),zoom).addLayer(kothic);
	/* testing leaflet
    var marker = new L.Marker(startPos);
    map.addLayer(marker);
    var circle = new L.Circle(new L.LatLng(51.06,-0.73),
        500, {color: 'red', fillColor: 'red',
                    fillOpacity: 0.2 } );
    map.addLayer(circle);
    var polygon = new L.Polygon (
        [ new L.LatLng(51.07,-0.73),
          new L.LatLng(51.06,-0.72),
          new L.LatLng(51.07,-0.71) ] );
    map.addLayer(polygon);
    marker.bindPopup("Fernhurst");
    circle.bindPopup("Marley Heights");
    polygon.bindPopup("Fridays Hill");
    map.on('click',onMapClick);
	*/
    map.on('dragend',onDragEnd);

    map.addLayer(geojsonLayer);

    geojsonLayer.on("featureparse", function(e)
                        {
                            e.layer.bindPopup
                                (e.properties.osm_id+' ' +
                                 e.properties.name);
                        }
                    );

    wgs84=new Proj4js.Proj('EPSG:4326');
    goog=new Proj4js.Proj('EPSG:900913');

    indexedFeatures = new Array();
}

function onMapClick(e)
{
    alert(e.latlng.lat+' ' + e.latlng.lng);
}

function onDragEnd(e)
{
    var url='/0.6/ws/bsvr.php';
    var qs ='bbox=' +
        e.target.getBounds().getSouthWest().lng+ ',' + 
        e.target.getBounds().getSouthWest().lat+ ',' + 
        e.target.getBounds().getNorthEast().lng+ ',' + 
        e.target.getBounds().getNorthEast().lat + '&inProj=4326&outProj=900913'
		+'&poi=place,amenity,natural&format=geojson';
    
    new Ajax.Request (url,
        { method : 'GET',
          parameters: qs,
          onComplete: processFeatures } 
                      );
}

function processFeatures(xmlHTTP)
{
    var json = xmlHTTP.responseText.evalJSON();
    for(var i=0; i<json.features.length; i++)
    {
        var c = json.features[i].geometry.coordinates;
        var p = new Proj4js.Point(c[0],c[1]);
        Proj4js.transform(goog,wgs84,p);
        json.features[i].geometry.coordinates[0] = p.x;
        json.features[i].geometry.coordinates[1] = p.y;

        if (!indexedFeatures[json.features[i].properties.osm_id])
        {
            indexedFeatures[json.features[i].properties.osm_id]=
                json.features[i];
            geojsonLayer.addGeoJSON(json.features[i]);
        }
    }
}
