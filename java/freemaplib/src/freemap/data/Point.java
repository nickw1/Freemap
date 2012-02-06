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
}
