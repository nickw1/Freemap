package freemap.data;

import java.util.ArrayList;
import freemap.jdem.DEM;
import java.io.PrintWriter;

public class Way extends Feature {

	ArrayList<Point> points;
	
	public Way()
	{
		points=new ArrayList<Point>();
	}
	
	public void addPoint(double x, double y)
	{
		points.add(new Point(x,y));
	}
	
	public void addPoint(double x, double y, double z)
	{
		points.add(new Point(x,y,z));
	}
	
	public int nPoints()
	{
		return points.size();
	}
	
	public Point getPoint(int i)
	{
		return points.get(i);
	}
	
	public Point getUnprojectedPoint (int i)
	{
		return proj == null ? points.get(i): proj.unproject(points.get(i));
	}
	
	public String toString()
	{
		return "Way: " + points.toString() + "\n" + super.toString();
	}
	
	public void applyDEM(DEM dem)
	{
		for(Point p: points)
		{
			p.z = dem.getHeight(p.x,p.y,proj);
		}
	}
	
	public boolean isWithin(DEM dem)
	{
		for (Point p: points)
		{
			if(dem.pointWithin(p,proj))
				return true;
		}
		return false;
	}
	
	public void save(PrintWriter pw)
	{
		pw.println("<way>");
		for(Point p: points)
		{
			pw.println("<point x='" + p.x+ "' y='" + p.y+ "' z='" + p.z + "' />");
		}
		pw.println(tagsAsXML());
		pw.println("</way>");
	}
	
	// in whatever units the points are represented as
	public double length()
	{
		double len = 0.0;
		for(int i=0; i<points.size()-1; i++)
			len += points.get(i).distanceTo(points.get(i+1));
		return len;
	}
	
	public double distanceTo(Point p)
	{
		double smallestDistance = Double.MAX_VALUE,curDist;
		for(int i=0; i<points.size(); i++)
		{
			curDist = points.get(i).distanceTo(p);
			if(curDist<smallestDistance)
				smallestDistance=curDist;
		}
		return smallestDistance;	
	}
	
	// 130715 corrected to unproject way points when calculating distance
	public double haversineDistanceTo(Point p)
	{
		double smallestDistance = Double.MAX_VALUE, curDist;
		for(int i=0; i<points.size(); i++)
		{
	
			Point unproj = (proj==null) ? points.get(i): proj.unproject(points.get(i));
			curDist = Algorithms.haversineDist(p.x, p.y, unproj.x, unproj.y);
		
			if(curDist < smallestDistance)
				smallestDistance = curDist;
		}
		return smallestDistance;
	}
	
	public void reproject(Projection newProj)
	{
	
		for(int i=0; i<points.size(); i++)
		{
			points.set(i, (proj==null) ? points.get(i): proj.unproject(points.get(i)));
			points.set(i, (newProj==null) ? points.get(i): newProj.project(points.get(i)));
		}
		proj=newProj;
	}
}
