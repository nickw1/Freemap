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
		this(x,y,-1.0);
	}

	public POI(double x, double y, double z)
	{
	    point=new Point(x,y,z);
	}
	
	public Point getPoint()
	{
		return point;
	}

	public Point getUnprojectedPoint()
	{
		return proj==null? point:proj.unproject(point);
	}
	
	public String toString()
	{
		return "POI: " + point.toString() + "\n" + super.toString();
	}

	public void applyDEM(DEM dem)
	{
		point.z = dem.getHeight(point.x,point.y,proj);
	}

	public void save(PrintWriter pw)
	{
		pw.println("<poi x='" + point.x+"' y='" + point.y+"'>");
		System.out.println("<poi x='" + point.x+"' y='" + point.y+"'>");
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
		return point.bearingFrom(p);
		
	}

	public String directionFrom(Point p)
	{
		return point.directionFrom(p);
	}
	
	public static void sortByDistanceFrom(List<POI> pois,Point p)
	{
		POI.DistanceComparator c=new POI.DistanceComparator(p);
		Collections.sort(pois,c);
	}
}