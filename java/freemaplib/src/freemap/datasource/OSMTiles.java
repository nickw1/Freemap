package freemap.datasource;


/**
 * Created by nick on 07/07/15.
 */

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import freemap.data.Way;
import freemap.data.Point;

import freemap.data.*;


public class OSMTiles
{
    HashMap<String, Tile> osmTiles;

    public class WayIterator
    {

        Iterator<Map.Entry<String, Tile>> tileIterator;
        FreemapDataset curTile;
        Iterator<Way> featureIterator;


        WayIterator() 
        {
            tileIterator = osmTiles.entrySet().iterator();
            if(tileIterator.hasNext())
            {
                curTile= (FreemapDataset)(tileIterator.next().getValue().data);
                featureIterator = curTile.wayIterator();
            }
        }

        public Way next() 
        {
            if(featureIterator!=null && featureIterator.hasNext())
                return featureIterator.next();
            else if (tileIterator.hasNext())
            {
            	Map.Entry<String, Tile> e = tileIterator.next();
            	//System.out.println("\n*** NEW TILE: " + e.getKey());
                curTile = (FreemapDataset)e.getValue().data;
                featureIterator = curTile.wayIterator();
                if(featureIterator.hasNext())
                	return featureIterator.next();
                else
                	return next(); // recursively call for next tile
            }
            else
                return null;
        }
    }

    public OSMTiles()
    {
        osmTiles = new HashMap<String, Tile>();
    }

    public OSMTiles(HashMap<String, Tile> data) {
        osmTiles = data;

    }

    public void clear()
    {
        osmTiles.clear();
    }

    public HashMap<String, Tile> tilesAsMap()
    {
        return osmTiles;
    }
    
    public WayIterator wayIterator()
    {
        return new WayIterator();
    }

    public void operateOnNearbyPOIs(FreemapDataset.POIVisitor visitor, Point pointLL, double distanceMetres)
    {
        Iterator<Map.Entry<String, Tile>> iterator = osmTiles.entrySet().iterator();
        while (iterator.hasNext())
            ((FreemapDataset)(iterator.next().getValue().data)).operateOnNearbyPOIs(visitor, pointLL, distanceMetres);
    }

    public void operateOnPOIs(FreemapDataset.POIVisitor visitor)
    {
        Iterator<Map.Entry<String, Tile>> iterator = osmTiles.entrySet().iterator();
        while (iterator.hasNext())
        {
        	System.out.println("operating on POIs for tile");
        
            ((FreemapDataset) (iterator.next().getValue().data)).operateOnPOIs(visitor);
        }
    }

    public void operateOnWays(FreemapDataset.WayVisitor visitor)
    {
        Iterator<Map.Entry<String, Tile>> iterator = osmTiles.entrySet().iterator();
        while (iterator.hasNext())
            ((FreemapDataset) (iterator.next().getValue().data)).operateOnWays(visitor);
    }

    public void operateOnNearbyWays(FreemapDataset.WayVisitor visitor, Point point, double distance)
    {
        Iterator<Map.Entry<String, Tile>> iterator = osmTiles.entrySet().iterator();
        while (iterator.hasNext())
            ((FreemapDataset) (iterator.next().getValue().data)).operateOnNearbyWays(visitor, point, distance);
    }
    

	static class TestVisitor implements FreemapDataset.POIVisitor, FreemapDataset.WayVisitor
	{
		public void visit(Way w)
		{
			System.out.println(w);
		}
		
		public void visit(POI poi)
		{
			System.out.println(poi);
		}
	}
	
	public static void main (String args[])
	{		
		System.out.println("enter lon/lat:");
		
		double lon = -0.72, lat=51.05;
		
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
			 OSMTiles.WayIterator i = tiles.wayIterator();
			 
			 Way w = i.next();
			 while (w!=null)
			 { 
				 System.out.print(w.getValue("name")==null?"":w.getValue("name")+","); 
				 w = i.next();
			 }
			 
			 System.out.println("\n");
			 System.out.println(formatter.getFeatureTypes());
			 TestVisitor v = new TestVisitor();
		
			 tiles.operateOnNearbyWays(v, new Point(-0.72, 51.05), 5000.0);
			
			 
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	
	}
	
}
