package freemap.andromaps;


import org.mapsforge.core.GeoPoint;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.graphics.drawable.Drawable;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;



// Role: to receive a location and manage location provider updates, show the "my location" marker,
// manage "waiting for GPS" dialogs and forward the location on to a LocationReceiver which can do 
// application-specific processing.

public class MapLocationProcessor implements LocationListener
{
	LocationManager mgr;
	LocationDisplayer displayer;
	Drawable icon;
	//ProgressDialog gpsWaitingDialog;
	Context ctx;
	boolean gpsWaiting;
	Toast toast;
	
	public interface LocationDisplayer
	{
		public void addLocationMarker(GeoPoint p);
		public void showLocationMarker();
		public void moveLocationMarker(GeoPoint p);
		public void hideLocationMarker();
		public boolean isLocationMarker();
	}
	
	public interface LocationReceiver
	{
		public void receiveLocation(Location location);
		public void noGPS();
	}
	
	LocationReceiver receiver;
	boolean isUpdating;
	
	public MapLocationProcessor(LocationReceiver processor,Context ctx,LocationDisplayer displayer)
	{
		mgr = (LocationManager)ctx.getSystemService(Context.LOCATION_SERVICE);
		this.displayer = displayer;
		this.receiver = processor;
		this.ctx=ctx;
	}
	
	public void startUpdates(long time, float distance)
	{
		if(!isUpdating)
		{
			isUpdating=true;
			Log.d("OpenTrail","MapLocationProcessor.startUpdates()");
			mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,time,distance,this);
			if(displayer.isLocationMarker())
			{
				displayer.showLocationMarker();
			}
		}
	}
	
	public void stopUpdates()
	{
		Log.d("OpenTrail","MapLocationProcessor.stopUpdates()");
		mgr.removeUpdates(this);
		if(displayer.isLocationMarker())
		{
			displayer.hideLocationMarker();
		}
		isUpdating=false;
		cancelGPSWaiting();
	}
	
	public void onLocationChanged(Location loc)
	{
		GeoPoint p = new GeoPoint(loc.getLatitude(),loc.getLongitude());
		
		if(!displayer.isLocationMarker())
			displayer.addLocationMarker(p);
		else
			displayer.moveLocationMarker(p);
		
		cancelGPSWaiting();
		receiver.receiveLocation(loc);	
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
			displayer.showLocationMarker();
	}

	private void hideLocationMarker()
	{
		if(displayer.isLocationMarker())
			displayer.hideLocationMarker();
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