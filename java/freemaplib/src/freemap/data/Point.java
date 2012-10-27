package freemap.data;


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
}
