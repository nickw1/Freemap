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
	
	// in case our projection is WGS84/4326 but we want to use microdegrees for tiles so we
	// can deal in whole numbers
	protected double tileMultiplier; 
	
	public BaseTileDeliverer(String name,int tileWidth,int tileHeight,Projection proj)
	{
		this(name,tileWidth,tileHeight,proj,1.0);
	}
	
	public BaseTileDeliverer(String name,int tileWidth,int tileHeight,Projection proj,double multiplier)
	{
	    data=new HashMap<String,TiledData>();
    
	    this.tileWidth=tileWidth;
	    this.tileHeight=tileHeight;
	    this.proj=proj;
	    this.name=name;
	    this.tileMultiplier = multiplier;
	}
	public TiledData update(Point lonLat) throws Exception
    {
        Point newPos = proj.project(lonLat);
        // IMPORTANT curPos is projected but has no multiplier applied
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
        
        if(curPos==null || isNewObject(curPos,newPos))
        {   
            curPos = newPos;
            System.out.println("newPos=" + newPos + " tileWidth=" + tileWidth + " tileHieght=" + tileHeight +
                        " multiplier=" + tileMultiplier);
            Point origin = getOrigin(newPos);
            System.out.println("origin=" + origin);
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
            
        }
        else
        {
            
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
		   
			
				
			curData = getDataFromSource(origin); 
			data.put(key,curData);
		}
		else
		{
			curData=data.get(key);
		}
		
		return new Tile(origin, curData, false);
	}

	public boolean needNewData(Point lonLat)
	{
		return curPos==null || isNewObject(curPos,proj.project(lonLat));
	}
	
	protected boolean isNewObject(Point oldPos, Point newPos)
	{
		Point bottomLeftOld = getOrigin(oldPos), bottomLeftNew = getOrigin(newPos);
		return !(bottomLeftOld.equals(bottomLeftNew));
	}
	
	protected Point getOrigin(Point p)
	{
		return (p==null) ? null:new Point(Math.floor((p.x*tileMultiplier)/tileWidth)*tileWidth,
		        Math.floor((p.y*tileMultiplier)/tileHeight)*tileHeight);
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
	
	// NEW
	// tested
    public TiledData getData(Point p)
    {
        Point bottomLeft = getOrigin(p);
        String key = "" + ((int)bottomLeft.x)+"."+((int)bottomLeft.y);
        return data.get(key);
    }
        
    public Projection getProjection()
    {
        return proj;
    }
    
    public abstract TiledData getDataFromSource(Point origin) throws Exception;
}
