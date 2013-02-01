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

public class TileDeliverer {
	
	int tileWidth, tileHeight;
	Point curPos, prevPos;
	DataSource dataSource;
	HashMap<String,TiledData> data;
	DataInterpreter interpreter;
	protected Projection proj;
	String name, cachedir;
	
	
	public TileDeliverer(String name,DataSource ds, DataInterpreter interpreter,int tileWidth,int tileHeight,
							Projection proj,String cachedir)
	{
		data=new HashMap<String,TiledData>();
		dataSource=ds;
		this.interpreter=interpreter;
		this.tileWidth=tileWidth;
		this.tileHeight=tileHeight;
		this.proj=proj;
		this.name=name;
		this.cachedir=cachedir;
	}
	
	public TiledData update(Point lonLat, boolean cacheData) throws Exception
	{
		return update(lonLat,cacheData,false);
	}
	
	public TiledData update(Point lonLat, boolean cacheData, boolean forceReload) throws Exception
	{
		Point newPos = proj.project(lonLat);
		TiledData curData=null;
		if(curPos==null || isNewObject(curPos,newPos))
		{	
			curPos = newPos;
			Point origin = getOrigin(newPos);
			//System.out.println("update calling doUpdate");
			curData = doUpdate(origin,cacheData,forceReload);
			//System.out.println("done");
		}
		else
		{
			//System.out.println("No change in tile");
			curPos = newPos;	
		}
		
		
		//System.out.println("Returning from update");
		// this is just arbitrary data
		return curData;
	}
	
	public TiledData updateSurroundingTiles(Point lonLat, boolean cacheData) throws Exception
	{
		return updateSurroundingTiles(lonLat,cacheData,false);
	}
	
	public TiledData updateSurroundingTiles(Point lonLat, boolean cacheData, boolean forceReload) throws Exception
	{
		Point newPos = proj.project(lonLat);
		TiledData curData=null, thisData;
		Point curOrigin = new Point();
		if(curPos==null || isNewObject(curPos,newPos))
		{	
			curPos = newPos;
			Point origin = getOrigin(newPos);
			//System.out.println("update calling doUpdate");
			for(int row=-1; row<=1; row++)
			{
				for(int col=-1; col<=1; col++)
				{
					curOrigin.x = origin.x+col*tileWidth;
					curOrigin.y = origin.y+row*tileHeight;
					thisData = doUpdate(curOrigin,cacheData,forceReload);
					if(row==0 && col==0)
						curData=thisData;
				}
			}
			//System.out.println("done");
		}
		else
		{
			//System.out.println("No change in tile");
			curPos = newPos;	
		}
		return curData;
	}

	
	// sets position without downloading data
	// e.g. after a forceDownload()
	public void setPosition(Point lonLat)
	{
		curPos = proj.project(lonLat);
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
	
	
	protected TiledData doUpdate(Point origin, boolean cacheData, boolean forceReload) throws Exception
	{
		TiledData curData=null;
		String key = "" + ((int)origin.x)+"."+((int)origin.y);
		//System.out.println("key="+key);
		if(!data.containsKey(key))
		{
			String cachefile=cachedir+"/"+name+"."+key;
			//System.out.println("cachefile="+cachefile);
			if(cachedir!=null && isCache(cachefile) && !forceReload)
			{
				curData = loadFromCache(cachefile,origin);
			}
			else
			{
				System.out.println("Loading from web");
				curData = dataWrap(origin,dataSource.getData(origin,interpreter));
				// It is assumed the data is in standard 4326 and we 
				// reproject on the client side. This is because it's a bit of
				// a pain to reproject into arbitrary projections server side
				// due to lack of a PHP Proj.4 library, whereas there is one for Java.
				//System.out.println("Reprojecting");
				curData.reproject(proj);
				if(cacheData)
					cache(curData,cachefile);
			}
			
			//System.out.println("Adding to data with the key: " + key);
			data.put(key,curData);
		}
		else
		{
			curData=data.get(key);
		}
		//System.out.println("Returning curData");
		return curData;
	}
	
	
	
	public void cache() throws Exception
	{
		cache(getData(),getCacheFile());
	}
	
	public void cache(String cachefile) throws Exception
	{
		cache(getData(),cachefile);
	}
	
	public void cache(TiledData data) throws Exception
	{
		cache(data,getCacheFile());
	}
	
	public void cache(TiledData data,String cachefile) throws Exception
	{
		System.out.println("Caching data");
		if(cachedir!=null && data!=null)
		{	
			//System.out.println("Actually caching data");
			data.save(cachefile);	
		}
	}
	
	protected TiledData loadFromCache(String cachefile,Point origin) throws Exception
	{
		TiledData curData = null;
		//System.out.println("Loading from file");
		FileDataSource ds = new FileDataSource(cachefile);
		curData = dataWrap(origin,ds.getData(interpreter));
		//System.out.println("Curdata=" + curData);
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
			System.out.println("Downloading tile: " + curPoint);
			curPoint.y = blProjected.y;
			while((int)curPoint.y <= (int)trProjected.y)
			{
				doUpdate(curPoint,true,false);
				curPoint.y += tileHeight;
			}
			curPoint.x += tileWidth;
		}
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
	
	/*
	public static void main (String args[])
	{

		
		System.out.println("enter lon/lat:");
		Scanner s=new Scanner(System.in);
		
		
		 double lon=s.nextDouble(), lat=s.nextDouble();
		 
		
		
		double lon=-0.72;
		double lat=51.05;
		
		
		WebDataSource dataSource=new WebDataSource("http://www.free-map.org.uk/downloads/lfp/", 
			new LFPFileFormatter());
	
		
		TileDeliverer deliverer=new HGTTileDeliverer("dem",dataSource, new HGTDataInterpreter(101,101,50,
										DEMSource.LITTLE_ENDIAN),
					5000, 5000, new OSGBProjection(),101,101,50,".");
		
		
		try
		{
			deliverer.update(new Point(lon,lat));
			DEM dem = (DEM)(deliverer.getData());
			if(dem!=null)
			{
				System.out.println(dem);
				System.out.println("Height: " + dem.getHeight(lon,lat,null));
				System.out.println("*Got data: " + dem);
			}
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		
		
		
		WebDataSource dataSource2=new WebDataSource("http://www.free-map.org.uk/freemap/ws/", 
				new FreemapFileFormatter("epsg:3785"));
		TileDeliverer deliverer2=new TileDeliverer("osm",dataSource2, new XMLDataInterpreter
				(new FreemapDataHandler()),5000,5000,new GoogleProjection(),".");


		try
		{
			//TiledData data = deliverer2.update(new Point(lon,lat));
			//System.out.println("*Got data: " + deliverer2.getData());
			deliverer2.forceDownload(new Point(16.33, 48.19), 
					new Point(16.4, 48.22));
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	
	}
*/
}
