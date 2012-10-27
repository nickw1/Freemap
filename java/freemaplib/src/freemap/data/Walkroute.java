package freemap.data;

import java.util.ArrayList;

public class Walkroute {
	
	Projection proj;
	
	public class Stage
	{
		public Point start;
		public String description;
		public int id;
		
		public Stage(int id, Point start, String description)
		{
			this.id=id;
			this.start=start;
			this.description=description;
		}
		
		public String toString()
		{
			return "Stage: start="  + start + " description=" + description;
		}
	}

	String title, description;
	int id;
	
	ArrayList<Point> points = new ArrayList<Point>();
	ArrayList<Stage> stages = new ArrayList<Stage>();

	public Walkroute()
	{
		title=description="Unknown";
	}
	
	public Walkroute(String title, String description)
	{
		setTitle(title);
		setDescription(description);
	}
	
	public void addStage(Point start,String description)
	{
		stages.add(new Stage(stages.size()+1,start,description));
	}
	
	public void addPoint(Point p)
	{
		points.add(p);
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
	
	public Point getStart()
	{
		return (points.size()>0) ? points.get(0) : null;
	}
	
	public Point getFinish()
	{
		return (points.size()>0) ? points.get(points.size()-1) : null;
	}
	
	public String toString()
	{
		String desc = "WALKROUTE Title: " + title + " Description: " + description + "\n";
		for(int i=0; i<stages.size(); i++)
		{
			desc += stages.get(i).toString() + "\n";
		}
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
	
	public ArrayList<Point> getPoints()
	{
		return points;
	}
	
	public ArrayList<Stage> getStages()
	{
		return stages;
	}
	
	// distLimit must be in metres, lonlat must be in lon/lat
	public Stage findNearestStage(Point lonlat, double distLimit)
	{
		double lowestSoFar=distLimit, dist;
		Stage nearest = null;
		Point startAsLL;
		
		for(int i=0; i<stages.size(); i++)
		{
			startAsLL=(proj==null)?stages.get(i).start:proj.unproject(stages.get(i).start);
			dist = Point.haversineDist(lonlat.x,lonlat.y,startAsLL.x,startAsLL.y);
			if(dist < lowestSoFar)
			{
				lowestSoFar = dist;
				nearest = stages.get(i);
			}
		}
		return nearest;
	}
	
	public void setProjection(Projection proj)
	{
		this.proj = proj;
	}
}
