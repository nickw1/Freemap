package freemap.opentrail;





import freemap.data.Walkroute;

import freemap.datasource.WalkrouteHandler;

import freemap.datasource.WebXMLSource;


import android.util.Log;
import android.content.Context;
import android.app.ProgressDialog;

import android.location.Location;
import android.os.AsyncTask;

import freemap.andromaps.DataCallbackTask;


public class DownloadWalkrouteTask extends DataCallbackTask<Integer,Void> {
	
	
	
	int returnedIdx;
	
	
	public DownloadWalkrouteTask(Context ctx, DataReceiver receiver)
	{
		super(ctx,receiver);
		setDialogDetails("Downloading...","Downloading walk route...");
	}
	
	public String doInBackground(Integer... idx)
	{
		
		
		try
		{
			WebXMLSource source = new WebXMLSource(
					"http://www.free-map.org.uk/0.6/ws/wr.php?action=get&id="
							+ Shared.walkroutes.get(idx[0]).getId()
							+ "&format=gpx", new WalkrouteHandler());
			Log.d("OpenTrail","URL="+"http://www.free-map.org.uk/0.6/ws/wr.php?action=get&id="
							+ Shared.walkroutes.get(idx[0]).getId()
							+ "&format=gpx");
			Log.d("OpenTrail","hello1");
			Walkroute wr = (Walkroute)source.getData();
			Log.d("OpenTrail","hello2");
			Log.d("OpenTrail","Sent back: data");
			setData(wr);
			returnedIdx = idx[0];
			
			
			
			return "Successfully downloaded walk route";
		} 
		catch (Exception e) 
		{	
			return e.getMessage();	
		}
			
	}	
	
	public void receive(Object obj)
	{
		Log.d("OpenTrail","Received a walkroute: ");
		
		if(receiver!=null)
			((DataReceiver)receiver).receiveWalkroute(returnedIdx,(Walkroute)obj);
		
	}
}

