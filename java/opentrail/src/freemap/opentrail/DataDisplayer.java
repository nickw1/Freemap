package freemap.opentrail;

import java.util.ArrayList;
import java.util.HashMap;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.ArrayItemizedOverlay;
import org.mapsforge.android.maps.overlay.ArrayWayOverlay;
import org.mapsforge.android.maps.overlay.ItemizedOverlay;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.android.maps.overlay.OverlayWay;
import org.mapsforge.core.GeoPoint;

import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.graphics.drawable.Drawable;
import android.content.Context;

import freemap.data.Annotation;
import freemap.data.POI;
import freemap.data.Point;
import freemap.data.TrackPoint;
import freemap.data.Walkroute;
import freemap.data.Projection;


public class DataDisplayer extends freemap.andromaps.LocationDisplayer implements 
	freemap.datasource.FreemapDataset.AnnotationVisitor
{
	
	
	OverlayItem  lastAddedPOI;
	ArrayWayOverlay walkrouteOverlay;
	Paint outline;
	ArrayList<OverlayItem> walkrouteStages;
	
	Drawable markerIcon, annotationIcon;
	HashMap<Integer,OverlayItem> indexedAnnotations;
	Projection proj;
	
	boolean includeAnnotations;
	
	public DataDisplayer(Context ctx, MapView mapView, Drawable locationIcon,  Drawable markerIcon, Drawable annotationIcon,
							Projection proj)
	{
		super(ctx,mapView,locationIcon);
		
		this.markerIcon = markerIcon;
		this.annotationIcon = annotationIcon;
		this.proj = proj;
	
    	outline = new Paint();
    	outline.setColor(Color.BLUE);
    	outline.setStrokeWidth(5);
    	outline.setAlpha(128);
    	outline.setStyle(Paint.Style.STROKE);
    	
    	//overlay = new ArrayItemizedOverlay(markerIcon,true);
    	
    	walkrouteOverlay = new ArrayWayOverlay(null,outline);
     	mapView.getOverlays().add(walkrouteOverlay);
     	

     	
		walkrouteStages = new ArrayList<OverlayItem>();
		indexedAnnotations = new HashMap<Integer,OverlayItem>();
		
		
	}
	
	
	
	public void showWalkroute(Walkroute walkroute)
	{
		
		GeoPoint gp = new GeoPoint(walkroute.getStart().y,walkroute.getStart().x);
		Log.d("OpenTrail","showWalkroute(): center=" + gp);
		mapView.setCenter(gp);
		ArrayList<TrackPoint> points = walkroute.getPoints();
		GeoPoint p[] = new GeoPoint[points.size()];
		for(int i=0; i<points.size(); i++)
			p[i] = new GeoPoint(points.get(i).y, points.get(i).x);
		GeoPoint[][] pts = new GeoPoint[][] { p };
		OverlayWay way = new OverlayWay(pts);
		clearWalkroute();
		walkrouteOverlay.addWay(way);
		ArrayList<Walkroute.Stage> stages = walkroute.getStages();
		GeoPoint curStagePoint = null;
	
		while(walkrouteStages.size() > 0)
		{	
			overlay.removeItem(walkrouteStages.remove(0));
		}
	
		for(int i=0; i<stages.size(); i++)
		{
			curStagePoint = new GeoPoint(stages.get(i).start.y, stages.get(i).start.x);
			OverlayItem item = new OverlayItem(curStagePoint,stages.get(i).description,
											stages.get(i).description,ItemizedOverlay.boundCenterBottom
											(markerIcon));
			walkrouteStages.add(item);
			overlay.addItem(item);
		}
	}
	
	public void clearWalkroute()
	{
		if(walkrouteOverlay.size()>0)
			walkrouteOverlay.clear();
	}
	
	public void visit(Annotation ann)
    {
    	if(indexedAnnotations.get(ann.getId()) == null)
    	{
    		Point unproj = proj.unproject(ann.getPoint());
    		GeoPoint gp = new GeoPoint(unproj.y,unproj.x);
    		OverlayItem item = new OverlayItem(gp,"Annotation #"+ann.getId(),ann.getDescription(),
    				ItemizedOverlay.boundCenterBottom(annotationIcon));
    		overlay.addItem(item);
    		
    		indexedAnnotations.put(ann.getId(), item);
    	}
    }
	
	public void displayPOI(POI poi)
	{
	    if(lastAddedPOI!=null)
	    	overlay.removeItem(lastAddedPOI);
	   	Log.d("OpenTrail","Found POI: " + poi.getPoint());
	   	Point unproj = proj.unproject(poi.getPoint());
	   	Log.d("OpenTrail","unprojected: " + unproj);
	   	GeoPoint gp = new GeoPoint(unproj.y,unproj.x);
	   	if(mapView==null)
	   		Log.d("OpenTrail","WARNING  mapview null");
    	mapView.setCenter(gp);
    	if(poi==null)
    		Log.d("OpenTrail","WARNING poi null");
    	String name=poi.getValue("name");
	    name=(name==null)? "unnamed":name;
	    OverlayItem item = new OverlayItem(gp,name,name,ItemizedOverlay.boundCenterBottom(markerIcon));
	   	overlay.addItem(item);
	   	lastAddedPOI = item;	   
	}

	public void addIconItem(OverlayItem item)
	{
		overlay.addItem(item);
	}
	
	public void removeIconItem(OverlayItem item)
	{
		overlay.removeItem(item);
	}
	
	public void addIconOverlay()
	{
		mapView.getOverlays().add(overlay);
	}
	
	public void removeIconOverlay()
	{
		mapView.getOverlays().remove(overlay);
	}

	public void requestRedraw()
	{
		overlay.requestRedraw();
		walkrouteOverlay.requestRedraw();
	}
	
	public void hideAnnotations()
	{
		for(HashMap.Entry<Integer,OverlayItem> entry: indexedAnnotations.entrySet())
			overlay.removeItem(entry.getValue());
	}
	
	public void showAnnotations()
	{
		for(HashMap.Entry<Integer,OverlayItem> entry: indexedAnnotations.entrySet())
			overlay.addItem(entry.getValue());
	}
	
	// for bitmap out of memory issues
	public void cleanup()
	{
		indexedAnnotations.clear();
    	overlay.clear();
    	indexedAnnotations = null;
    	overlay = null;
	}
	
}
