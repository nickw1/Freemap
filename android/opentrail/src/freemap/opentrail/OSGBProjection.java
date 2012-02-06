package freemap.opentrail;

import freemap.data.Point;
import freemap.data.Projection;
import freemap.data.SimpleProjection;
import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.OSRef;

public class OSGBProjection extends SimpleProjection implements Projection{
	
	public Point project (Point lonLat)
	{
		LatLng ll=new LatLng(lonLat.y,lonLat.x);
		ll.toOSGB36();
		OSRef gr = ll.toOSRef();
		return new Point(gr.getEasting(), gr.getNorthing());
		
	}

	public Point unproject (Point projected)
	{
		OSRef gr = new OSRef(projected.x,projected.y);
		LatLng ll = gr.toLatLng();
		ll.toWGS84();
		return new Point(ll.getLng(), ll.getLat());
	}
	
	public String getID()
	{
		return "epsg:27700";
	}
}
