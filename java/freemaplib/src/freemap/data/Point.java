package freemap.data;

import java.util.Arrays;

import java.io.*;

public class Point {
	public double x,y,z;
	
	public Point()   
	{ 
		this.z=-1.0; 
	} 
	
	public Point(double x, double y)
	{
		this.x=x;
		this.y=y;
		this.z=-1.0;
	}
	
	public Point(double x, double y, double z)
	{
		this.x=x;
		this.y=y;
		this.z=z;
	}
	
	public boolean equals (Point other)
	{
		return Math.abs(x-other.x) < 0.00000001 && Math.abs(y-other.y) < 0.00000001;
	}
	
	public String toString()
	{
		return "x= "+x+ " y="+y+ " z=" + z;
	}
	
	public double distanceTo(Point p)
	{
		double dx=p.x-x, dy=p.y-y;
		return Math.sqrt(dx*dx+dy*dy);
	}
	
	
	
	// from old osmeditor2 code - comments as follows:
    // find the distance from a point to a line
    // based on theory at: 
    // astronomy.swin.edu.au/~pbourke/geometry/pointline/
    // given equation was proven starting with dot product

    public double haversineDistToLine (Point p1, Point p2)
    {
        double u = ((x-p1.x)*(p2.x-p1.x)+(y-p1.y)*(p2.y-p1.y)) / (Math.pow(p2.x-p1.x,2)+Math.pow(p2.y-p1.y,2));
        double xintersection = p1.x+u*(p2.x-p1.x), yintersection=p1.y+u*(p2.y-p1.y);
        return (u>=0&&u<=1) ? Algorithms.haversineDist(x,y,xintersection,yintersection) : Double.MAX_VALUE;
    }
    
    // Assumption: points are in standard wgs84 lat/lon
    
    
    
    public static void main (String[] args) throws java.io.IOException
    {
        String base="T040114";
        BufferedReader r = new BufferedReader(new FileReader("/home/nick/gpx/"+base+".txt"));
      
        java.util.ArrayList<Point> points = new java.util.ArrayList<Point>();
        System.out.println("Reading in...");
        double distMetres = 5.0;
        
        String txt;
        
        while((txt = r.readLine())!=null)
        {
            String[] values = txt.split(",");
            points.add(new Point(Float.parseFloat(values[0]), Float.parseFloat(values[1])));
        }
        Point[] pts =new Point[points.size()];
        points.toArray(pts);
        System.out.println("Doing Douglas-Peucker...");
        Point[] simp = Algorithms.douglasPeucker(pts, distMetres);
        System.out.println("Writing out...");
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("/home/nick/gpx/"+base+".simp."+(int)distMetres+".txt")));
        for(int i=0; i<simp.length; i++)
            pw.println(simp[i].x+", " + simp[i].y);
        pw.close();
    }
}
