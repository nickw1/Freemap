package freemap.opentrail;

import freemap.data.Point;
import freemap.datasource.FreemapDataset;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
			Point p = new Point(location.getLongitude(),location.getLatitude());
			
			td.update(p, true, forceWebDownload);
			//td.updateSurroundingTiles(p,true);
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
