package freemap.routing;

import freemap.datasource.CachedTileDeliverer;
import freemap.datasource.FreemapDataHandler;
import freemap.datasource.FreemapFileFormatter;
import freemap.datasource.OSMTiles;
import freemap.datasource.WebDataSource;
import freemap.datasource.XMLDataInterpreter;


/**
 * Created by nick on 07/07/15.
 */


import freemap.data.GoogleProjection;
import freemap.data.Point;
import freemap.data.Way;
import freemap.data.Algorithms;
import java.util.ArrayList;



public class JunctionManager  {

    OSMTiles dataset;
    double distance;
    ArrayList<Way> storedWays;

    public JunctionManager()
    {
    	this(5);
    }
   
    public JunctionManager(double distance)
    {
    	this.distance=distance;
    }
    
    public void setDataset(OSMTiles d)
    {
        dataset=d;
    }

    public boolean hasDataset()
    {
    	return dataset!=null;
    }
    
    public boolean isJunction(Point lonLat)
    {
    	return isJunction(lonLat, false);
    }
    
    private boolean isJunction(Point lonLat, boolean storeWays)
    {

        int nNearWays = 0, nTerminals = 0;

        OSMTiles.WayIterator iWay = dataset.wayIterator();
        Way w = (Way)iWay.next();
        double lowestSoFar;
        boolean isTerminal;
        
        if(storeWays)
        {
        	if(storedWays==null)
        		storedWays = new ArrayList<Way>();
        	else
        		storedWays.clear();
        }
        
        while(w!=null)
        {
        	lowestSoFar = Double.MAX_VALUE;
        	isTerminal = false;
            
            for(int i=0; i<w.nPoints(); i++)
            {
            	Point p = w.getUnprojectedPoint(i);
            	
            	double curDist = Algorithms.haversineDist(lonLat.x, lonLat.y, p.x, p.y);
            	
                if(curDist<distance && curDist<lowestSoFar)
                {
                	System.out.println("Updating: Found a nearby way: ID: " + w.getId() + " type: " + w.getValue("highway") + " point number: " 
                				+ i + " of " + w.nPoints());
                    isTerminal = (i==0 || i==w.nPoints()-1);
                    lowestSoFar = curDist;
                }
            }
            
            if(lowestSoFar!=Double.MAX_VALUE)
            {
            	nNearWays++;
            	nTerminals += isTerminal ? 1:0;
            	
            	if(storeWays)
            		storedWays.add(w);
            }
            w =(Way)iWay.next();
        }
        System.out.println("Found " + nNearWays + " ways with " + nTerminals + " terminals.");
        return nNearWays>=3 || (nNearWays==2 && nTerminals<2);
    }
    
    public Point getJunction (Point p)
    {
    	// Are we near a junction?
    	if(isJunction(p, true) && storedWays.size()>=2)
    	{
    		// If so we need to find the actual position of the junction
    		Way testWay = storedWays.get(0), otherWay = storedWays.get(1);
    		
    		// Take the first way, and find out which point is closest to the second.
    		// We don't really need to worry about subsequent ways as the point closest to the 2nd will give us our junction.
    		//return storedWays.get(0).closestPointToUnprojected(storedWays.get(1));
    		
    		double lowestDist = Double.MAX_VALUE;
    		Point point = null, curPoint;
    		
    		
    		for(int i=0; i<testWay.nPoints(); i++)
    		{
    			curPoint = testWay.getUnprojectedPoint(i);
    			double distToOtherWay = otherWay.haversineDistanceTo(curPoint), distFromTestPoint = 
    					Algorithms.haversineDist(p.x, p.y, curPoint.x, curPoint.y);
    			
    			// The point is only counted if it is the closest so far AND below the threshold distance to the test point
    			// (this is to prevent finding the "wrong" point of a looped way)
    			if (distToOtherWay < lowestDist && distFromTestPoint<distance)
    			{
    				lowestDist = distToOtherWay;
    				point = curPoint;
    				
    			}
    		}
    		return point;
    	}
    	return null;
    }
    
    public ArrayList<Way> getStoredWays()
    {
    	return storedWays;
    }
    
    public static void main (String[] args)
    {
    	// 51.0460 -0.7343 : 1263/66
    	// 51.0389 -0.7349 : 1265/66
    	// 51.0473 -0.7340 : Park House Farm drive end
    	// 51.0504 -0.7078 : 2005/06
    	// 51.0495 -0.7167 : 2005 service end
    	// 51.0498 -0.7143 : loop track west end
    	// 51.0495 -0.7117 : loop track east end  	
    		
    		double lon = -0.7343, lat=51.0460;
    		
    		FreemapFileFormatter formatter = new FreemapFileFormatter("epsg:3857");
    		formatter.setScript("bsvr2.php");
            formatter.selectWays("highway");
            formatter.selectPOIs("place,amenity,natural");
            
    		WebDataSource dataSource2=new WebDataSource("http://www.free-map.org.uk/fm/ws/", 
    				formatter);
    		CachedTileDeliverer deliverer2=new CachedTileDeliverer("osm",dataSource2, new XMLDataInterpreter
    				(new FreemapDataHandler()),5000,5000,new GoogleProjection(),"/home/nick/cache");
    	
    		try
    		{	
    			 OSMTiles tiles = new OSMTiles(deliverer2.doUpdateSurroundingTiles(new Point(lon,lat)));
    			 JunctionManager jMgr = new JunctionManager(20);
    			 jMgr.setDataset(tiles);
    			 Point junc = jMgr.getJunction(new Point(lon,lat));
    			 System.out.println("Is this a junction? "+ (junc!=null));
    			 if(junc!=null)
    				 System.out.println("Junction: " + junc);
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    	
    	}
    	
    	
    
}
