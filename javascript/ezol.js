
var wgs84=new OpenLayers.Projection("EPSG:4326");
var _map;

function makeMap(id)
{
	_map = new OpenLayers.Map(id,
             {
                controls:[
                    new OpenLayers.Control.Navigation(),
                    new OpenLayers.Control.LayerSwitcher(),
                    new OpenLayers.Control.PanZoomBar(),
                    new OpenLayers.Control.Attribution()],
                maxExtent: new OpenLayers.Bounds
                (-20037508.34,-20037508.34,20037508.34,20037508.34),
                maxResolution: 156543.0399,
                numZoomLevels: 15,
                units: 'm',
                projection: new OpenLayers.Projection("EPSG:900913")
            } );
	return _map;
}

function project(ll)
{
	return ll.transform(wgs84,_map.getProjectionObject());
}

function unproject(sphmerc)
{
	return sphmerc.transform(_map.getProjectionObject(),wgs84);
}

function makePoint(lon,lat)
{
	var projected=project(new OpenLayers.LonLat(lon,lat));
	return new OpenLayers.Geometry.Point(projected.lon,projected.lat);
}
