package freemap.data;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class WalkrouteSummary {
	
	Projection proj;
	Point start;
	
	

	String title, description;
	int id;
	double presetDistance;
	

	public WalkrouteSummary()
	{
		title=description="Unknown";
		presetDistance = -1.0;
	}
	
	public WalkrouteSummary(String title, String description)
	{
		setTitle(title);
		setDescription(description);
	}

	public void setDistance (double d)
	{
		presetDistance = d;
	}
	
	
	public void setTitle(String title)
	{
		this.title=title;
	}
	
	public void setDescription(String description)
	{
		this.description=description;
	}
	
	public void setId(int id)
	{
		this.id=id;
	}
	
	public int getId()
	{
		return id;
	}
	
	public void setStart(Point start)
	{
		this.start=start;
	}
	
	public String toString()
	{
		String desc = "WALKROUTE Title: " + title + " Description: " + description + "\n";
		return desc;

	}
	
	public String toXML()
	{
		DecimalFormat format=new DecimalFormat("000");
		String desc = "<gpx><trk><name>" + title + "</name><desc>" + description + "</desc><number>"+id+
				"</number><extensions><distance>" + getDistance() + "</distance></extensions>";
		desc+="</gpx>";
		return desc;

	}
	public String getTitle()
	{
		return title;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	
	public double getDistance()
	{
		return presetDistance;
	}
	
	public void setProjection(Projection proj)
	{
		this.proj = proj;
	}
	
	public Point getStart()
	{
		return start;
	}
}
