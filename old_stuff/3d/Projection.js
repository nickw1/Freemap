
var Projection = new Object(); 

Projection.W = 40075016.68;
Projection.halfW = 20037508.34; 

Projection.lonToGoogle = function(lon)
{
	return lon * (Projection.W/360.0);
}

Projection.latToGoogle = function(lat)
{
	var a = Math.log(Math.tan((90+parseFloat(lat))*Math.PI / 360))/
		(Math.PI / 180);

	// we need negative as N is - and S is +
	return -(a * (Projection.W/360.0));
}

Projection.googleToLon = function(x)
{
	return (x/Projection.halfW) * 180.0;
}

Projection.googleToLat = function(y)
{
	var lat= (-y/Projection.halfW)*180.0;
	lat=180/Math.PI * (2*Math.atan(Math.exp(lat*Math.PI/180)) - Math.PI/2);
	return lat;
}
