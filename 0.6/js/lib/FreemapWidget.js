
function FreemapWidget(elemId,options)
{
    var options = options || {};
    var baseUrl = options.baseUrl
        || 'http://www.free-map.org.uk/0.6/ws/tsvr.php';

    var tileUrl = baseUrl +  
        '?x={x}&y={y}&z={z}&way=all&poi=all&kothic=1&contour=1&coastline=1';

    var kothic=new L.TileLayer.Kothic(tileUrl,{minZoom:11,
            attribution: 'Map data &copy; 2004-' + new Date().getFullYear() +
                        ' OpenStreetMap contributors,'+
                'Open Database Licence,'+
                'contours &copy; Crown Copyright and database right '+
                'Ordnance Survey 2011, Rendering by '+
                '<a href="http://github.com/kothic/kothic-js">Kothic JS</a>'} );

    var newLayers = [kothic];
    if(options.layers)
    {
        for(var i=0; i<options.layers.length; i++)
            newLayers.push(options.layers[i]);
    }
    options.layers = newLayers;
    L.Map.apply(this,[elemId,options]);
}

FreemapWidget.prototype = Object.create(L.Map.prototype);
