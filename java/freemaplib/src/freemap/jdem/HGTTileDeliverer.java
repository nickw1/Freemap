package freemap.jdem;

import freemap.data.Point;
import freemap.data.Projection;
import freemap.datasource.DataInterpreter;
import freemap.datasource.DataSource;
import freemap.datasource.TiledData;

public class HGTTileDeliverer extends freemap.datasource.CachedTileDeliverer {

	int demWidth, demHeight;
	double demRes;
	
	public HGTTileDeliverer(String name,DataSource ds, DataInterpreter interpreter,int tileWidth,int tileHeight,
            Projection proj, int demWidth, int demHeight, double demRes,String cachedir)
	{
	    this(name,ds,interpreter,tileWidth,tileHeight,proj,demWidth,demHeight,demRes,cachedir,1.0);
	}
	
	public HGTTileDeliverer(String name,DataSource ds, DataInterpreter interpreter,int tileWidth,int tileHeight,
			Projection proj, int demWidth, int demHeight, double demRes,String cachedir, double multiplier)
	{
		super(name,ds,interpreter,tileWidth,tileHeight,proj,cachedir,multiplier);
		this.demWidth = demWidth;
		this.demHeight = demHeight;
		this.demRes = demRes;
	}

	protected TiledData dataWrap(Point origin,Object rawData)
	{
		if(rawData!=null)
		{
		    
			DEM dem = new DEM(new Point(origin.x/tileMultiplier, origin.y/tileMultiplier),demWidth,demHeight,
			        demRes,proj);
		
			dem.setHeights((int[])rawData);
			
			return dem;
		}
		return null;
	}
}
