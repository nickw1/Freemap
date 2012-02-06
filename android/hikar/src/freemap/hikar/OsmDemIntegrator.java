package freemap.hikar;

import freemap.data.Projection;
import freemap.datasource.TileDeliverer;
import freemap.datasource.WebDataSource;
import freemap.jdem.DEMSource;
import freemap.jdem.HGTDataInterpreter;
import freemap.jdem.HGTTileDeliverer;
import freemap.datasource.FreemapFileFormatter;
import freemap.data.Point;
import freemap.datasource.XMLDataInterpreter;
import freemap.data.GoogleProjection;
import freemap.datasource.FreemapDataHandler;
import freemap.jdem.DEM;
import java.util.Set;
import java.util.Map;
import freemap.datasource.OSMRenderData;
import android.util.Log;
import android.os.Environment;


public class OsmDemIntegrator {

	TileDeliverer osm, hgt;
	Projection tilingProj;
	
	public OsmDemIntegrator(String projID)
	{
		WebDataSource hgtDataSource=new WebDataSource("http://www.free-map.org.uk/downloads/lfp/", 
				new LFPFileFormatter());
		
		WebDataSource osmDataSource = new WebDataSource("http://www.free-map.org.uk/freemap/ws/",
				new FreemapFileFormatter(projID));
	
		Proj4ProjectionFactory factory=new Proj4ProjectionFactory();
		tilingProj = factory.generate(projID);
		hgt=new HGTTileDeliverer("dem",hgtDataSource, new HGTDataInterpreter(101,101,50,
											DEMSource.LITTLE_ENDIAN),
						5000, 5000, tilingProj,101,101,50,
						Environment.getExternalStorageDirectory().getAbsolutePath()+"/opentrail/cache/"+
						tilingProj.getID().toLowerCase().replace("epsg:","")+"/");
		
		osm = new TileDeliverer("osm",osmDataSource, 
					new XMLDataInterpreter(new FreemapDataHandler(factory)),
					5000,5000,
					tilingProj,
					Environment.getExternalStorageDirectory().getAbsolutePath()+"/opentrail/cache/"+
					tilingProj.getID().toLowerCase().replace("epsg:","")+"/");
	}
	
	// ASSUMPTION: the tiling systems for hgt and osm data coincide - which they do here (see constructor)
	public boolean update(Point point) 
	{
		OSMRenderData osmupdated=null;
		
		try
		{
			DEM hgtupdated = (DEM)hgt.update(point,true);
			Log.d("OpenTrail"," DEM returned ");
		
			//Log.d("OpenTrail", hgtupdated.toString());
			Log.d("OpenTrail","Getting OSM data...");
			osmupdated = (OSMRenderData)osm.update(point,false);
			
			if (hgtupdated!=null && osmupdated!=null)
			{
				 Log.d("OpenTrail","hgtupdated and osmupdated not null");
				 if(!osm.isCache())
				 {
					Log.d("OpenTrail","Applying DEM as not cached");
					System.out.println("Applying DEM as not cached");
					//System.out.println("Before updating: osmupdated="+osmupdated);
				 	osmupdated.applyDEM(hgtupdated);
				 	//System.out.println("After updating: osmupdated="+osmupdated);
				 	osm.cache(osmupdated);
				 	Log.d("OpenTrail","Done");
				 }
				 else
					 Log.d("OpenTrail","is cache");
				
			}
		}
		catch(Throwable t)
		{
			Log.e("OpenTrail"," ERROR: " + t);
		}
		boolean b = osmupdated!=null;
		if(b)System.out.println("Returning true");
		return b;
	}
	
	public double getHeight(Point p)
	{
		DEM dem = (DEM) hgt.getData();
		if(dem!=null)
		{
			double h=dem.getHeight(p.x,p.y,tilingProj);
			//Log.d("OpenTrail","Height at: " + p.x+" "+p.y+"="+h+" proj=" + tilingProj);
			return h;
		}
		return -1;
	}
	
	public void forceDownload(Point bottomLeft, Point topRight) throws Exception
	{
		osm.forceDownload(bottomLeft, topRight);
	}
	
	public DEM getCurrentDEM()
	{
		return (DEM)hgt.getData();
	}
	
	public OSMRenderData getCurrentOSMData()
	{
		return (OSMRenderData)osm.getData();
	}
}
