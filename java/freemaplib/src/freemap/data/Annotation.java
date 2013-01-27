package freemap.data;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class Annotation extends Projectable {
	protected Point point;
	protected String description, type;
	protected int id;
	HashMap<String,String> extras;
	
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
		extras=new HashMap<String,String>();
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
	
	public void putExtra(String key,String value)
	{
		extras.put(key,value);
	}
	
	public void save (PrintWriter pw)
	{
		String curKey;
		pw.println("<annotation x='"+point.x+"' y='"+point.y+"' id='"+id+"'>");
		pw.println("<description>"+description+"</description>");
		if(!(type.equals("")))
			pw.println("<type>"+type+"</type>");
		if(!extras.isEmpty())
		{
			pw.println("<extras>");
			for(Map.Entry<String,String> entry: extras.entrySet())
			{
				curKey = entry.getKey();
				pw.println("<"+curKey+">"+entry.getValue()+"</"+curKey+">");
			}
			pw.println("</extras>");
		}
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
	
	public void setDescription(String description)
	{
		this.description=description;
	}
	
	public void setType(String type)
	{
		this.type=type;
	}
	
	public double distanceTo(Point p)
	{
		return point.distanceTo(p);
	}
}
