package freemap.opentrail;

import freemap.data.Point;
import com.jhlabs.map.proj.ProjectionFactory;
import com.jhlabs.map.Point2D; // must use modified, AWT-free proj4java

// Wrapper class to allow the proj4java library to be used with
// the freemaplib Projection interface.

public class Proj4Projection implements freemap.data.Projection {

	String name;
	com.jhlabs.map.proj.Projection proj4proj;
	
	public Proj4Projection (String name)
	{
		name=name.toLowerCase();
		proj4proj = ProjectionFactory.getNamedPROJ4CoordinateSystem(name);
		if (proj4proj!=null)
			proj4proj.setName(name);
	}
	
	public Point project (Point p)
	{
		if(proj4proj!=null)
		{
			Point2D.Double in=new Point2D.Double(p.x,p.y), out=new Point2D.Double();
			out=proj4proj.transform(in,out);
			return new Point(out.x,out.y);
		}
		return null;
	}
	
	public Point unproject(Point p)
	{
		if(proj4proj!=null)
		{
			Point2D.Double in=new Point2D.Double(p.x,p.y), out=new Point2D.Double();
			out=proj4proj.inverseTransform(in,out);
			return new Point(out.x,out.y);
		}
		return null;
	}
	
	public boolean equals(Proj4Projection other)
	{
		return (proj4proj==null) ? false:getID().equals(other.getID());
	}
	
	public String getID()
	{
		return (proj4proj==null) ? null:proj4proj.getName();
	}
	
	public String toString()
	{
		return getID();
	}
}
