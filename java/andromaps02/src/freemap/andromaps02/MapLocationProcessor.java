package freemap.andromaps02;








import android.os.Bundle;
import android.graphics.drawable.Drawable;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import android.location.LocationProvider;


import org.mapsforge.core.model.LatLong;

// Role: to receive a location and manage location provider updates, show the "my location" marker,
// manage "waiting for GPS" dialogs and forward the location on to a LocationReceiver which can do 
// application-specific processing.

public class MapLocationProcessor 
{
	
	LocationDisplayer displayer;
	Drawable icon;
	//ProgressDialog gpsWaitingDialog;
	Context ctx;
	boolean gpsWaiting;
	Toast toast;
	
	public interface LocationDisplayer
	{
		public void setLocationMarker(LatLong p);
		public void addLocationMarker();
		public void moveLocationMarker(LatLong p);
		public void removeLocationMarker();
		public boolean isLocationMarker();
	}
	
	public interface LocationReceiver
	{
		public void receiveLocation(double lon, double lat, boolean refresh);
		public void noGPS();
	}
	
	LocationReceiver receiver;
	boolean isUpdating;
	
	public MapLocationProcessor(LocationReceiver processor,Context ctx,
			LocationDisplayer displayer)
	{
		
		this.displayer = displayer;
		this.receiver = processor;
		this.ctx=ctx;
	}
	
	
	
	public void startUpdates()
	{
		if(!isUpdating)
		{
			isUpdating=true;
			Log.d("OpenTrail","MapLocationProcessor.startUpdates()");
			
			if(displayer.isLocationMarker())
			{
				displayer.addLocationMarker();
			}
		}
	}
	
	public void stopUpdates()
	{
		Log.d("OpenTrail","MapLocationProcessor.stopUpdates()");
		
		if(displayer.isLocationMarker())
		{
			displayer.removeLocationMarker();
		}
		isUpdating=false;
		cancelGPSWaiting();
	}
	
	public void onLocationChanged(double lon, double lat)
	{
		onLocationChanged(lon,lat,false);
	}
	
	public void onLocationChanged(double lon, double lat, boolean refresh)
	{
		Log.d("OpenTrail", "broadcastreceiver: location=" + lon+","+lat);
		LatLong p = new LatLong(lat,lon);
		
		if(!displayer.isLocationMarker())
			displayer.setLocationMarker(p);
		else
			displayer.moveLocationMarker(p);
		
		cancelGPSWaiting();
		receiver.receiveLocation(lon,lat,refresh);	
	}
	
	public void onProviderEnabled(String provider)
	{
		showGpsWaiting("Waiting for GPS");
	}

	public void onProviderDisabled(String provider)
	{
		hideLocationMarker();
    	cancelGPSWaiting();
    	receiver.noGPS();
	}

	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		switch(status)
		{
			case LocationProvider.OUT_OF_SERVICE:
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				hideLocationMarker();
				receiver.noGPS();
	    		showGpsWaiting("Waiting for GPS");
				break;
				
			case LocationProvider.AVAILABLE:
				showLocationMarker();
				cancelGPSWaiting();
				break;
		}
	}
	
	private void showLocationMarker()
	{
		if(displayer.isLocationMarker())
			displayer.addLocationMarker();
	}

	private void hideLocationMarker()
	{
		if(displayer.isLocationMarker())
			displayer.removeLocationMarker();
	} 
   
    public void showGpsWaiting(String msg)
    {
    	if(!gpsWaiting)
    	{
    		gpsWaiting=true;
    		toast=Toast.makeText(ctx, "Waiting for GPS...", Toast.LENGTH_LONG);
    		toast.show();
    	}
    }
    
    public void cancelGPSWaiting()
    {
    	gpsWaiting=false;
    	if(toast!=null)
    		toast.cancel();
    }
}