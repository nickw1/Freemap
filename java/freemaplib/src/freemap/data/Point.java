package freemap.data;



import java.io.*;




import java.util.Arrays;

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
	
	// Assumption: x increases eastwards, y increases northwards	
		public double bearingFrom(Point p)
		{
			double dx=x-p.x, dy=y-p.y, bearing=-((Math.atan2(dy,dx)*(180.0/Math.PI))-90);
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
	
	// from old osmeditor2 code - comments as follows:
    // find the distance from a point to a line
    // based on theory at: 
    // astronomy.swin.edu.au/~pbourke/geometry/pointline/
    // given equation was proven starting with dot product

    public double haversineDistToLine (Point p1, Point p2)
    {
        double u = ((x-p1.x)*(p2.x-p1.x)+(y-p1.y)*(p2.y-p1.y)) / (Math.pow(p2.x-p1.x,2)+Math.pow(p2.y-p1.y,2));
        double xintersection = p1.x+u*(p2.x-p1.x), yintersection=p1.y+u*(p2.y-p1.y);
        
        return (u>=0&&u<=1) ? Algorithms.haversineDist(x,y,xintersection,yintersection) : -1;
    }
    
    // Assumption: points are in standard wgs84 lat/lon
    
    
    
    public static void main (String[] args) throws java.io.IOException
    {
        String base="090314";
        BufferedReader r = new BufferedReader(new FileReader("/home/nick/gpx/"+base+".txt"));
      
        java.util.ArrayList<TrackPoint> points = new java.util.ArrayList<TrackPoint>();
        System.out.println("Reading in...");
        double distMetres = 5.0;
        
        String txt;
        
        while((txt = r.readLine())!=null)
        {
            String[] values = txt.split(",");
            if(!(values.length>2 && values[2].contains("WAYPOINT")))
                points.add(new TrackPoint(Float.parseFloat(values[0]), Float.parseFloat(values[1])));
        }
        TrackPoint[] pts =new TrackPoint[points.size()];
        points.toArray(pts);
        System.out.println("Doing Douglas-Peucker... in points = " + pts.length);
       
        Point[] simp = Algorithms.douglasPeucker(pts, distMetres);
        System.out.println("Writing out... out points=" + simp.length);
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("/home/nick/gpx/"+base+".simp.r."+(int)distMetres+".txt")));
        for(int i=0; i<simp.length; i++)
            pw.println(simp[i].x+", " + simp[i].y);
        pw.close();
    }
}
