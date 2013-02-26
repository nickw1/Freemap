package freemap.jdem;

import java.util.HashMap;

import freemap.data.Point;
import freemap.data.Projection;
import freemap.datasource.DataInterpreter;
import freemap.datasource.DataSource;
import freemap.datasource.TiledData;

public class HGTTileDeliverer extends freemap.datasource.TileDeliverer {

	int demWidth, demHeight;
	double demRes;
	
	public HGTTileDeliverer(String name,DataSource ds, DataInterpreter interpreter,int tileWidth,int tileHeight,
			Projection proj, int demWidth, int demHeight, double demRes,String cachedir)
	{
		super(name,ds,interpreter,tileWidth,tileHeight,proj,cachedir);
		this.demWidth = demWidth;
		this.demHeight = demHeight;
		this.demRes = demRes;
	}

	
	
	protected TiledData dataWrap(Point origin,Object rawData)
	{
		if(rawData!=null)
		{
		   
			DEM dem = new DEM(origin,demWidth,demHeight,demRes,proj);
		
			dem.setHeights((int[])rawData);
			
			return dem;
		}
		return null;
	}
	
}
