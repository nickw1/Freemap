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
	
	    return updateSurroundingTiles(lonLat, cacheData, false);
	   
	}
	
	public TiledData updateSurroundingTiles(Point lonLat, boolean cacheData, boolean forceReload) throws Exception
    {
	    
	        
	        HashMap<String,Tile> updatedData = doUpdateSurroundingTiles(lonLat,cacheData,forceReload);
	        
	        Point curOrigin = getOrigin(curPos);
	        return (updatedData.get(""+(int)curOrigin.x+"." + (int)curOrigin.y)!=null)?
	                updatedData.get(""+(int)curOrigin.x+"." + (int)curOrigin.y).data : null;
	        
	    
           
    }
	
	// 24/02/13 now returns an array of Tiles.
	// This is to allow external manipulation of data e.g. applying a DEM then saving the data, where the
	// DEM-applied data is cached (such as in hikar)
	public HashMap<String,Tile> doUpdateSurroundingTiles(Point lonLat, boolean cacheData, boolean forceReload) throws Exception
	{
		HashMap<String,Tile> updatedData = new HashMap<String,Tile>();
	    Point newPos = proj.project(lonLat);
		TiledData thisData;
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
					
					thisData = doUpdate(curOrigin,cacheData,forceReload);
					String key = ""+(int)curOrigin.x+"." + (int)curOrigin.y;
					
					updatedData.put(key,new Tile(curOrigin,thisData,isCache(cachedir+"/"+name+"."+key)));
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
				//System.out.println("Loading from web");
				curData = dataWrap(origin,dataSource.getData(origin,interpreter));
				// It is assumed the data is in standard 4326 and we 
				// reproject on the client side. This is because it's a bit of
				// a pain to reproject into arbitrary projections server side
				// due to lack of a PHP Proj.4 library, whereas there is one for Java.
				//System.out.println("Reprojecting to " + proj);
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
	
	public void cacheByKey(TiledData data, String key) throws Exception
	{
	    cache(data,cachedir+"/"+name+"."+key);
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
    
    /*
    public static void main (String args[])
    {

        
        
        
        double lon=-0.72;
        double lat=51.05;
        
        
        WebDataSource dataSource=new WebDataSource("http://www.free-map.org.uk/downloads/lfp/", 
            new LFPFileFormatter());
    
        
        HGTTileDeliverer deliverer=new HGTTileDeliverer("dem",dataSource, new HGTDataInterpreter(101,101,50,
                                        DEMSource.LITTLE_ENDIAN),
                    5000, 5000, new freemap.proj.OSGBProjection(),101,101,50,".");
        
        
        try
        {
            deliverer.updateSurroundingTiles(new Point(lon,lat), true);
            DEM dem = (DEM)(deliverer.getData());
            if(dem!=null)
            {
                //System.out.println(dem);
                //System.out.println("Height: " + dem.getHeight(-0.72, 51.05, null));
                //System.out.println("*Got data: " + dem);
                Point p1 = new Point(489600,128500), p2 = new Point(494600,128500), p3 = new Point(491600,131500);
                
                
                int[] tileID = deliverer.getTileID(p1),
                        tileID2, tileID3;
                System.out.println(tileID[0]+" "+tileID[1]);
                tileID2 = deliverer.getTileID(p2);
                System.out.println(tileID2[0]+" "+tileID2[1]);
                tileID3 = deliverer.getTileID(p3);
                System.out.println(tileID3[0]+" "+tileID3[1]);
                DEM dem1 = (DEM)deliverer.tileIDToData(tileID),
                        dem2 = (DEM)deliverer.tileIDToData(tileID2),
                        dem3 = (DEM)deliverer.tileIDToData(tileID3),
                        dem4 = (DEM)deliverer.tileIDToData(new int[] {11,22});
                
                System.out.println(dem1.getBottomLeft());
                System.out.println(dem2.getBottomLeft());
                System.out.println(dem3.getBottomLeft());
                System.out.println(((DEM)deliverer.getData(p1)).getBottomLeft());
                System.out.println(((DEM)deliverer.getData(p2)).getBottomLeft());
                System.out.println(((DEM)deliverer.getData(p3)).getBottomLeft());
                System.out.println(dem4);
                
                int[] gp1 = dem1.pointToGridPosition(p1, deliverer.proj);
                System.out.println(gp1[0] + " "+ gp1[1]);
                int[] gp2 = dem1.pointToGridPosition(new Point(485000,129000), deliverer.proj);
                System.out.println(gp2[0] + " "+ gp2[1]);
                System.out.println(dem1.gridPositionToIndex(new int[] {0, 0} ));
                System.out.println(dem1.gridPositionToIndex(new int[] {50, 0} ));
                System.out.println(dem1.gridPositionToIndex(new int[] {0, 1} ));
                System.out.println(dem1.gridPositionToIndex(new int[] {50, 1} ));
                
                int[] z1 = deliverer.getGlobalGridPos(dem1, p1);
                System.out.println("global grid pos for " + p1 + " is " + z1[0] +" " +z1[1]);
                int[] z2 = deliverer.getGlobalGridPos(dem2, p2);
                System.out.println("global grid pos for " + p2 + " is " +z2[0] +" " +z2[1]);
                int[] z4 = deliverer.getGlobalGridPos(dem2, new Point(494600,128600));
                System.out.println(z4[0] +" " +z4[1]);
                int[] z3 = deliverer.getGlobalGridPos(dem3, p3);
                System.out.println("global grid pos for " + p3 + " is " +z3[0] +" " +z3[1]); 
                
                int demw = dem1.getPtWidth()-1, demh = dem1.getPtHeight()-1; // -1 for boundary points
                
                int[] tileOrigin = new int[2], localGridPos = new int[2];
                
                tileOrigin[0] = (int)Math.floor((double)z1[0] /demw) * demw;
                tileOrigin[1] = (int)Math.floor((double)z1[1] / demh) * demh;
                
                System.out.println("Tile origin in global grid points for : " + p1 + " is " + tileOrigin[0] +
                            " " + tileOrigin[1]);
                
                // Use this to get local grid points
                localGridPos[0] = z1[0] - tileOrigin[0];
                localGridPos[1] = z1[1] - tileOrigin[1];
                
                System.out.println("Local grid pos: " + localGridPos[0] +" " + localGridPos[1]);
                
                deliverer.lineOfSight(p1, p3);
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        
        
        
        //WebDataSource dataSource2=new WebDataSource("http://www.free-map.org.uk/freemap/ws/", new FreemapFileFormatter("epsg:3785"));
        //TileDeliverer deliverer2=new TileDeliverer("osm",dataSource2, new XMLDataInterpreter(new FreemapDataHandler()),5000,5000,new GoogleProjection(),".");


        //try
        //{
            //TiledData data = deliverer2.update(new Point(lon,lat));
            //System.out.println("*Got data: " + deliverer2.getData());
            //deliverer2.forceDownload(new Point(16.33, 48.19), new Point(16.4, 48.22));
        //}
        //catch(Exception e)
        //{
        //  System.out.println(e);
        //}
    
    }
    */
}
