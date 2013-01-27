package freemap.opentrail;

import freemap.data.Point;
import freemap.data.Walkroute;
import freemap.datasource.FreemapDataset;

import android.content.Context;
import android.app.ProgressDialog;
import freemap.datasource.TileDeliverer;

import android.os.AsyncTask;
import android.util.Log;

import freemap.andromaps.DataCallbackTask;

public class DownloadPOIsTask extends DataCallbackTask<Void,Void>  {

	
	
	
	TileDeliverer td;
	
	public DownloadPOIsTask(Context ctx, TileDeliverer td, DataReceiver receiver, boolean showDialog)
	{
		super(ctx,receiver);
		setShowProgressDialog(showDialog);
		setShowDialogOnFinish(showDialog);
		setDialogDetails("Downloading...","Downloading POIs...");
		this.td=td;
	}
	

	public String doInBackground(Void... unused)
	{
		if(Shared.location==null) return "Location unknown";
		
		try
		{
			
			Log.d("OpenTrail","doing task");
			Point p = new Point(Shared.location.getLongitude(),Shared.location.getLatitude());
			td.update(p, true);
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
