package freemap.data;

import freemap.jdem.DEM;
import java.io.PrintWriter;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

public class POI extends Feature{
	
	Point point;
	
	static class DistanceComparator implements Comparator<POI>
	{
			Point pt;
			public DistanceComparator(Point p ){pt=p; }
			public int compare(POI p1,POI p2)
			{
				double d1=p1.distanceTo(pt),
					d2=p2.distanceTo(pt);
				return (d1<d2) ? -1:(d1==d2 ? 0:1);
			}
			
			public boolean equals(POI p1,POI p2)
			{
				return p1.distanceTo(pt)==p2.distanceTo(pt);
			}
	}
	
	public POI(double x, double y)
	{
		point=new Point(x,y);
	}
	
	public Point getPoint()
	{
		return point;
	}
	
	public String toString()
	{
		return "POI: "  + point.toString() + "\n" + super.toString();
	}
	
	public void applyDEM(DEM dem)
	{
		point.z = dem.getHeight(point.x,point.y,proj);
	}
	
	public void save(PrintWriter pw)
	{
		pw.println("<poi x='" + point.x+"' y='" + point.y+"'>");
		pw.println(tagsAsXML());
		pw.println("</poi>");
	}
	
	public void reproject(Projection newProj)
	{
		point = (proj==null) ? point: proj.unproject(point);
		point = (newProj==null) ? point: newProj.project(point);
		proj=newProj;
	}
	
	public double distanceTo(Point p)
	{
		return point.distanceTo(p);
	}
	
	// Assumption: x increases eastwards, y increases northwards
	public double bearingFrom(Point p)
	{
		double dx=point.x-p.x, dy=point.y-p.y, bearing=-((Math.atan2(dy,dx)*(180.0/Math.PI))-90);
		return (bearing<0) ?bearing+360:bearing;
	}
	
	public String directionFrom(Point p)
	{
		double bearing=bearingFrom(p);
		if(bearing<22.5||bearing>=337.5)
			return "N";
		else if (bearing<67.5)
			return "NE";
		else if (bearing<112.5)
			return "E";
		else if (bearing<157.5)
			return "SE";
		else if (bearing<202.5)
			return "S";
		else if (bearing<247.5)
			return "SW";
		else if (bearing<292.5)
			return "W";
		else 
			return "NW";
	}
	
	public static void sortByDistanceFrom(List<POI> pois,Point p)
	{
		POI.DistanceComparator c=new POI.DistanceComparator(p);
		Collections.sort(pois,c);
	}
}
