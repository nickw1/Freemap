package freemap.andromaps;




import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.content.Context;




// This is similar to the MapLocationProcessor but also sets up the actual location listener
// It was separated off to allow location to also be done with a BroadcastReceiver receiving a broadcast
// from a Service, as is now done in OpenTrail.

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