package freemap.data;

import java.text.DecimalFormat;
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

	WalkrouteSummary summary;
	
	ArrayList<TrackPoint> points = new ArrayList<TrackPoint>();
	ArrayList<Stage> stages = new ArrayList<Stage>();

	public Walkroute()
	{
		summary=new WalkrouteSummary();
		
	}
	
	public Walkroute(String title, String description)
	{
		summary=new WalkrouteSummary(title,description);

	}

	public void setDistance (double d)
	{
		summary.setDistance(d);
	}
	
	public void addStage(Point start,String description)
	{
	
		stages.add(new Stage(stages.size()+1,start,description));
	}
	
	public void addPoint(TrackPoint p)
	{
		if(points.size()==0)
			summary.setStart(p);
		points.add(p);
	}
	
	public void setTitle(String title)
	{
		summary.setTitle(title);
	}
	
	public void setDescription(String description)
	{
		summary.setDescription(description);
	}
	
	public void setId(int id)
	{
		summary.setId(id);
	}
	
	public int getId()
	{
		return summary.getId();
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
		String desc = "WALKROUTE Title: " + summary.getTitle() + " Description: " 
				+ summary.getDescription() + "\n";
		for(int i=0; i<stages.size(); i++)
		{
			desc += stages.get(i).toString() + "\n";
		}
		return desc;

	}
	
	public String toXML()
	{
		DecimalFormat format=new DecimalFormat("000");
		String desc = "<gpx><trk><name>" + summary.getTitle() + "</name><desc>" + 
					summary.getDescription() + "</desc><number>"+summary.getId()+
				"</number><extensions><distance>" + getDistance() + "</distance></extensions>";
		
		
		desc += "<trkseg>";
		for(int i=0; i<points.size(); i++)
		{
			desc += "<trkpt lat='" + points.get(i).y+"' lon='" + points.get(i).x+"'>";
			if(points.get(i).timestamp>=0L)
				desc += "<time>" + points.get(i).getGPXTimestamp()+"</time>";
			desc += "</trkpt>";
		}
		desc+="</trkseg></trk>";
		for(int i=0; i<stages.size(); i++)
		{
			desc += "<wpt lat='" + stages.get(i).start.y+"' lon='" + stages.get(i).start.x+"'>" +
					"<name>" + format.format(stages.get(i).id)+"</name><desc>"+
					stages.get(i).description+"</desc><type>stage</type></wpt>";
		}
		desc+="</gpx>";
		return desc;

	}
	public String getTitle()
	{
		return summary.getTitle();
	}
	
	public String getDescription()
	{
		return summary.getDescription();
	}
	
	public ArrayList<TrackPoint> getPoints()
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
			dist = Algorithms.haversineDist(lonlat.x,lonlat.y,startAsLL.x,startAsLL.y);
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
	
	public void clear()
	{
		points.clear();
		stages.clear();
	}
	
	public Walkroute simplifyDouglasPeucker(double distMetres)
	{
	    Walkroute simplified = new Walkroute (summary.getTitle(), summary.getDescription());
	    for(int i=0; i<stages.size(); i++)
	        simplified.addStage(stages.get(i).start, stages.get(i).description);
	    
	    TrackPoint[] pts = new TrackPoint[points.size()];
	    points.toArray(pts);
	    
	    TrackPoint[] ptsSimp = (TrackPoint[])Algorithms.douglasPeuckerNonRecursive(pts, distMetres);
	    
	    for(int i=0; i<ptsSimp.length; i++)
	        simplified.addPoint(ptsSimp[i]);
	     
	    return simplified;
	}
	
	public double getDistance()
	{
		// Calculate distance if there are points, otherwise return pre-loaded distance (e.g from source XML)
		if(points.size()>1)
		{
			double dist = 0.0;
			Point lastPointLL, pointLL=null;
		
			for(int i=1; i<points.size(); i++)
			{
				lastPointLL=pointLL!=null ? pointLL: (proj==null ?points.get(i-1):proj.unproject(points.get(i-1)));
				pointLL = (proj==null) ? points.get(i): proj.unproject(points.get(i));
				dist += Algorithms.haversineDist(lastPointLL.x, lastPointLL.y, pointLL.x, pointLL.y);
			}
			System.out.println("getDistance(): calculated distance: " + dist/1000);
			return dist / 1000;
			
		}
		System.out.println("getDistance(): presetDistance: " + summary.getDistance());
		return summary.getDistance();
	}
}
