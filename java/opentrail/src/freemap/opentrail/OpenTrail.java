package freemap.opentrail;

// Credits for icons
// res/drawable/person.png is taken from osmdroid, cropped
// res/drawable/annotation.png is modified from the standard OSM viewpoint icon.

import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.content.Context;

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
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.ComponentName;

import android.view.MenuItem;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.MotionEvent;
import android.widget.Toast;


import android.location.Location;
import android.os.Vibrator;
import android.os.AsyncTask;
import android.location.LocationManager;
import android.content.IntentFilter;
import android.net.Uri;
import android.media.Ringtone;
import android.media.RingtoneManager;

import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.android.maps.overlay.ItemizedOverlay;

import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;

import uk.me.jstott.jcoord.OSRef;

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
import freemap.datasource.AnnotationCacheManager;
import freemap.datasource.WalkrouteCacheManager;

import freemap.andromaps.MapLocationProcessor;
import freemap.andromaps.DataCallbackTask;
import freemap.andromaps.DownloadBinaryFilesTask;
import freemap.andromaps.DownloadTextFilesTask;
import freemap.andromaps.HTTPUploadTask;
import freemap.andromaps.HTTPCommunicationTask;
import freemap.andromaps.DialogUtils;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/*
References to this
mapView
alertDisplayMgr
mapLocationProcessor
tasks

dataDisplayer: mapView
*/

// key for ACRA: dGZ6RDJDaWxCMTlfcEJqYTJDRFEtTmc6MQ

public class OpenTrail extends MapActivity implements 
						AlertDisplay, MapLocationProcessor.LocationReceiver,
						DataReceiver, HTTPCommunicationTask.Callback,
						MapView.OnTouchListener {
	
	String mapFile, sdcard, styleFile;
	MapView mapView;
	MapLocationProcessorBR mapLocationProcessor;
	IntentFilter filter;
	
	boolean tracking;
	
	FreemapFileFormatter formatter;
	TileDeliverer poiDeliverer;
	String cachedir;
	boolean prefGPSTracking, prefAutoDownload, prefAnnotations, recordingWalkroute;
	Drawable personIcon, annotationIcon, markerIcon;
	
	String curGridsq;

	AlertDisplayManager alertDisplayMgr;
	AnnotationCacheManager annCacheMgr;
	
	
	DataDisplayer dataDisplayer;
	
	DataCallbackTask<?,?> dataTask;
	HTTPCommunicationTask dfTask;
	
	int walkrouteIdx, localAnnId, recordingWalkrouteId;
	int xDown = -256, yDown = -256;
	long lastWRUpdateTime = 0;
	int readZoom = -1;
	
	boolean mapSetup=false,waitingForNewPOIData=false;
	
	
	
	
	GeoPoint initPos;
	ServiceConnection gpsServiceConn;
	GPSService gpsService;
	WalkrouteCacheManager wrCacheMgr;
	GeoPoint location;
	
	freemap.data.Projection proj;
	
	public static FreemapDataset pois;
	public static ArrayList<Walkroute> walkroutes;
	
	public static class SavedData
	{
		DataCallbackTask<?,?> dataTask;
		HTTPCommunicationTask dfTask;
		FreemapDataset pois;
		ArrayList<Walkroute> walkroutes;
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
  
    	
 
    	
        super.onCreate(savedInstanceState);
        
        try
        {
        	sdcard = Environment.getExternalStorageDirectory().getAbsolutePath() ;
        	//sdcard="/mnt/extSdCard";
        	File opentrailDir = new File(sdcard+"/opentrail");
        	if(!opentrailDir.exists())
        		opentrailDir.mkdir();
        
        	mapFile = null; 
        	mapView = new MapView(this); 
        	mapView.setClickable(true);
        	mapView.setBuiltInZoomControls(true);
        	mapView.setOnTouchListener(this);

        	Proj4ProjectionFactory factory=new Proj4ProjectionFactory();
        	String projString="epsg:27700";
    		this.proj = new OSGBProjection(); // factory.generate(projString);
        	
    		SavedData savedData=(SavedData)getLastNonConfigurationInstance();
        	if(savedData!=null)
        	{
        		
        		if(savedData.dataTask!=null)
        			savedData.dataTask.reconnect(this, this);
        		else if (savedData.dfTask!=null)
        			savedData.dfTask.reconnect(this, this);
        		
        		if(Shared.pois==null && savedData.pois!=null)
        			Shared.pois = savedData.pois;
        		if(Shared.walkroutes==null && savedData.walkroutes!=null)
        			Shared.walkroutes = savedData.walkroutes;
        	}
        	
        	walkrouteIdx = -1;
        	
        	
        	
        	dataDisplayer = new DataDisplayer(getApplicationContext(),mapView,
					getResources().getDrawable(R.drawable.person),
					getResources().getDrawable(R.drawable.marker),
					getResources().getDrawable(R.drawable.annotation),
					this.proj);
        	
        	SharedPreferences prefs;
        	
        	if(savedInstanceState!=null)
        	{
        		initPos = new GeoPoint(savedInstanceState.getDouble("lat"),
        								savedInstanceState.getDouble("lon"));
        		readZoom = savedInstanceState.getInt("zoom");
       			mapView.getController().setZoom(readZoom);
       			mapFile = savedInstanceState.getString("mapFile");
       			walkrouteIdx = savedInstanceState.getInt("walkrouteIdx");
       			recordingWalkroute = savedInstanceState.getBoolean("recordingWalkroute");
       			waitingForNewPOIData = savedInstanceState.getBoolean("waitingForNewPOIData");
       			//loadAnnotationOverlay();
       			
       			
       		}   	
        	else if ((prefs=getPreferences(Context.MODE_PRIVATE))!=null)
        	{
        	    
        		initPos = new GeoPoint(prefs.getFloat("lat",51.05f),
        							prefs.getFloat("lon", -0.72f));
        		mapFile = prefs.getString("mapFile",null);
        		
        		readZoom = prefs.getInt("zoom", -1);
        		recordingWalkroute = prefs.getBoolean("recordingWalkroute", false);    
        		waitingForNewPOIData = prefs.getBoolean("waitingForNewPOIData", false);
        		
        		if(readZoom>=0)
        			mapView.getController().setZoom(readZoom);
        		
        	}
        	
        	
    		cachedir=makeCacheDir(projString);
    		FreemapFileFormatter formatter=new FreemapFileFormatter(this.proj.getID());
    		formatter.setScript("bsvr.php");
    		formatter.selectPOIs("place,amenity,natural");
    		formatter.selectAnnotations(true);
    		WebDataSource ds=new WebDataSource("http://www.free-map.org.uk/0.6/ws/",formatter);
    		poiDeliverer=new TileDeliverer("poi",ds, new XMLDataInterpreter
    				(new FreemapDataHandler()),5000,5000,this.proj,cachedir);
        	
    		
    		
    		if (Shared.pois==null)
    		{
    			Shared.pois = new FreemapDataset();
    			Shared.pois.setProjection(proj);
    		}
        
        	
    		alertDisplayMgr = new AlertDisplayManager(this, 50);
    		alertDisplayMgr.setPOIs(Shared.pois);
    		if(Shared.walkroutes!=null && walkrouteIdx > -1 && walkrouteIdx < Shared.walkroutes.size())
    		 	alertDisplayMgr.setWalkroute(Shared.walkroutes.get(walkrouteIdx));
    	
    		wrCacheMgr = new WalkrouteCacheManager(sdcard+"/opentrail/walkroutes/");
    	
    		
    		mapLocationProcessor=new MapLocationProcessorBR(this,this,dataDisplayer);
    		filter = new IntentFilter();
    		filter.addAction("freemap.opentrail.providerenabled");
    		filter.addAction("freemap.opentrail.statuschanged");
    		filter.addAction("freemap.opentrail.locationchanged");
    		
    		
    		File wrDir = new File(sdcard+"/opentrail/walkroutes/");
    		if(!wrDir.exists())
    			wrDir.mkdir();
    			
        	curGridsq="";
        	
        	
        	annCacheMgr = new AnnotationCacheManager(sdcard+"/opentrail/annotations/");
        	
        	gpsServiceConn = new ServiceConnection()
        	{
        		public void onServiceConnected(ComponentName n, IBinder binder)
        		{
        			gpsService = ((GPSService.Binder)binder).getService();
        			
        		}
        		
        		public void onServiceDisconnected(ComponentName n)
        		{
        			gpsService = null;
        		}
        	};
        	
        	Intent startServiceIntent = new Intent(this,GPSService.class);
       	 	bindService(startServiceIntent, gpsServiceConn, Context.BIND_AUTO_CREATE);
        	try
        	{
        		ArrayList<Annotation> savedAnnotations = annCacheMgr.getAnnotations();
        		
        		for(Annotation a: savedAnnotations)
        			Shared.pois.add(a);
        	}
        	catch(Exception e)
        	{
        		
        		DialogUtils.showDialog(this,"Error retrieving saved annotations: " + e.getMessage());
        	}
        	SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        	boolean prefGPSTracking = sprefs.getBoolean("prefGPSTracking", true);
        	if(prefGPSTracking)
         	{
         		//mapLocationProcessor.startUpdates(5000, 10.0f);
         		LocationManager mgr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
         		if(!mgr.isProviderEnabled(LocationManager.GPS_PROVIDER))
            	{
            		DialogUtils.showDialog(this,"GPS not enabled, please enable it to see current location");
            	}
         		else
         		{		
         			mapLocationProcessor.getProcessor().showGpsWaiting("Waiting for GPS");
         		}
         	}
        	
        	downloadStyleFile();
        	registerReceiver(mapLocationProcessor,filter);
        }
        catch(Exception e)
        {
        	DialogUtils.showDialog(this,"Error:" + e.getMessage());
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
     	
     	
     	
     	if(Shared.walkroutes!=null && walkrouteIdx > -1 && walkrouteIdx < Shared.walkroutes.size())
		 	dataDisplayer.showWalkroute(Shared.walkroutes.get(walkrouteIdx));
		 	
     	
     	if(prefAnnotations==false && oldPrefAnnotations==true)
     		dataDisplayer.hideAnnotations();
     		
     
     	
     	else if (prefAnnotations==true && oldPrefAnnotations==false)
     		dataDisplayer.showAnnotations();
     	
     	mapView.invalidate();
    	dataDisplayer.requestRedraw();
    	
    	//services can be both started and bound
    	//http://developer.android.com/guide/components/bound-services.html
    	//we need this as the activity requires data from the service, but we
    	//also need the service to keep going once the activity finishes
    	Intent startServiceIntent = new Intent(this,GPSService.class);
    	startServiceIntent.putExtra("wrCacheLoc",sdcard+"/opentrail/walkroutes/");
    	startService(startServiceIntent);   	
    }
    
    public void onStop()
    {
    	super.onStop();
    	
    	
    	
    	Log.d("OpenTrail","***onStop()***");
    	
    	//this.location=null;
    	
    	
    	Intent stopIfNotLoggingBroadcast = new Intent("freemap.opentrail.stopifnotlogging");
    	sendBroadcast(stopIfNotLoggingBroadcast);
    	
    }
    
    public void onDestroy()
    {
    	
    	super.onDestroy();
    	
    	unregisterReceiver(mapLocationProcessor);
    	unbindService(gpsServiceConn);
    	Log.d("OpenTrail","***onDestroy()***");
    	
    	dataDisplayer.cleanup();
    	//mapLocationProcessor=null;
    	//alertDisplayMgr=null;
    	dataDisplayer=null;
    	
    	SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = settings.edit();
    	
    	editor.putFloat("lat",(float)mapView.getMapPosition().getMapCenter().getLatitude());
    	editor.putFloat("lon",(float)mapView.getMapPosition().getMapCenter().getLongitude());
    	editor.putInt("zoom", mapView.getMapPosition().getZoomLevel());
    	editor.putBoolean("recordingWalkroute", recordingWalkroute);
    	editor.putBoolean("waitingForNewPOIData",waitingForNewPOIData);
    	editor.putString("mapFile",mapFile);
    	editor.commit();
    	mapView=null;
    	
    }
    
    public void downloadStyleFile()
    {
    	styleFile = sdcard+"/opentrail/freemap.xml";
    	File sf = new File(styleFile);
    	if(!sf.exists())
    	{
    		dfTask = new DownloadTextFilesTask(this,  new String[] 
    		                                   { "http://www.free-map.org.uk/data/android/freemap.xml" }, 
    		                           new String[] { styleFile }, 
    		                           "No Freemap style file found. Download?", this, 1);
    		dfTask.setDialogDetails("Downloading...","Downloading style file...");
    		dfTask.confirmAndExecute();
    	}
    	else if(mapFile!=null)
    	{
   			setupMap(new File(mapFile));
    	}
    }
    
    public void setupMap(File mf)
    {
    	try
    	{
    		mapView.setMapFile(mf);
    		if(!mapSetup)
			{
				if(styleFile!=null)
	    			mapView.setRenderTheme(new File(styleFile));
				setContentView(mapView);
				
				mapSetup=true;
			}
    		if(this.location!=null)
				mapView.setCenter(this.location);
			else if (initPos!=null)
				mapView.setCenter(initPos);
    		mapView.redrawTiles();
    		loadAnnotationOverlay();
    	}
    	catch(FileNotFoundException e)
    	{
    		DialogUtils.showDialog(this,"Style and/or map file not found: " + e.getMessage());
    	}
    }
    
    public void downloadFinished(int id, Object addData)
    {
    	switch(id)
    	{
    		case 0:
    			if(mapFile!=null)
       				setupMap(new File(mapFile));
    			break;
    			
    		case 1:
    			if(addData!=null)
    			{
    				Point osgb=null;
    				String gsq="";
    				if(this.location!=null)
    				{
    					
    					osgb = this.proj.project(new Point(this.location.getLongitude(),this.location.getLatitude()));
    					gsq = new OSRef(osgb.x,osgb.y).toSixFigureString().substring(0,2).toLowerCase();
    				
    					if(gsq.equals((String)addData))
    					{
    						mapFile=sdcard+"/opentrail/"+((String)addData)+".map";
    						File mf = new File(mapFile);
    						if(mf.exists())
    							setupMap(mf);
    					}
    				}
    			}
    			break;
    			
    		case 2:
    			annCacheMgr.deleteCache();
    			break;
    			
    		case 3:
    			// walkroute uploaded
    			break;
    	}
    }
    
    public void downloadCancelled(int id)
    {
    	switch(id)
    	{
    		case 0:
    			DialogUtils.showDialog(this, "Freemap style will not be used, using default osmarender style");
    			break;
    	}
    }
    
    public void downloadError(int id)
    {
    	DialogUtils.showDialog(this,"Upload/download task failed");
    }
    
    public void downloadLocalMapFile()
    {
    	if(this.location!=null)
    	{
    		downloadMapFile(this.location.getLongitude(),this.location.getLatitude());
    	}
    	else
    	{
    		DialogUtils.showDialog(this, "Location not known yet");
    	}
    }
    
    public void downloadMapFile(double lon, double lat)
    {
    	Point p = this.proj.project(new Point(lon,lat)); 
		String gridsq = new OSRef(p.x,p.y).toSixFigureString().substring(0,2).toLowerCase();
		downloadMapFile(gridsq, "");
    }
    
    public void downloadMapFile(String gridsq, String addMsg)
    {		
    	dfTask = new DownloadBinaryFilesTask(this,  new String[] 
                    { "http://www.free-map.org.uk/data/android/"+gridsq+".map" }, 
                    	new String[] { sdcard+"/opentrail/"+gridsq+".map" }, 
                    	addMsg + "Download "+gridsq+".map? Warning: 10-20 MB file!", this, 1);
    	dfTask.setAdditionalData(gridsq);
    	dfTask.setDialogDetails("Downloading...","Downloading map file...");
    	dfTask.confirmAndExecute();
    }
    
    public void showMapDialog()
    {
    	final String[] values = { "sw","sx","sy","sz","tv",
    							  "ss","st","su","tq","tr",
    							  "so","sp","tl","tm",
    							  "sj","sk","tf","tg",
    							  "sd","se","ta",
    							  "nx","ny","nz" };
    							  
    
    	new AlertDialog.Builder(this).setTitle("Select the map to download").
    		setCancelable(true).
    		setNegativeButton("Cancel",null).
    		setItems(R.array.gridsquares, new DialogInterface.OnClickListener()
    			{
    				public void onClick(DialogInterface i, int which)
    				{
    					downloadMapFile(values[which], "");
    				}
    			} ).show();
    }
    
    
    

    
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	return true;
    }
    
    public boolean onPrepareOptionsMenu(Menu menu)
    {
    	MenuItem recordWalkrouteMenuItem = menu.findItem(R.id.recordWalkrouteMenuItem);
    	recordWalkrouteMenuItem.setTitle(recordingWalkroute ? "Stop recording":"Record walk route");
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	
    	boolean retcode=true;
    	if(item.getItemId()==R.id.aboutMenuItem)
    	{
    		about();
    	}
    	else if(!mapSetup)
    	{
    		DialogUtils.showDialog(this,"Cannot perform actions until a map is loaded.");
    		retcode=false;
    	}
    	else
    	{
    		Intent intent = null;
    		
    		switch(item.getItemId())
    		{
    		
    			case R.id.selectMapMenuItem:
    				intent = new Intent(this,FileChooser.class);
    				startActivityForResult(intent, 0);
    				break;
    			
    			case R.id.downloadLocalMapMenuItem:
    				downloadLocalMapFile();
    				break;
    			
    			case R.id.downloadMapMenuItem:
    				showMapDialog();
    				break;
    			
    			case R.id.myLocationMenuItem:
    				gotoMyLocation();
    				break;
    			
    			case R.id.inputAnnotationMenuItem:
    				if(this.location!=null)
    					launchInputAnnotationActivity(this.location.getLatitude(),
    						this.location.getLongitude());
    				else
    					DialogUtils.showDialog(this,"Location not known yet");
    			
    				break;
    			
    		
    			case R.id.settingsMenuItem:
    			
    				intent = new Intent(this,OpenTrailPreferences.class);
    				startActivity(intent);
    			
    				break;
    		
    			case R.id.poisMenuItem:
    				SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences
    					(getApplicationContext());
    				startPOIDownload(true,sprefs.getBoolean("prefForcePOIDownload",false));
    				break;
    			
    			case R.id.findPoisMenuItem:
    				intent = new Intent(this,POITypesListActivity.class);
    				if(this.location==null)
    					DialogUtils.showDialog(this,"Location not known");
    				else
    				{
    					Point p = this.proj.project(new Point(location.getLongitude(),location.getLatitude()));
    					intent.putExtra("projectedX", p.x);
    					intent.putExtra("projectedY", p.y);
    					startActivityForResult(intent,2);
    				}
    				break;
    			
    			case R.id.walkroutesMenuItem:
    				if(this.location==null)
    					DialogUtils.showDialog(this, "Location not known");
    				else
    				{
    					dataTask = new DownloadWalkroutesTask(this, this, location );
    					((DownloadWalkroutesTask)dataTask).execute();
    				}
    				break;
    			
    			case R.id.findWalkroutesMenuItem:
    				if(Shared.walkroutes!=null)
    				{
    					intent = new Intent(this,WalkrouteListActivity.class);
    					startActivityForResult(intent,3);
    				}
    				else
    					DialogUtils.showDialog(this, "No walk routes downloaded yet");
    				break;
    			
    			case R.id.uploadAnnotationsMenuItem:
    				uploadCachedAnnotations();
    				break;
    			
    			case R.id.recordWalkrouteMenuItem:
    				recordingWalkroute = !recordingWalkroute;
    				item.setTitle(recordingWalkroute ? "Stop recording" : "Record walk route");
    			
    				
    				if(recordingWalkroute)
    				{
    					
    					dataDisplayer.clearWalkroute();
    					mapView.invalidate();
    				
    					
    			    	Intent startLoggingBroadcast = new Intent("freemap.opentrail.startlogging");
    			    	sendBroadcast(startLoggingBroadcast);
    			    	
    				}
    				else
    				{
    					
    			    	Intent stopLoggingBroadcast = new Intent("freemap.opentrail.stoplogging");
    			    	sendBroadcast(stopLoggingBroadcast);
    					showWalkrouteDetailsActivity();
    				}
    				break;
    			
    			case R.id.uploadWalkrouteMenuItem:
    				showRecordedWalkroutesActivity();
    				break;
    			default:
        			retcode=false;
        			break;
        	
    		}
    	}
    	return retcode;
    }
    
    public void launchInputAnnotationActivity(double lat, double lon)
    {
    	if(this.location!=null)
		{
    		Intent intent = new Intent(this,InputAnnotationActivity.class);
    		Bundle extras = new Bundle();
    		extras.putDouble("lat", lat);
    		extras.putDouble("lon", lon);
    		intent.putExtras(extras);
    		startActivityForResult(intent,1);
		}
		else
		{
			DialogUtils.showDialog(this,"Location unknown");
		}
    	
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
    				
    				mapFile = sdcard+"/opentrail/"+extras.getString("mapFile");
    				
    				File mf=new File(mapFile);
    				if(mf.exists())
    				{
    					mapView.setMapFile(mf);
    					mapView.setCenter(currentCentre);
    					gotoMyLocation();
    					mapView.redrawTiles();
    				}
    				else
    				{
    					DialogUtils.showDialog(this,"Unable to find map file: " + mapFile);
    				}
    				break;
    			case 1:
    				extras = i.getExtras();
    				if(extras.getBoolean("success"))
    				{
    					String id=extras.getString("ID"),description=extras.getString("description");
    					GeoPoint gp = new GeoPoint(extras.getDouble("lat"),extras.getDouble("lon"));
    					//Point p = this.proj.project(new Point(extras.getDouble("lon"),extras.getDouble("lat")));
    					Point p = new Point(extras.getDouble("lon"),extras.getDouble("lat"));
    					OverlayItem item = new OverlayItem(gp,(id.equals("0") ? "New annotation":
    							"Annotation #"+id),description);
    					item.setMarker(ItemizedOverlay.boundCenterBottom(getResources().getDrawable(R.drawable.annotation)));
    					dataDisplayer.addIconItem(item);
    					int idInt = id.equals("0")? -(annCacheMgr.size()+1):Integer.parseInt(id);
    					Annotation ann=new Annotation(idInt,p.x,p.y,description);
    			
    					mapView.invalidate();
    					if(idInt<0)
    					{
    						try
    						{
    							annCacheMgr.addAnnotation(ann); // adding in wgs84 latlon
    							Shared.pois.add(ann); // this reprojects it
    						}
    						catch(IOException e)
    						{
    							DialogUtils.showDialog(this,"Could not save annotation, please enable upload");
    						}
    					}
    				}
    				break;
    			case 2:
    				extras = i.getExtras();
    				POI poi = Shared.pois.getPOIById(Integer.parseInt(extras.getString("osmId")));
    				
    				if(poi!=null)
    					dataDisplayer.displayPOI(poi);
    				break;
    				
    			case 3:
    				extras = i.getExtras();
    				int idx = extras.getInt("selectedRoute"), wrId = Shared.walkroutes.get(idx).getId();
    				boolean loadSuccess=false;
    				
    				if(wrCacheMgr.isInCache(wrId))
    				{
    					try
    					{
    						Walkroute wr= wrCacheMgr.getWalkrouteFromCache(wrId);
    						loadSuccess=true;
    						setWalkroute(idx,wr);
    						
    					}
    					catch(Exception e)
    					{
    						DialogUtils.showDialog(this,"Unable to retrieve route from cache: " +
									e.getMessage()+". Loading from network");
    					}
    				}
    				if(!loadSuccess)
    				{
    					dataTask = new DownloadWalkrouteTask(this, this);
    					((DownloadWalkrouteTask)dataTask).execute(wrId, idx);
    				}
    				break;
    			
    			
    			case 4:
    				extras = i.getExtras();
    				String title = extras.getString("freemap.opentrail.wrtitle"),
    						description = extras.getString("freemap.opentrail.wrdescription"),
    						fname = extras.getString("freemap.opentrail.wrfilename");
    				
    				try
    				{
    					Walkroute recordingWalkroute = gpsService.getRecordingWalkroute();
    					if(recordingWalkroute!=null)
    					{
    						recordingWalkroute.setTitle(title);
    						recordingWalkroute.setDescription(description);
    						wrCacheMgr.addWalkrouteToCache(recordingWalkroute,fname);
    						if(!wrCacheMgr.deleteRecordingWalkroute())
    							DialogUtils.showDialog(this, "Unable to delete recorded walkroute file");
    						recordingWalkroute.clear();
    						dataDisplayer.clearWalkroute();
    						mapView.invalidate();
    					}
    					else
    					{
    						DialogUtils.showDialog(this, "No recorded walk route");
    					}
    				}
    				catch(IOException e)
    				{
    					DialogUtils.showDialog(this,"Cannot save walk route: " + e.getMessage());
    				}
    				break;
    			
    			case 5:
    				extras = i.getExtras();
    				String filename = extras.getString("freemap.opentrail.gpxfile");
    				uploadRecordedWalkroute(filename);
    				break;	
    		}
    	}
    }
    
    public void gotoMyLocation()
    {
    	if(this.location!=null)
    	{
    	
    		mapView.setCenter(this.location);
    	}
    }
    
    private void startPOIDownload(boolean showDialog, boolean forceWebDownload)
    {
    	if(this.location!=null)
    	{
    		if(dataTask==null || dataTask.getStatus()!=AsyncTask.Status.RUNNING)
    		{
            	dataTask = new DownloadPOIsTask(this, poiDeliverer, this, showDialog, forceWebDownload, location);
            	((DownloadPOIsTask)dataTask).execute();
    		}
    		else
    		{
    			DialogUtils.showDialog(this,"You're already downloading!");
    		}
    	}
    	else
    	{
    		DialogUtils.showDialog(this,"Location unknown");
    	}
    }
    
 
    
    public void loadAnnotationOverlay()
    {
    	if(dataDisplayer!=null)
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
    	
    	if(Shared.pois!=null)
    		saved.pois = Shared.pois;
    	
    	if(Shared.walkroutes!=null)
    		saved.walkroutes = Shared.walkroutes;
    	
    	return saved;
    }
    
    
    private String makeCacheDir(String projID)
    {
    	String cachedir = sdcard+"/opentrail/cache/" + projID.toLowerCase().replace("epsg:", "")+"/";
    	File dir = new File(cachedir);
    	if(!dir.exists())
    		dir.mkdirs();
    	return cachedir;
    }
    
    public void displayAnnotationInfo(String msg, int type, int alertId)
    {
    	new AlertDialog.Builder(this).setMessage(msg).setCancelable(false).setPositiveButton("OK",null).show();
    	
    	Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    	if(ringtoneUri!=null)
    	{
    	    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);
    	    r.play();
    	}
    	String summary = (type==AlertDisplay.ANNOTATION) ? "New walk note" : "New walk route stage";
    	NotificationManager mgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    	Notification notification = new Notification(R.drawable.marker,summary,
    	                                                System.currentTimeMillis());

    	Intent intent = new Intent(this,OpenTrail.class);
    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    	notification.setLatestEventInfo(this, summary, msg, pIntent);
    	
        mgr.notify(alertId,notification);
    }
    
    public void receiveLocation(Location loc)
    {
    	receiveLocation(loc.getLongitude(),loc.getLatitude(),false);
    }
    
    public void receiveLocation(double lon, double lat, boolean refresh)
    {	
    	
    	GeoPoint p = new GeoPoint(lat,lon);
		Point pt = new Point(lon,lat);
		
		
		if(refresh)
		{
			
			try
			{
				Walkroute recordingWalkroute = gpsService.getRecordingWalkroute();
				if(recordingWalkroute!=null && recordingWalkroute.getPoints().size()!=0)
					dataDisplayer.showWalkroute(recordingWalkroute);
				mapView.invalidate();
			}
			catch(Exception e)
			{
				Toast.makeText(this, "Unable to read GPS track for drawing", Toast.LENGTH_LONG).show();
			}
				
		}
			
				
		Point osgb = this.proj.project(pt);
		String gridsq = new OSRef(osgb.x,osgb.y).toSixFigureString().substring(0,2).toLowerCase();
		
		if(!curGridsq.equals(gridsq))
		{
			curGridsq = gridsq;
			File mf = new File(sdcard+"/opentrail/"+gridsq+".map");
			if(!mf.exists())
			{
				downloadMapFile(gridsq,"Map file for current location not present on device. ");
			}
			else if (mapView!=null)
			{
			    mapFile = mf.getAbsolutePath();
				setupMap(mf);
				// resetting the map file (e.g. one map file from the preferences, another
				// from the current location) resets the zoom - so use original zoom from preferences
				if(readZoom>=0)
					mapView.getController().setZoom(readZoom);
				mapView.redrawTiles();
			}
		}
    	this.location = new GeoPoint(lat,lon);
    	if(prefGPSTracking==true)
    	{
    		if(mapView!=null && mapSetup)
    			mapView.setCenter(p);
    	}
    	if(prefAutoDownload && poiDeliverer.needNewData(pt))
    	{
    		if(poiDeliverer.isCache(pt))
    			Toast.makeText(this, "Loading data from cache", Toast.LENGTH_SHORT).show();
    		else 
    			Toast.makeText(this, "Loading data from web", Toast.LENGTH_SHORT).show();
    		startPOIDownload(false, false);
    		
    	}
    	
    	alertDisplayMgr.update(pt);
    	
    }
   
    public void noGPS()
    {
    	//this.location=null;
    }

    public void receivePOIs(FreemapDataset ds)
    {
    	Shared.pois = ds;
    	waitingForNewPOIData=false;
    	//Log.d("OpenTrail","Received POIs:" + ds);
    	alertDisplayMgr.setPOIs(Shared.pois);
    	loadAnnotationOverlay();
    }
    
    public void receiveWalkroutes(ArrayList<Walkroute> walkroutes)
    {
    	Shared.walkroutes = walkroutes;
    }
   
    public void receiveWalkroute(int idx, Walkroute walkroute)
    {
    	
    	setWalkroute(idx,walkroute);
  
    	try
    	{
    		this.wrCacheMgr.addWalkrouteToCache(walkroute);
    	}
    	catch(IOException e)
    	{
    		DialogUtils.showDialog(this,"Downloaded walk route, but unable to save to cache: " +
    												e.getMessage());
    	}
    }
    
    private void setWalkroute(int idx, Walkroute walkroute)
    {
      	walkrouteIdx = idx;
		Shared.walkroutes.set(idx, walkroute);
    	alertDisplayMgr.setWalkroute(walkroute);
    	dataDisplayer.showWalkroute(walkroute);
    }
    
    public void uploadCachedAnnotations()
    {
    	if(annCacheMgr.isEmpty())
    	{
    		DialogUtils.showDialog(this,"No annotations to upload");
    	}
    	else
    	{
    		try
    		{
    			String xml = annCacheMgr.getAllAnnotationsXML();
    			ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
    			postData.add(new BasicNameValuePair("action","createMulti"));
    			postData.add(new BasicNameValuePair("inProj","4326"));
    			postData.add(new BasicNameValuePair("data",xml));
    			
    			dfTask = new HTTPUploadTask
    				(this,  "http://www.free-map.org.uk/0.6/ws/annotation.php",
    						 postData ,
    						"Upload annotations?", this, 2);
    			dfTask.setDialogDetails("Uploading...", "Uploading annotations...");
    			dfTask.confirmAndExecute();
    			
    		}
    		catch(IOException e)
    		{
    			DialogUtils.showDialog(this,"Error retrieving cached annotations: " + e.getMessage());
    		}
    	} 	
    }
    
    public void showWalkrouteDetailsActivity()
    {
    	Intent intent = new Intent (this, WalkrouteDetailsActivity.class);
    	startActivityForResult(intent, 4);
    }
    
    public void showRecordedWalkroutesActivity()
    {
    	Intent intent = new Intent(this, RecordedWalkroutesListActivity.class);
    	startActivityForResult(intent, 5);
    }
    
    public void uploadRecordedWalkroute(String wrFile)
    {
    	try
    	{
    		final Walkroute walkroute = this.wrCacheMgr.getRecordedWalkroute(wrFile);
    		if(walkroute!=null && walkroute.getPoints().size()>0)
    		{
    			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    			if(prefs.getString("prefUsername", "").equals("") || prefs.getString("prefPassword","").equals(""))
    			{
    				new AlertDialog.Builder(this).setMessage("WARNING: Username and password not specified in the preferences." +
    									" Walk route will still be uploaded but will need to be authorised before becoming "+
    									"visible.").setPositiveButton("OK", 
    											
    											new DialogInterface.OnClickListener()
    											{
    												public void onClick(DialogInterface i, int which)
    												{
    													doUploadRecordedWalkroute(walkroute);
    												}
    											}
    											
    											).setNegativeButton("Cancel",null).show();
    			}
    			else
    				doUploadRecordedWalkroute(walkroute);
    		}
    	}
    	catch(Exception e)
    	{
    		DialogUtils.showDialog(this,"Error obtaining walk route: " + e.getMessage());
    	}
    }
    
    private void doUploadRecordedWalkroute(Walkroute walkroute)
    {
    	final Walkroute wr=walkroute;
    	if(walkroute!=null)
    	{
    		
    		new AlertDialog.Builder(this).setPositiveButton("OK", 
    					new DialogInterface.OnClickListener()
    					{
    						public void onClick(DialogInterface i, int which)
    						{
    							String gpx = wr.toXML();
    				    		ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
    				    		postData.add(new BasicNameValuePair("action","add"));
    				    		postData.add(new BasicNameValuePair("route", gpx));
    				    		postData.add(new BasicNameValuePair("format", "gpx"));
    				    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    				    		String username = prefs.getString("prefUsername",""), password = prefs.getString("prefPassword", "");
    							dfTask = new HTTPUploadTask(OpenTrail.this,"http://www.free-map.org.uk/0.6/ws/wr.php", postData, 
										"Upload walk route?", OpenTrail.this, 3);
    							if(!(username.equals("")) && !(password.equals("")))
    								((HTTPUploadTask)dfTask).setLoginDetails(username, password);
    							dfTask.setDialogDetails("Uploading...", "Uploading walk route...");
    							dfTask.confirmAndExecute();
    						}
    					} ).setCancelable(true).
    					setMessage("No simplification on track done yet, "+
    							"tracks typically 1 kilobyte/minute. Still upload?").
    					setNegativeButton("Cancel",null).show();
    		
    	}
    }
    
    public boolean onTouch(View mv, MotionEvent ev)
    {
    	
    	boolean retcode=false;
    	
    	switch(ev.getAction())
    	{
    		case MotionEvent.ACTION_DOWN:
    			xDown = (int)ev.getX();
    			yDown = (int)ev.getY();
    			break;
    		case MotionEvent.ACTION_UP:
    			final int x = (int)ev.getX(), y=(int)ev.getY();
    			if(Math.abs(x-xDown)<5 && Math.abs(y-yDown)<5)
    			{
    				// get ontouch location
    				new AlertDialog.Builder(this).setMessage("Add an annotation at this location?").
    					setNegativeButton("Cancel",null).
    					setPositiveButton("OK", new DialogInterface.OnClickListener()
    					{
    						public void onClick(DialogInterface i, int which)
    						{
    							GeoPoint p = mapView.getProjection().fromPixels(x,y);
    							launchInputAnnotationActivity(p.getLatitude(),p.getLongitude());
    						}
    					}).show();
    				
    				retcode=true;
    			}
    			xDown = yDown = -256;
    			break;
    	}
    	return retcode;
    	
    }
    
    
    
    public void about()
    {
    	DialogUtils.showDialog(this,"OpenTrail 0.1-alpha. Uses OpenStreetMap data, copyright 2012 " +
    											"OpenStreetMap contributors, Open Database Licence. Uses " +
    											"Ordnance Survey OpenData LandForm Panorama contours, Crown Copyright.");
    }
    
    public void onSaveInstanceState(Bundle state)
    {
      	GeoPoint gp = mapView.getMapPosition().getMapCenter();
    	state.putDouble("lat", gp.getLatitude());
    	state.putDouble("lon", gp.getLongitude());
    	state.putInt("zoom",mapView.getMapPosition().getZoomLevel());
    	state.putString("mapFile",mapFile);
    	state.putInt("walkrouteIdx", walkrouteIdx);
    	state.putBoolean("recordingWalkroute", recordingWalkroute);
    	state.putBoolean("waitingForNewPOIData",waitingForNewPOIData);
     	state.putInt("recordingWalkrouteId", recordingWalkrouteId);
     	Log.d("OpenTrail","Saving zoom: " + mapView.getMapPosition().getZoomLevel());
    }
}