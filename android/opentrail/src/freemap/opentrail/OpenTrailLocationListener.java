package freemap.opentrail;





import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;


public class OpenTrailLocationListener implements LocationListener {

	 public interface Observer {
		public void receive (Location loc);
	 }
	 
	 boolean gotGPSFix;
	 Observer observer;
	 
	 public void startUpdates(Context ctx)
	 {
			LocationManager mgr = (LocationManager)ctx.getSystemService(Context.LOCATION_SERVICE);
			mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
	 }
	 
	 public void stopUpdates(Context ctx)
	 {
			LocationManager mgr = (LocationManager)ctx.getSystemService(Context.LOCATION_SERVICE);
			mgr.removeUpdates(this);
	 }
	 
	 public void onLocationChanged(Location loc)
	 {
	    if(!gotGPSFix && loc.getProvider().equals(LocationManager.GPS_PROVIDER))
	    {
	    	gotGPSFix = true;
	    }
	    		
	    if(loc.getProvider().equals(LocationManager.GPS_PROVIDER) || gotGPSFix==false)
	    {
	    	if(observer!=null)
	    		observer.receive(loc);
	    		
	    }
	 }
	 public void onProviderDisabled(String provider) {
	 }
	  
	 public void onProviderEnabled(String provider) {
	 }
	    	
	 public void onStatusChanged(String provider,int status,Bundle extras) {
	 }

	 public void setObserver(Observer obs) {
		 observer=obs;
	 }
}
	    

	    
       

	
	
	
