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

public class MapLocationProcessorWithListener extends MapLocationProcessor implements LocationListener
{
	LocationManager mgr;

	// location manager is specific
	public MapLocationProcessorWithListener(LocationReceiver processor,Context ctx,LocationDisplayer displayer)
	{
		super(processor,ctx,displayer);
		mgr = (LocationManager)ctx.getSystemService(Context.LOCATION_SERVICE);
		
	}
	
	public void startUpdates(long time, float distance)
	{
		if(!isUpdating)
		{
			super.startUpdates();	
			mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,time,distance,this);
		
		}
	}
	
	public void stopUpdates()
	{	
		mgr.removeUpdates(this);
		super.stopUpdates();
	}
	
	public void onLocationChanged(Location loc)
	{
		super.onLocationChanged(loc.getLongitude(), loc.getLatitude());
	}
}