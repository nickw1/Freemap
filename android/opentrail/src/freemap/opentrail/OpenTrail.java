package freemap.opentrail;

// Credits for icons
// res/drawable/person.png is taken from osmdroid.
// res/drawable/annotation.png is modified from the standard OSM viewpoint icon.

import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.core.GeoPoint;
import android.app.AlertDialog;
import java.io.IOException;
import android.util.Log;
import java.util.ArrayList;

import java.io.FileNotFoundException;


import android.content.Intent;
import android.content.SharedPreferences;
import android.view.MenuItem;
import android.view.Menu;
import android.view.MenuInflater;

import android.location.Location;
import android.os.Vibrator;
import android.os.AsyncTask;

import org.mapsforge.android.maps.overlay.OverlayItem;

import android.graphics.drawable.Drawable;




import freemap.data.Annotation;
import freemap.data.POI;
import freemap.data.Point;
import freemap.data.Walkroute;
import freemap.datasource.FreemapDataHandler;
import freemap.datasource.FreemapDataset;
import freemap.datasource.FreemapFileFormatter;
import freemap.datasource.TileDeliverer;
import freemap.datasource.WebDataSource;
import freemap.datasource.XMLDataInterpreter;

import freemap.andromaps.MapLocationProcessor;
import freemap.andromaps.DataCallbackTask;
import freemap.andromaps.ConfigChangeSafeTask;
import freemap.andromaps.DownloadFilesTask;


/*
References to this

mapView
alertDisplayMgr
mapLocationProcessor
tasks

dataDisplayer: mapView
*/

public class OpenTrail extends MapActivity implements 
						AlertDisplay, MapLocationProcessor.MapLocationReceiver,
						DataReceiver, DownloadFilesTask.Callback {
	
	String mapFile, sdcard, styleFile;
	MapView mapView;
	MapLocationProcessor mapLocationProcessor;
	
	boolean tracking;
	
	FreemapFileFormatter formatter;
	TileDeliverer poiDeliverer;
	String cachedir;
	boolean prefGPSTracking, prefAutoDownload, prefAnnotations;
	Drawable personIcon, annotationIcon, markerIcon;

	AlertDisplayManager alertDisplayMgr;
	


	
	DataDisplayer dataDisplayer;
	
	DataCallbackTask<?,?> dataTask;
	DownloadFilesTask dfTask;
	
	int walkrouteIdx;
	
	public static class SavedData
	{
		DataCallbackTask<?,?> dataTask;
		ConfigChangeSafeTask<?,?> dfTask;	
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
  
    	
    	
        super.onCreate(savedInstanceState);
        try
        {
        	sdcard = Environment.getExternalStorageDirectory().getAbsolutePath() ;
        	
        	mapFile = sdcard + "/opentrail/hampshire.map";
        	mapView = new MapView(this); 
        	mapView.setClickable(true);
        	mapView.setBuiltInZoomControls(true);
        	

        	Proj4ProjectionFactory factory=new Proj4ProjectionFactory();
        	String proj="epsg:27700";
    		Shared.proj = factory.generate(proj);
        	
    		SavedData savedData=(SavedData)getLastNonConfigurationInstance();
        	if(savedData!=null)
        	{
        		Log.d("OpenTrail","Restarting task");	
        		if(savedData.dataTask!=null)
        			savedData.dataTask.reconnect(this, this);
        		else if (savedData.dfTask!=null)
        			savedData.dfTask.reconnect(this);
        	}
        	
        	walkrouteIdx = -1;
        	
        	GeoPoint pos = new GeoPoint(51, -1);
        	
        	dataDisplayer = new DataDisplayer(getApplicationContext(),mapView,
					getResources().getDrawable(R.drawable.person),
					getResources().getDrawable(R.drawable.marker),
					getResources().getDrawable(R.drawable.annotation),
					Shared.proj);
        	
        	
        	if(savedInstanceState!=null)
        	{
        			
        			
        		pos = new GeoPoint(savedInstanceState.getDouble("lat"),
        								savedInstanceState.getDouble("lon"));
       			mapView.getController().setZoom(savedInstanceState.getInt("zoom"));
       			mapFile = savedInstanceState.getString("mapFile");
       			walkrouteIdx = savedInstanceState.getInt("walkrouteIdx");
       			loadAnnotationOverlay();
       		}   	
    		
        	setContentView(mapView);
        	
          	File mf = new File(mapFile);
        	if(mf.exists())
        		mapView.setMapFile(mf);
        	
        	mapView.setCenter(pos);
     
    		cachedir=makeCacheDir(proj);
    		FreemapFileFormatter formatter=new FreemapFileFormatter(Shared.proj.getID());
    		formatter.setScript("bsvr.php");
    		formatter.selectPOIs("place,amenity,natural");
    		formatter.selectAnnotations(true);
    		WebDataSource ds=new WebDataSource("http://www.free-map.org.uk/0.6/ws/",formatter);
    		poiDeliverer=new TileDeliverer("poi",ds, new XMLDataInterpreter
    				(new FreemapDataHandler()),5000,5000,Shared.proj,cachedir);
        	
    		
    		
    		if (Shared.pois==null)
    			Shared.pois = new FreemapDataset();
        
        	
    		alertDisplayMgr = AlertDisplayManager.getInstance(this, 10);
    		alertDisplayMgr.setPOIs(Shared.pois);
    		
    	
    		
    		mapLocationProcessor=new MapLocationProcessor(this,getApplicationContext(),dataDisplayer);
    		
    		if(walkrouteIdx > -1 && walkrouteIdx < Shared.walkroutes.size())
    		 	dataDisplayer.showWalkroute(Shared.walkroutes.get(walkrouteIdx));
    	
        	
        	
        	styleFile = sdcard+"/opentrail/freemap.xml";
        	File sf = new File(styleFile);
        	if(!sf.exists())
        	{
        		dfTask = new DownloadFilesTask(this,  new String[] 
        		                                   { "http://www.free-map.org.uk/data/android/freemap.xml" }, 
        		                           new String[] { styleFile }, 
        		                           "No style file found. Download?", this, 0);
        		dfTask.setDialogDetails("Downloading...","Downloading style file...");
        		dfTask.confirmAndExecute();
        	}
        	else
        	{
        		mapView.setRenderTheme(sf);
        	}
        	
        	
      
        }
        catch(IOException e)
        {
        	new AlertDialog.Builder(this).setPositiveButton("OK",null).
        		setCancelable(false).setMessage(e.getMessage()).show();
        } 
    }
    
    public void downloadFinished(int id)
    {
    	try
    	{
    		mapView.setRenderTheme(new File(styleFile));
    	}
    	catch(FileNotFoundException e)
    	{
    		new AlertDialog.Builder(this).setPositiveButton("OK",null).
    			setCancelable(false).setMessage(e.getMessage()).show();
    	}
    }
    
    public void onStart()
    {
    	super.onStart();
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
     	boolean oldPrefAnnotations = prefAnnotations;
     	
     	prefGPSTracking = prefs.getBoolean("prefGPSTracking", true);
     	prefAnnotations = prefs.getBoolean("prefAnnotations", true);
     	prefAutoDownload = prefs.getBoolean("prefAutoDownload", false);
     	
     	if(prefGPSTracking)
     	{
     		mapLocationProcessor.startUpdates();
     	}
     	
     	if(prefAnnotations==false && oldPrefAnnotations==true)
     	{
     		dataDisplayer.hideAnnotations();
     		mapView.invalidate();
     		dataDisplayer.requestRedraw();
     	}
     	
     	else if (prefAnnotations==true && oldPrefAnnotations==false)
     	{
     		dataDisplayer.showAnnotations();
     		mapView.invalidate();
     		dataDisplayer.requestRedraw();
     	}
    }
    
    public void onStop()
    {
    	super.onStop();
    	Log.d("OpenTrail","***onStop()***");
    	mapLocationProcessor.stopUpdates();
    }
    
    public void onDestroy()
    {
    	super.onDestroy();
    	Log.d("OpenTrail","***onDestroy()***");
    	dataDisplayer.cleanup();
    	mapLocationProcessor=null;
    	mapView = null;
    	alertDisplayMgr=null;
    	dataDisplayer=null;
    }
    
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	Intent intent = null;
    	boolean retcode=false;
    	switch(item.getItemId())
    	{
    		
    		case R.id.selectMapMenuItem:
    			intent = new Intent(this,FileChooser.class);
    			startActivityForResult(intent, 0);
    			break;
    			
    		case R.id.myLocationMenuItem:
    			gotoMyLocation();
    			break;
    			
    		case R.id.inputAnnotationMenuItem:
    			if(Shared.location!=null)
    			{
    				intent = new Intent(this,InputAnnotationActivity.class);
    				startActivityForResult(intent,1);
    			}
    			else
    			{
    				new AlertDialog.Builder(this).setMessage("Location unknown").
    					setPositiveButton("OK",null).setCancelable(false).show();
    			}
    			retcode=true;
    			break;
    			
    		
    		case R.id.settingsMenuItem:
    			
    			intent = new Intent(this,OpenTrailPreferences.class);
    			startActivity(intent);
    			retcode=true;
    			
    			break;
    		
    		case R.id.poisMenuItem:
    			startPOIDownload();
    			break;
    			
    		case R.id.findPoisMenuItem:
    			intent = new Intent(this,POITypesListActivity.class);
    			startActivityForResult(intent,2);
    			break;
    			
    		case R.id.walkroutesMenuItem:
    			if(Shared.location==null)
    				new AlertDialog.Builder(this).setMessage("Location not known").setPositiveButton("OK",null).
    					setCancelable(false).show();
    			else
    			{
    				dataTask = new DownloadWalkroutesTask(this, this);
    				((DownloadWalkroutesTask)dataTask).execute();
    			}
    			break;
    			
    		case R.id.findWalkroutesMenuItem:
    			intent = new Intent(this,WalkrouteListActivity.class);
    			startActivityForResult(intent,3);
    			break;
    	}
    	return retcode;
    }
    
    public void onActivityResult(int request, int result, Intent i)
    {
    	if(result==RESULT_OK)
    	{
    		switch(request)
    		{
    			case 0:
    				// Loading a new mapfile sets the centre to the centre of the mapfile
    				// so we need to save the old centre if we want the same position (and GPS not present)
    				GeoPoint currentCentre = mapView.getMapPosition().getMapCenter();
    				Bundle extras = i.getExtras();
    				Log.d("OpenTrail","Returned : " + extras.getString("mapFile"));
    				mapFile = sdcard+"/"+extras.getString("mapFile");
    				mapView.setMapFile(new File(mapFile));
    				mapView.setCenter(currentCentre);
    				gotoMyLocation();
    				mapView.redrawTiles();
    			 	Log.d("OpenTrail","Centre of map = " + currentCentre);
    				break;
    			case 1:
    				extras = i.getExtras();
    				String id=extras.getString("ID"),description=extras.getString("description");
    				GeoPoint gp = new GeoPoint(extras.getDouble("lat"),extras.getDouble("lon"));
    				Point p = Shared.proj.project(new Point(extras.getDouble("lon"),extras.getDouble("lat")));
    				OverlayItem item = new OverlayItem(gp,"Annotation #"+id,description);
    				item.setMarker(annotationIcon);
    				dataDisplayer.addIconItem(item);
    				Annotation ann=new Annotation(Integer.parseInt(id),p.x,p.y,description);
    				Shared.pois.add(ann);
    				mapView.invalidate();
    				break;
    			case 2:
    				extras = i.getExtras();
    				POI poi = Shared.pois.getPOIById(Integer.parseInt(extras.getString("osmId")));
    				Log.d("OpenTrail","found POI="+poi);
    				if(poi!=null)
    					dataDisplayer.displayPOI(poi);
    				break;
    				
    			case 3:
    				extras = i.getExtras();
    				int idx = extras.getInt("selectedRoute");
    				dataTask = new DownloadWalkrouteTask(this, this);
    				((DownloadWalkrouteTask)dataTask).execute(idx);
    				
    				break;
    		}
    	}
    }
    
    public void gotoMyLocation()
    {
    	if(Shared.location!=null)
    	{
    		GeoPoint p = new GeoPoint(Shared.location.getLatitude(),Shared.location.getLongitude());
    		mapView.setCenter(p);
    	}
    }
    
    private void startPOIDownload()
    {
    	if(Shared.location!=null)
    	{
    		if(dataTask==null || dataTask.getStatus()!=AsyncTask.Status.RUNNING)
    		{
            	dataTask = new DownloadPOIsTask(this, poiDeliverer, this);
            	((DownloadPOIsTask)dataTask).execute();
    		}
    		else
    		{
    			new AlertDialog.Builder(this).setMessage("You're already downloading!").setCancelable(false).
					setPositiveButton("OK",null).show();
    		}
    	}
    	else
    	{
    		new AlertDialog.Builder(this).setMessage("Location unknown").setPositiveButton("OK", null).
    				setCancelable(false).show();
    	}
    }
    
 
    
    public void loadAnnotationOverlay()
    {
    	Shared.pois.operateOnAnnotations(dataDisplayer);
    }
    
    public Object onRetainNonConfigurationInstance()
    {
    	SavedData saved = new SavedData();
    	
    	if(dataTask!=null && dataTask.getStatus()==AsyncTask.Status.RUNNING)
    	{
    		dataTask.disconnect();
    		saved.dataTask = dataTask;
    		
    	}
    	
    	else if(dfTask!=null && dfTask.getStatus()==AsyncTask.Status.RUNNING)
    	{
    		dfTask.disconnect();
    		saved.dfTask = dfTask;
    	}
    	
    	return saved;
    }
    
    
    private String makeCacheDir(String projID)
    {
    	String cachedir = Environment.getExternalStorageDirectory().getAbsolutePath()
    		+"/opentrail/cache/" + projID.toLowerCase().replace("epsg:", "")+"/";
    	File dir = new File(cachedir);
    	if(!dir.exists())
    		dir.mkdirs();
    	return cachedir;
    }
    
    public void displayAnnotationInfo(String msg)
    {
    	new AlertDialog.Builder(this).setMessage(msg).setCancelable(false).setPositiveButton("OK",null).show();
    	/*
    	Vibrator vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
    	vib.vibrate(1000);
    	*/
    }
    
    public void receiveLocation(Location loc)
    {
    	GeoPoint p = new GeoPoint(loc.getLatitude(),loc.getLongitude());
		Point pt = new Point(loc.getLongitude(), loc.getLatitude());
    	Shared.location = loc;
    	if(prefGPSTracking==true)
    	{
    		if(mapView==null)
    			Log.e("OpenTrail","mapView is null - HELP! this should never happen, wtf is going on here???");
    		else
    			mapView.setCenter(p);
    	}
    	if(prefAutoDownload && poiDeliverer.needNewData(pt))
    		startPOIDownload();
    	alertDisplayMgr.update(pt);
    }
   
   

    public void receivePOIs(FreemapDataset ds)
    {
    	Shared.pois = ds;
    	alertDisplayMgr.setPOIs(Shared.pois);
    	loadAnnotationOverlay();
    }
    
    public void receiveWalkroutes(ArrayList<Walkroute> walkroutes)
    {
    	Shared.walkroutes = walkroutes;
    }
   
    public void receiveWalkroute(int idx, Walkroute walkroute)
    {
		Shared.walkroutes.set(idx, walkroute);
    	alertDisplayMgr.setWalkroute(walkroute);
    	dataDisplayer.showWalkroute(walkroute);
    	walkrouteIdx = idx;
    }
    
    public void onSaveInstanceState(Bundle state)
    {
      	GeoPoint gp = mapView.getMapPosition().getMapCenter();
    	state.putDouble("lat", gp.getLatitude());
    	state.putDouble("lon", gp.getLongitude());
    	state.putInt("zoom",mapView.getMapPosition().getZoomLevel());
    	state.putString("mapFile",mapFile);
    	state.putInt("walkrouteIdx", walkrouteIdx);
    }
}