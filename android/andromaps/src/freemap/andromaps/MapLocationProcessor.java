package freemap.andromaps;

import org.mapsforge.android.maps.overlay.ItemizedOverlay;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.core.GeoPoint;


import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.graphics.drawable.Drawable;
import android.content.Context;

import android.util.Log;



// Role: to receive a location and show the "my location" marker.
// Forwards the location on to a MapLocationReceiver which can do application-specific processing.

public class MapLocationProcessor implements LocationListener
{
	LocationManager mgr;
	LocationDisplayer displayer;
	Drawable icon;
	
	public interface MapLocationReceiver
	{
		public void receiveLocation (Location loc);
	}
	
	public interface LocationDisplayer
	{
		public void addLocationMarker(GeoPoint p);
		public void showLocationMarker();
		public void moveLocationMarker(GeoPoint p);
		public void hideLocationMarker();
		public boolean isLocationMarker();
	}
	
	MapLocationReceiver receiver;
	public MapLocationProcessor(MapLocationReceiver receiver,Context ctx,LocationDisplayer displayer)
	{
		mgr = (LocationManager)ctx.getSystemService(Context.LOCATION_SERVICE);
		this.displayer = displayer;
		this.receiver = receiver;
	}
	
	public void startUpdates()
	{
		Log.d("OpenTrail","MapLocationProcessor.startUpdates()");
		mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
		if(displayer.isLocationMarker())
		{
			displayer.showLocationMarker();
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
	}
	
	public void onLocationChanged(Location loc)
	{
		GeoPoint p = new GeoPoint(loc.getLatitude(),loc.getLongitude());
		if(!displayer.isLocationMarker())
		{
			displayer.addLocationMarker(p);
		}
		else
		{
			displayer.moveLocationMarker(p);
		}
		
		receiver.receiveLocation(loc);	
	}
	
	public void onProviderEnabled(String provider)
	{
	}

	public void onProviderDisabled(String provider)
	{
	}

	public void onStatusChanged(String provider, int status, Bundle extras)
	{
	
	}
}