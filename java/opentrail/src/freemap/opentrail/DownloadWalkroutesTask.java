package freemap.opentrail;

import java.util.ArrayList;


import freemap.data.Walkroute;

import freemap.datasource.WalkroutesHandler;
import freemap.datasource.WebXMLSource;


import android.util.Log;
import android.content.Context;

import freemap.andromaps.DataCallbackTask;


public class DownloadWalkroutesTask extends DataCallbackTask<Void,Void> {


	
	
	public DownloadWalkroutesTask(Context ctx, DataReceiver receiver)
	{
		super(ctx,receiver);
		setDialogDetails("Downloading...","Downloading walk routes...");
	}
	
	public String doInBackground(Void... unused)
	{
		if(Shared.location==null) return "Location unknown";
		
		try
		{
			
				String url = "http://www.free-map.org.uk/0.6/ws/wr.php?action=getByRadius&format=gpx&radius=100&lat="
					+ Shared.location.getLatitude()
					+ "&lon="
					+ Shared.location.getLongitude();
				Log.d("OpenTrail","URL=" + url);
				WebXMLSource xmlsource = new WebXMLSource(url,new WalkroutesHandler());
				setData((ArrayList<Walkroute>) xmlsource.getData());
				
				
				return "Successfully downloaded walk routes";
		} 
		catch (org.xml.sax.SAXException e) 
		{	
			return "sacexxeption:" +e.getMessage();	
		}
		catch(java.io.IOException e)
		{
			return "ioexception:"  + e.getMessage();
		}
			
	}

	public void receive(Object data)
	{
		if(receiver!=null)
			((DataReceiver)receiver).receiveWalkroutes((ArrayList<Walkroute>)data);
	}
}
