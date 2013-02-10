package freemap.opentrail;

import freemap.data.Point;
import freemap.datasource.FreemapDataset;
import android.content.Context;
import freemap.datasource.TileDeliverer;
import android.util.Log;
import freemap.andromaps.DataCallbackTask;
import org.mapsforge.core.GeoPoint;

public class DownloadPOIsTask extends DataCallbackTask<Void,Void>  {

	
	
	
	TileDeliverer td;
	boolean forceWebDownload;
	GeoPoint location;
	
	public DownloadPOIsTask(Context ctx, TileDeliverer td, DataReceiver receiver, boolean showDialog,
								boolean forceWebDownload, GeoPoint location)
	{
		super(ctx,receiver);
		setShowProgressDialog(showDialog);
		setShowDialogOnFinish(showDialog);
		setDialogDetails("Downloading...","Downloading POIs...");
		this.td=td;
		this.forceWebDownload=forceWebDownload;
		this.location=location;
	}
	

	public String doInBackground(Void... unused)
	{
		
		
		try
		{
			Point p = new Point(location.getLongitude(),location.getLatitude());
			td.updateSurroundingTiles(p,true,forceWebDownload);
			setData((FreemapDataset)td.getAllData());
			Log.d("OpenTrail","done");
			return "Successfully downloaded";
		}
		catch(Exception e)
		{
			return e.getMessage();
		}
	}
	
	public void receive(Object data)
	{
		if(receiver!=null)
			((DataReceiver)receiver).receivePOIs((FreemapDataset)data);
	}
}
