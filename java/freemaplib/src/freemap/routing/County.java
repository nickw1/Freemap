package freemap.routing;

/**
 * Created by nick on 04/07/15.
 */

import java.util.ArrayList;
import freemap.data.Point;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

public class County {

    String name;
    ArrayList<Point> polygon;
    Point sw,ne;


    class Segment
    {
        Point north, south;
    }

    public County(String name)
    {
        polygon = new ArrayList<Point>(); this.name=name;
        sw=new Point(181, 91);
        ne=new Point(-181, -91);
    }

    public void read(String filename) throws IOException
    {
        BufferedReader in = new BufferedReader(new FileReader(filename));
        String line;
        int nPolygons=0;

        while((line = in.readLine())!=null)
        {
            if(name==null)
                name = line;
            else if (line.matches("^\\d+$"))
            {
            	
                // only parse if there is one polygon
                nPolygons = Integer.parseInt(line);
            }
            else if (!line.equals("END") && nPolygons==1)
            {
                String[] lonLat = line.trim().split("\\s+");
                Point p = new Point (Double.parseDouble(lonLat[0]), Double.parseDouble(lonLat[1]));
                if(p.x<sw.x)
                	sw.x=p.x;
                else if (p.x>ne.x)
                	ne.x=p.x;
                if(p.y<sw.y)
                	sw.y=p.y;
                else if (p.y>ne.y)
                	ne.y=p.y;
                polygon.add(p);
            }
        }
    }

    public boolean pointWithin(Point lonLat)
    {
    	if(lonLat.x<sw.x || lonLat.x>ne.x || lonLat.y<sw.y|| lonLat.y>ne.y)
    		return false;
        ArrayList<Segment> segs = findLineSegs(lonLat);
        return getIntersections (lonLat, segs) % 2 == 1;
    }

    private ArrayList<Segment> findLineSegs (Point p)
    {
        ArrayList<Segment> segs = new ArrayList<Segment>();
        
        for(int i=0; i<polygon.size()-1; i++)
        {
        	Segment s= new Segment();
            if(polygon.get(i).y < p.y && polygon.get(i+1).y > p.y)
            {
                s.north = polygon.get(i+1);
                s.south = polygon.get(i);
                segs.add(s);
            }
            else if (polygon.get(i).y > p.y && polygon.get(i+1).y < p.y)
            {
                s.north = polygon.get(i);
                s.south = polygon.get(i+1);
                segs.add(s);
            }
        }
        return segs;
    }

    // Cast a ray to the east of the point
    private int getIntersections (Point p, ArrayList<Segment> segs)
    {
        Point north, south;
        double prop;
        double lonIntersection;
        int nIntersections = 0;

        for(int i=0; i<segs.size(); i++)
        {
            north = segs.get(i).north;
            south = segs.get(i).south;
            prop = (p.y - south.y) / (north.y - south.y);
            lonIntersection = south.x + prop*(north.x-south.x);
          
            if(lonIntersection > p.x)
                nIntersections++;           
        }
        return nIntersections;
    }

    public String getName()
    {
        return name;
    }
    
    public boolean equals (County other)
    {
    	return other.getName().equals(name);
    }
}
