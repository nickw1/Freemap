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

    abstract class FeatureIterator
    {
    	Iterator<Map.Entry<String, Tile>> tileIterator;
        FreemapDataset curTile;
        
        FeatureIterator()
        {
        	tileIterator = osmTiles.entrySet().iterator();
        	if(tileIterator.hasNext())
            {
                curTile= (FreemapDataset)(tileIterator.next().getValue().data);
                initFeatureIterator();
               
            }
        }
        
        public Feature next() 
        {
            if(!nullFeatureIterator() && featureIteratorHasNext())
                return nextFeature();
            else if (tileIterator.hasNext())
            {
            	Map.Entry<String, Tile> e = tileIterator.next();
            	//System.out.println("\n*** NEW TILE: " + e.getKey());
                curTile = (FreemapDataset)e.getValue().data;
                initFeatureIterator();
                if(featureIteratorHasNext())
                	return nextFeature();
                else
                	return next(); // recursively call for next tile
            }
            else
                return null;
        }
        
        abstract void initFeatureIterator();
        abstract Feature nextFeature();
        abstract boolean nullFeatureIterator();
        abstract boolean featureIteratorHasNext();
        
    }
    
    public class WayIterator extends FeatureIterator
    {
        Iterator<Way> featureIterator;


        void initFeatureIterator()
        {
        	 featureIterator = curTile.wayIterator();
        }
        
        Feature nextFeature()
        {
        	return featureIterator.next();
        }
        
        boolean nullFeatureIterator()
        {
        	return featureIterator==null;
        }
        
        boolean featureIteratorHasNext()
        {
        	return featureIterator.hasNext();
        }
    }

    public class POIIterator extends FeatureIterator
    {
        Iterator<Long> featureIterator;


        void initFeatureIterator()
        {
        	featureIterator = curTile.poiIterator();
        }

        Feature nextFeature()
        {
        	return curTile.getPOIById(featureIterator.next());
        }
        
        boolean nullFeatureIterator()
        {
        	return featureIterator==null;
        }
        
        boolean featureIteratorHasNext()
        {
        	return featureIterator.hasNext();
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
    
    public POIIterator poiIterator()
    {
    	return new POIIterator();
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
		double lon=-1.40052532, lat = 50.90760112;
		
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
			 /*
			 OSMTiles.WayIterator i = tiles.wayIterator();
			 
			 Way w = (Way)i.next();
			 while (w!=null)
			 { 
				 System.out.print(w.getValue("name")==null?"":w.getValue("name")+","); 
				 w = (Way)i.next();
			 }
			 
			 System.out.println("\n");
			 */
			 OSMTiles.POIIterator ii = tiles.poiIterator();
			 System.out.println("POIs...");
			 POI p = (POI)ii.next();
			
			 while (p!=null)
			 { 
				 System.out.print(p.getValue("name")==null?"":p.getValue("name")+","); 
				 p = (POI)ii.next();
			 }
			 
			 System.out.println("\n");
			 System.out.println(formatter.getFeatureTypes());
			 TestVisitor v = new TestVisitor();
		
			// tiles.operateOnNearbyWays(v, new Point(-0.72, 51.05), 5000.0);
			
			 
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	
	}
	
}
