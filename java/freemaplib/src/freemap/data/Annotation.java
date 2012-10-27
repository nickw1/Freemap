package freemap.data;

import java.io.PrintWriter;

public class Annotation extends Projectable {
	Point point;
	String description, type;
	int id;
	
	public Annotation(int id,double x,double y,String description)
	{
		this(id,x,y,description,"");
	}
	public Annotation(int id,double x,double y, String description, String type)
	{
		point=new Point(x,y);
		this.description=description;
		this.type=type;
		this.id=id;
	}
	
	public String toString()
	{
		return "Annotation: id=" + id + " description: " + description +" type:" + type;
	}
	
	public void reproject(Projection newProj)
	{
		point = (proj==null) ? point: proj.unproject(point);
		point = (newProj==null) ? point: newProj.project(point);
		proj=newProj;
	}
	
	public void save (PrintWriter pw)
	{
		pw.println("<annotation x='"+point.x+"' y='"+point.y+"' id='"+id+"'>");
		pw.println("<description>"+description+"</description>");
		pw.println("</annotation>");
	}
	
	public int getId()
	{
		return id;
	}
	
	public Point getPoint()
	{
		return point;
	}

	public String getDescription()
	{
		return description;
	}
	
	public double distanceTo(Point p)
	{
		return point.distanceTo(p);
	}
}
