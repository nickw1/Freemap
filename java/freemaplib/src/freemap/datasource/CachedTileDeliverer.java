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

// The BaseTileDeliverer/CachedTileDeliverer system is intended to replace the plain TileDeliverer
// to allow for loading from a database. However TileDeliverer still in use.

public class CachedTileDeliverer extends BaseTileDeliverer {
	
	
	DataSource dataSource;
	
	DataInterpreter interpreter;
	
	String cachedir;
	
	private boolean cacheData, forceReload, reprojectCachedData;
	

	public CachedTileDeliverer(String name,DataSource ds, DataInterpreter interpreter,int tileWidth,int tileHeight,
							Projection proj,String cachedir)
	{
	    super (name, tileWidth, tileHeight, proj);
		dataSource=ds;
		this.interpreter=interpreter;	
		this.cachedir=cachedir;
		
		cacheData = true;
		forceReload = false;
		reprojectCachedData = true;
	}
	
	public void setCache(boolean cache)
	{
	    cacheData = cache;
	}
	
	public void setForceReload(boolean fr)
	{
	    forceReload = fr;
	}
	
	public void setReprojectCachedData(boolean rp)
	{
	    reprojectCachedData = rp;
	}
		
	public TiledData updateSurroundingTiles(Point lonLat) throws Exception
    {
	    
	        
	        
	        HashMap<String,Tile> updatedData = doUpdateSurroundingTiles(lonLat);
	        
	        Point curOrigin = getOrigin(curPos);
	        
	      
            
	        return (updatedData.get(""+(int)curOrigin.x+"." + (int)curOrigin.y)!=null)?
	                updatedData.get(""+(int)curOrigin.x+"." + (int)curOrigin.y).data : null;
	        
    }
	
	
	
	protected String getCacheFile()
	{
		return getCacheFile(curPos);
	}
	
	
	protected String getCacheFile(Point p)
	{
		Point origin=getOrigin(p);
		String key="" + ((int)origin.x)+"."+((int)origin.y);
		return cachedir+"/"+name+"."+key;
	}
	
	
	protected Tile doUpdate(Point origin) throws Exception
	{
		TiledData curData=null;
		String key = "" + ((int)origin.x)+"."+((int)origin.y);
		
		if(!data.containsKey(key))
		{
		   
			String cachefile=cachedir+"/"+name+"."+key;
			
			if(cachedir!=null && isCache(cachefile) && !forceReload)
			{
				curData = loadFromCache(cachefile,origin);
			}
			else
			{
				
				curData = getDataFromSource(origin, cacheData ? cachefile: null);
				
				// It is assumed the data is in standard 4326 and we 
				// reproject on the client side. This is because it's a bit of
				// a pain to reproject into arbitrary projections server side
				// due to lack of a PHP Proj.4 library, whereas there is one for Java.
				
				curData.reproject(proj);
			}
			
			
			data.put(key,curData);
		}
		else
		{
			curData=data.get(key);
		}
		
		return new Tile(origin,curData,isCache(cachedir+"/"+name+"."+key));
		
	}
	
	
	// 220913 deleted all the cache() and cacheByKey() methods, I don't think we need
	// them anymore, they are in TileDeliverer in case they turn out to be needed...
	
	// we need these two to do later caching e.g. after applying a DEM
	
    public void cache(TiledData data,String cachefile) throws Exception
    {
      
        if(cachedir!=null && data!=null)
        {   
          
            data.save(cachefile);   
        }
    }
    
    public void cacheByKey(TiledData data, String key) throws Exception
    {
        cache(data,cachedir+"/"+name+"."+key);
    }
	
	protected TiledData loadFromCache(String cachefile,Point origin) throws Exception
	{
		TiledData curData = null;
		
		FileDataSource ds = new FileDataSource(cachefile);
		curData = dataWrap(origin,ds.getData(interpreter));
		
		
		// 220913 We now need to reproject cached data as we are dumping the data straight to cache
		// on loading from the web, unless the data was cached later (e.g. applying a DEM)
		if(reprojectCachedData)
		    curData.reproject(proj);
		return curData;
	}
	
	public boolean isCache()
	{
		return curPos!=null && isCache(getCacheFile());
	}
	
	public boolean isCache(Point lonLat)
	{
		String cacheFile=getCacheFile(proj.project(lonLat));
		return new File(cacheFile).exists();
	}
	
	public boolean isCache(String cachefile)
	{
		Point origin = getOrigin(curPos);
		return (origin==null) ? false: new File(cachefile).exists();
	}

	public void forceDownload(Point bottomLeft, Point topRight) throws Exception
	{
		Point blProjected = proj.project(bottomLeft);
		blProjected.x = Math.floor(blProjected.x/tileWidth) * tileWidth;
		blProjected.y = Math.floor(blProjected.y/tileHeight) * tileHeight;
		Point trProjected = proj.project(topRight);
		trProjected.x = Math.floor(trProjected.x/tileWidth) * tileWidth;
		trProjected.y = Math.floor(trProjected.y/tileHeight) * tileHeight;
		Point curPoint = new Point(blProjected.x,blProjected.y);
		while((int)curPoint.x <= (int)trProjected.x)
		{
			
			curPoint.y = blProjected.y;
			while((int)curPoint.y <= (int)trProjected.y)
			{
				doUpdate(curPoint);
				curPoint.y += tileHeight;
			}
			curPoint.x += tileWidth;
		}
	}
	
	public TiledData getDataFromSource (Point origin) throws Exception
	{
	    return getDataFromSource (origin, null);
	}
	
    public TiledData getDataFromSource (Point origin, String cacheFile) throws Exception
    {
        return dataWrap(origin,dataSource.getData(origin,interpreter,cacheFile));
    }
}
