package freemap.data;

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
	
	// www.faqs.org/faqs/geography/infosystems-faq
	public static double haversineDist(double lon1, double lat1, double lon2, double lat2)
	{
		double R = 6371000;
		double dlon=(lon2-lon1)*(Math.PI / 180);
		double dlat=(lat2-lat1)*(Math.PI / 180);
		double slat=Math.sin(dlat/2);
		double slon=Math.sin(dlon/2);
		double a = slat*slat + Math.cos(lat1*(Math.PI/180))*Math.cos(lat2*(Math.PI/180))*slon*slon;
		double c = 2 *Math.asin(Math.min(1,Math.sqrt(a)));
		return R*c;
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
        return (u>=0&&u<=1) ? haversineDist(x,y,xintersection,yintersection) : Double.MAX_VALUE;
    }
    
    // Assumption: points are in standard wgs84 lat/lon
    
    public static Point[] douglasPeucker (Point[] points, double distMetres)
    {
        int index = -1;
        double maxDist = 0;
        
        double curDist;
        
        for(int i=1; i<points.length-1; i++)
        {
            curDist = points[i].haversineDistToLine(points[0], points[points.length-1]);
            if(curDist > maxDist)
            {
                index = i;
                maxDist = curDist;
            }
        }
        
        if (maxDist > distMetres)
        {
            Point[] before = Arrays.copyOfRange(points, 0, index), after = Arrays.copyOfRange(points, index, points.length-1);
            Point[] simp1 = douglasPeucker(before,distMetres), 
                        simp2 = douglasPeucker(after,distMetres);
            Point[] merged = new Point[simp1.length + simp2.length - 1];
            System.arraycopy(simp1, 0, merged, 0, simp1.length);
            System.arraycopy(simp2, 1, merged, simp1.length, simp2.length-1);
            return merged;
        }
        else
            return new Point[] { points[0], points[points.length-1] };
    }   
}
