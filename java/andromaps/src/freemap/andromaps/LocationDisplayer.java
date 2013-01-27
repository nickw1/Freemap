package freemap.andromaps;

// General role: a displayer of overlay data
// This or its subclasses (e.g. DataDisplayer in opentrail) would collect together all necessary overlays
// and display data on them

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.ArrayItemizedOverlay;
import org.mapsforge.android.maps.overlay.ItemizedOverlay;
import org.mapsforge.core.GeoPoint;
import org.mapsforge.android.maps.overlay.OverlayItem;



import android.widget.Toast;
import android.graphics.drawable.Drawable;
import android.content.Context;


public class LocationDisplayer implements MapLocationProcessor.LocationDisplayer {
	protected MapView mapView;
	protected ArrayItemizedOverlay overlay;
	protected Context ctx;
	protected Drawable locationIcon;
	protected OverlayItem myLocOverlayItem;

	public LocationDisplayer(Context ctx, MapView mapView,  Drawable locationIcon)
	{
		this.mapView = mapView;
		this.ctx = ctx;
		this.locationIcon = locationIcon;
		
		
		overlay = new ArrayItemizedOverlay(locationIcon)
    	{
    		protected boolean onTap(int index)
    		{
    			OverlayItem item = this.createItem(index); // !!! http://code.google.com/p/mapsforge/issues/detail?id=105
    			toast(item.getSnippet());
    			return true;
    		}
    		/* onLongPress gives exception, it appears that another thread is created which means that the toast/alert dialog
    		 * cannot be displayed as it is not the GUI thread
    		protected boolean onLongPress(int index)
    		{
    			//Toast.makeText(OpenTrail.this, "Long press on index: " + index, Toast.LENGTH_SHORT).show();
    			new AlertDialog.Builder(OpenTrail.this).setPositiveButton("OK",null).
					setMessage("longpress " + index).setCancelable(false).show();
    			return true;
    		}
    		*/
    	};
    
    	mapView.getOverlays().add(overlay);	
	}
	
	protected void toast(String text)
	{
		Toast.makeText(ctx,text,Toast.LENGTH_SHORT).show();
	}
	
	public void addLocationMarker(GeoPoint p)
	{
		myLocOverlayItem = new OverlayItem(p,"My location","My location",
					ItemizedOverlay.boundCenterBottom(locationIcon));//,personIcon);
		showLocationMarker();
	}
	
	public void showLocationMarker()
	{
		if(overlay!=null)
			overlay.addItem(myLocOverlayItem);
	}
	
	public void moveLocationMarker(GeoPoint p)
	{
		myLocOverlayItem.setPoint(p);
	}
	
	public void hideLocationMarker()
	{
		if(overlay!=null)
			overlay.removeItem(myLocOverlayItem);
	}	
	
	public boolean isLocationMarker()
	{
		return myLocOverlayItem != null;
	}
}
