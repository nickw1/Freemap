package freemap.datasource;

import java.util.HashMap;

import freemap.data.GoogleProjection;
import freemap.data.Point;
import freemap.data.Projection;

import freemap.jdem.HGTDataInterpreter;
import freemap.jdem.DEMSource;
import freemap.jdem.HGTTileDeliverer;
import freemap.jdem.DEM;
import java.util.Scanner;
import java.util.Map;
import java.util.Set;

import java.io.IOException;
import java.io.File;

//The BaseTileDeliverer/CachedTileDeliverer system is intended to replace the plain TileDeliverer
//to allow for loading from a database. However TileDeliverer still in use.

public abstract class BaseTileDeliverer {
	
	protected int tileWidth, tileHeight;
	protected Point curPos, prevPos;
	
	protected HashMap<String,TiledData> data;
	
	protected Projection proj;
	protected String name;
	
	

	public BaseTileDeliverer(String name,int tileWidth,int tileHeight,Projection proj)
	{
		data=new HashMap<String,TiledData>();
		
		this.tileWidth=tileWidth;
		this.tileHeight=tileHeight;
		this.proj=proj;
		this.name=name;
	}
	
	public TiledData update(Point lonLat) throws Exception
    {
        Point newPos = proj.project(lonLat);
        TiledData curData=null;
        if(curPos==null || isNewObject(curPos,newPos))
        {   
            curPos = newPos;
            Point origin = getOrigin(newPos);
            
            Tile t = doUpdate(origin);
            curData = t.data;
        }
        else
        {
            curPos = newPos;    
        }
        
        // this is just arbitrary data
        return curData;
    }
	
	
	
	public TiledData updateSurroundingTiles(Point lonLat) throws Exception
    {
	    HashMap<String,Tile> updatedData = doUpdateSurroundingTiles(lonLat);
	        
	    Point curOrigin = getOrigin(curPos);
	    return (updatedData.get(""+(int)curOrigin.x+"." + (int)curOrigin.y)!=null)?
	                updatedData.get(""+(int)curOrigin.x+"." + (int)curOrigin.y).data : null;
    }
	
	
	// 24/02/13 now returns an array of Tiles.
    // This is to allow external manipulation of data e.g. applying a DEM then saving the data, where the
    // DEM-applied data is cached (such as in hikar)
    public HashMap<String,Tile> doUpdateSurroundingTiles(Point lonLat) throws Exception
    {
        HashMap<String,Tile> updatedData = new HashMap<String,Tile>();
        Point newPos = proj.project(lonLat);
        
        Point curOrigin = null;
        int i=0;
        if(curPos==null || isNewObject(curPos,newPos))
        {   
            curPos = newPos;
            Point origin = getOrigin(newPos);
            //System.out.println("update calling doUpdate");
            for(int row=-1; row<=1; row++)
            {
                for(int col=-1; col<=1; col++)
                {
                    curOrigin = new Point(origin.x+col*tileWidth,
                                    origin.y+row*tileHeight);
                    
                    Tile t  = doUpdate(curOrigin);
                    String key = ""+(int)curOrigin.x+"." + (int)curOrigin.y;
                    
                    updatedData.put(key, t);
                } 
            }
            //System.out.println("done");
        }
        else
        {
            //System.out.println("No change in tile");
            curPos = newPos;    
        }
        return updatedData;
    }
	
	// sets position without downloading data
	// e.g. after a forceDownload()
	public void setPosition(Point lonLat)
	{
		curPos = proj.project(lonLat);
	}
	
	
	
	protected Tile doUpdate(Point origin) throws Exception
	{
		TiledData curData=null;
		String key = "" + ((int)origin.x)+"."+((int)origin.y);
		
		if(!data.containsKey(key))
		{
		   
			
				
			curData = getDataFromSource(origin); // dataWrap(origin,dataSource.getData(origin,interpreter));
				// file/web sources: It is assumed the data is in standard 4326 and we 
				// reproject on the client side. This is because it's a bit of
				// a pain to reproject into arbitrary projections server side
				// due to lack of a PHP Proj.4 library, whereas there is one for Java.
				//System.out.println("Reprojecting to " + proj);
				
			
			
			//System.out.println("Adding to data with the key: " + key);
			data.put(key,curData);
		}
		else
		{
			curData=data.get(key);
		}
		//System.out.println("Returning curData");
		return new Tile(origin, curData, false);
	}

	public boolean needNewData(Point p)
	{
		return curPos==null || isNewObject(curPos,proj.project(p));
	}
	
	protected boolean isNewObject(Point oldPos, Point newPos)
	{
		Point bottomLeftOld = getOrigin(oldPos), bottomLeftNew = getOrigin(newPos);
		return !(bottomLeftOld.equals(bottomLeftNew));
	}
	
	protected Point getOrigin(Point p)
	{
		return (p==null) ? null:new Point(Math.floor(p.x/tileWidth)*tileWidth,Math.floor(p.y/tileHeight)*tileHeight);
	}
	
	public TiledData getData()
	{
		if(curPos!=null)
		{
			Point bottomLeft = getOrigin(curPos);
			String key = "" + ((int)bottomLeft.x)+"."+((int)bottomLeft.y);
			return data.get(key);
		}
		return null;
	}
	
	public Set<Map.Entry<String,TiledData> > getAllTiles()
	{
		return data.entrySet();
	}
	
	protected TiledData dataWrap(Point origin,Object rawData)
	{
		return (TiledData)rawData;
	}
	
	public TiledData getAllData()
	{
		FreemapDataset allData = new FreemapDataset();
		allData.setProjection(proj);
		Set<Map.Entry<String, TiledData>> entries = data.entrySet();
		for(Map.Entry<String, TiledData> e: entries)
		{
			allData.merge(e.getValue());
		}
		return allData;
	}
	
	// NEW
	// tested
    public TiledData getData(Point p)
    {
        Point bottomLeft = getOrigin(p);
        String key = "" + ((int)bottomLeft.x)+"."+((int)bottomLeft.y);
        return data.get(key);
    }
    
    
	
    // tested
    public int[] getTileID(Point p)
    {
        int[] tileID = new int[2];
        tileID[0]= (int)p.x / tileWidth;
        tileID[1] = -(int)p.y / tileHeight - 1;
        return tileID;
    }
    
    // tested
    public Object tileIDToData(int[] tileID)
    {
        Point origin = new Point();
        origin.x = tileID[0] * tileWidth;
        origin.y = -(tileID[1]+1) * tileHeight;
        return getData(origin);
    }
    // END NEW
    
    public abstract TiledData getDataFromSource(Point origin) throws Exception;
}
