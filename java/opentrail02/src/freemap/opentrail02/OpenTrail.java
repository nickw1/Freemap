package freemap.opentrail02;

// !!! check that tile layer is not null before trying to do a requestRedraw()

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;
import org.mapsforge.map.android.graphics.AndroidResourceBitmap;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.model.MapViewPosition;




import java.io.File;
import java.io.FileNotFoundException;

// FROM EXISTING OPENTRAIL

import android.preference.PreferenceManager;
import android.content.Context;


import java.io.IOException;
import android.util.Log;


import java.util.ArrayList;





import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.ComponentName;

import android.view.MenuItem;
import android.view.Menu;
import android.view.MenuInflater;




import android.location.Location;

import android.os.AsyncTask;
import android.location.LocationManager;
import android.content.IntentFilter;
import android.net.Uri;
import android.media.Ringtone;
import android.media.RingtoneManager;



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
import freemap.datasource.CachedTileDeliverer;
import freemap.datasource.WebDataSource;
import freemap.datasource.XMLDataInterpreter;
import freemap.datasource.AnnotationCacheManager;
import freemap.datasource.WalkrouteCacheManager;

import freemap.andromaps02.MapLocationProcessor;
import freemap.andromaps02.DataCallbackTask;
import freemap.andromaps02.DownloadBinaryFilesTask;
import freemap.andromaps02.DownloadTextFilesTask;
import freemap.andromaps02.HTTPUploadTask;
import freemap.andromaps02.HTTPCommunicationTask;
import freemap.andromaps02.DialogUtils;
import freemap.andromaps02.MapsforgeUtil;


import freemap.proj.OSGBProjection;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
// END



public class OpenTrail extends Activity implements AlertDisplay, MapLocationProcessor.LocationReceiver,
			DataReceiver, HTTPCommunicationTask.Callback
{

	// FROM EXISTING OPENTRAIL
	String mapFile, sdcard, styleFile;
	MapView mapView;
	MapLocationProcessorBR mapLocationProcessor;
	IntentFilter filter;
	
	boolean tracking;
	
	FreemapFileFormatter formatter;
	CachedTileDeliverer poiDeliverer;
	String cachedir;
	boolean prefGPSTracking, prefAutoDownload, prefAnnotations, recordingWalkroute;
	Drawable personIcon, annotationIcon, markerIcon;
	
	String curGridsq;

	AlertDisplayManager alertDisplayMgr;
	AnnotationCacheManager annCacheMgr;
	
	
	OverlayHandler overlayHandler;
	
	DataCallbackTask<?,?> dataTask;
	HTTPCommunicationTask dfTask;
	
	int walkrouteIdx, localAnnId, recordingWalkrouteId;
	int xDown = -256, yDown = -256;
	long lastWRUpdateTime = 0, touchTime = 0;
	int readZoom = -1;
	
	boolean mapSetup=false,waitingForNewPOIData=false;
	
	
	
	
	LatLong initPos;
	ServiceConnection gpsServiceConn;
	GPSService gpsService;
	WalkrouteCacheManager wrCacheMgr;
	LatLong location;
	
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
	// END
	
	
	TileCache tileCache;
	TileRendererLayer renderLayer;
	ExternalRenderTheme renderTheme;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		AndroidGraphicFactory.createInstance(this.getApplication());
		 
		mapView = new MapView(this);
	
		setContentView(mapView);
		mapView.setBuiltInZoomControls(true);
		mapView.setClickable(true);
	
		
		
		// Used to cache tiles to avoid re-rendering
		tileCache = AndroidUtil.createTileCache
					(this, "mapcache",
						mapView.getModel().displayModel.getTileSize(),
						1f,
						mapView.getModel().frameBufferModel.getOverdrawFactor());
		try
        {
        	sdcard = Environment.getExternalStorageDirectory().getAbsolutePath() ;
        	//sdcard="/mnt/extSdCard";
        	File opentrailDir = new File(sdcard+"/opentrail");
        	if(!opentrailDir.exists())
        		opentrailDir.mkdir();
        
        	mapFile = null;
        	
       	
        	
        	String projString="epsg:27700";
        	//Proj4ProjectionFactory factory=new Proj4ProjectionFactory();
    		this.proj = new OSGBProjection(); // factory.generate(projString);
        
    		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    		SharedPreferences.Editor ed = p.edit();
    		ed.putString("wrCacheLoc", sdcard+"/opentrail/walkroutes/");
    		ed.commit();
    		
    		
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
        	
        	
        	
        	overlayHandler = new OverlayHandler(getApplicationContext(),mapView,
					getResources().getDrawable(R.drawable.person),
					getResources().getDrawable(R.drawable.marker),
					getResources().getDrawable(R.drawable.annotation),
					this.proj);
        	
        	SharedPreferences prefs;
        	
        	if(savedInstanceState!=null)
        	{
        		initPos = new LatLong(savedInstanceState.getDouble("lat"),
        								savedInstanceState.getDouble("lon"));
        		readZoom = savedInstanceState.getInt("zoom");
        		
       			mapView.getModel().mapViewPosition.setZoomLevel((byte)readZoom);
       			mapFile = savedInstanceState.getString("mapFile");
       			walkrouteIdx = savedInstanceState.getInt("walkrouteIdx");
       			recordingWalkroute = savedInstanceState.getBoolean("recordingWalkroute");
       			waitingForNewPOIData = savedInstanceState.getBoolean("waitingForNewPOIData");
       		}   	
        	else if ((prefs=PreferenceManager.getDefaultSharedPreferences(getApplicationContext()))!=null)
        	{
        	    
        		initPos = new LatLong(prefs.getFloat("lat",51.05f),
        							prefs.getFloat("lon", -0.72f));
        		//mapFile = prefs.getString("mapFile",null);
        		mapFile = prefs.getString("mapFile", sdcard+"/opentrail/su.map");
        		Log.d("newmapsforge", "mapFile=" + mapFile);
        	
        		readZoom = prefs.getInt("zoom", -1);
        		recordingWalkroute = prefs.getBoolean("recordingWalkroute", false);    
        		waitingForNewPOIData = prefs.getBoolean("waitingForNewPOIData", false);
        		
        		if(readZoom>=0)
        		{
        			mapView.getModel().mapViewPosition.setZoomLevel((byte)readZoom);
        		}
        		
        	}
        	
        	
    		cachedir=makeCacheDir(projString);
    		FreemapFileFormatter formatter=new FreemapFileFormatter(this.proj.getID());
    		formatter.setScript("bsvr.php");
    		formatter.selectPOIs("place,amenity,natural");
    		formatter.selectAnnotations(true);
    		WebDataSource ds=new WebDataSource("http://www.free-map.org.uk/0.6/ws/",formatter);
    		poiDeliverer=new CachedTileDeliverer("poi",ds, new XMLDataInterpreter
    				(new FreemapDataHandler()),5000,5000,this.proj,cachedir);
    		poiDeliverer.setCache(true);
    		poiDeliverer.setReprojectCachedData(true);
    		
    		
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
    	
    		
    		mapLocationProcessor=new MapLocationProcessorBR(this,this,overlayHandler);
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
        		//	gpsService = null; does this cause garbage collector to clean up service?
        		}
        	};
        	
        	Intent bindServiceIntent = new Intent(this,GPSService.class);
       	 	bindService(bindServiceIntent, gpsServiceConn, Context.BIND_AUTO_CREATE);
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
        	
        	Log.d("newmapsforge", "downloading style file");
        	downloadStyleFile();
        	registerReceiver(mapLocationProcessor,filter);
        }
        catch(Exception e)
        {
        	DialogUtils.showDialog(this,"Error:" + e.getMessage() + " " + e);
        	Log.d("newmapsforge", e.getStackTrace().toString());
        	e.printStackTrace();
        	
        }
      
		
	}
	
	protected void onStart()
	{
		super.onStart();
		android.util.Log.d("newmapsforge", "onStart()");
	
		// newmapsforge - setup tile renderer layer
		/* remove this - was just done for testing
		mapView.getModel().mapViewPosition.setCenter(new LatLong(51.05, -0.72));
		mapView.getModel().mapViewPosition.setZoomLevel((byte)14);
		*/
		
		// renderer layer. can also have marker layers etc. must associate with cache
				// false= transparency
				// true = render labels
		android.util.Log.d("newmapsforge", "onStart(): setting up tile renderer layer");
		renderLayer = getTileRendererLayer
						(tileCache, mapView.getModel().mapViewPosition, false, true,
							AndroidGraphicFactory.INSTANCE);
		
		
		
		
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
     	boolean oldPrefAnnotations = prefAnnotations;
     	
     	prefGPSTracking = prefs.getBoolean("prefGPSTracking", true);
     	prefAnnotations = prefs.getBoolean("prefAnnotations", true);
     	prefAutoDownload = prefs.getBoolean("prefAutoDownload", false);
     	
     	
     	
     	if(Shared.walkroutes!=null && walkrouteIdx > -1 && walkrouteIdx < Shared.walkroutes.size())
		 	overlayHandler.setWalkroute(Shared.walkroutes.get(walkrouteIdx));
		 	
     	if(overlayHandler==null)
     	    Log.d("newmapsforge","WARNING data displayer is null");
     	
     	
     
     	
     	if (prefAnnotations==true)
     		loadAnnotationOverlay();
     		
     	
     
     	try
     	{
     		Log.d("newmapsforge", "setting render theme");
     		renderTheme = new ExternalRenderTheme(new File(sdcard + "/opentrail/freemap_v4.xml"));
     		renderLayer.setXmlRenderTheme(renderTheme);
     		Log.d("newmapsforge", "adding render layer to layers");
     		
    
     	
     		// check mapfile exists
     		File mf = mapFile==null ? null:new File(mapFile);
     		if(!mf.exists())
     			mapFile=null;
     		
     		if(mapSetup)
     		{
     			Log.d("newmapsforge", "map setup : mapfile = " + mapFile);
     			// redrawing bombs with no map file. setupMap() will do it otherwise
     		 	
         		// crashes if no mapfile
         		overlayHandler.addTileRendererLayer(renderLayer);
     			renderLayer.setMapFile(new File(mapFile));
     
     	
     
     			//mapView.invalidate();
     	    
     	    
     			overlayHandler.addAllOverlays();
     			Log.d("newmapsforge", "mapsetup: requesting redeaw done");
     		}
     		else if(mapFile!=null)
     		{
     			Log.d("newmapsforge", "setting up map with a file of " + mapFile);
     			setupMap(mf);
     		}
     		//services can be both started and bound
     		//http://developer.android.com/guide/components/bound-services.html
     		//we need this as the activity requires data from the service, but we
     		//also need the service to keep going once the activity finishes
     		Intent startServiceIntent = new Intent(this,GPSService.class);
     		startServiceIntent.putExtra("wrCacheLoc",sdcard+"/opentrail/walkroutes/");
     		startServiceIntent.putExtra("recordingWalkroute", recordingWalkroute);
     		startService(startServiceIntent);
     	}
     	catch(FileNotFoundException e) // should never happen
     	{
     		DialogUtils.showDialog(this, "can't find style file!");
     	}
	}
	
	protected void onStop()
	{
		super.onStop();    	
    	Intent stopIfNotLoggingBroadcast = new Intent("freemap.opentrail.stopifnotlogging");
    	sendBroadcast(stopIfNotLoggingBroadcast);
    	if(mapSetup)
    	{
    		overlayHandler.removeAllOverlays(false);
	
    		renderLayer.onDestroy();
    	}
	}
    	
	
	protected void onDestroy()
	{
		super.onDestroy();
		
		unregisterReceiver(mapLocationProcessor);
    	unbindService(gpsServiceConn);
    	
    	
    	overlayHandler.cleanup();
    	//mapLocationProcessor=null;
    	//alertDisplayMgr=null;
    	// 230213 no need for this and causes crashes dataDisplayer=null;
    	
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	SharedPreferences.Editor editor = settings.edit();
    	
    	
    	editor.putFloat("lat",(float)mapView.getModel().mapViewPosition.getCenter().latitude);
    	editor.putFloat("lon",(float)mapView.getModel().mapViewPosition.getCenter().longitude);
    	editor.putInt("zoom", mapView.getModel().mapViewPosition.getZoomLevel());
    	editor.putBoolean("recordingWalkroute", recordingWalkroute);
    	editor.putBoolean("waitingForNewPOIData",waitingForNewPOIData);
    	editor.putString("mapFile",mapFile);
    	editor.commit();
    	// 230213 no need for this and causes crashes mapView=null;
    	
    		tileCache.destroy();
    		mapView.getModel().mapViewPosition.destroy();
    		
			mapView.destroy();
			AndroidResourceBitmap.clearResourceBitmaps();
    	
	}
	
    public void downloadStyleFile()
    {
    	styleFile = sdcard+"/opentrail/freemap_v4.xml";
    	File sf = new File(styleFile);
    	if(!sf.exists())
    	{
    		dfTask = new DownloadTextFilesTask(this,  new String[] 
    		                                   { "http://www.free-map.org.uk/data/android/freemap_v4.xml" }, 
    		                           new String[] { styleFile }, 
    		                           "No Freemap style file (version 4) found. Download?", this, 0);
    		dfTask.setDialogDetails("Downloading...","Downloading style file...");
    		dfTask.confirmAndExecute();
    	}
    	
    }
    
    public void setupMap(File mf)
    {
    	setupMap(mf,true);
    }
    
    public void setupMap (File mf, boolean addTileLayer)
    {
    	
    		// onStart() from newmapsforge example
    		
    		
    		if(mf.exists())
    		{
    			android.util.Log.d("newmapsforge", "FILE WE WANT EXISTS, mapfile=" + mf);	
    			renderLayer.setMapFile(mf);
    		
    		}
    		else
    		{
    			new AlertDialog.Builder(this).setPositiveButton("OK",null).setMessage("can't find mapfile").show();
    		}
    		
    		// add the layer to the map
    		Log.d("newmapsforge", "adding layer to map");
    		// done onstart mapView.getLayerManager().getLayers().add(layer);
    		   		
    		if(!mapSetup)
			{
				Log.d("newmapsforge", "setting content view");
	    		
				setContentView(mapView);
				
				mapSetup=true;
			}
    		Log.d("newmapsforge", "setting centre");
    		if(this.location!=null)
				mapView.getModel().mapViewPosition.setCenter(this.location);
			else if (initPos!=null)
				mapView.getModel().mapViewPosition.setCenter(initPos);

    	 	
     		// crashes if no mapfile
    		if(addTileLayer)
    		{
    			overlayHandler.addTileRendererLayer(renderLayer);
    			Log.d("newmapsforge", "loading annotation overlay");
        		loadAnnotationOverlay();
        		overlayHandler.addAllOverlays();	
    		}
     		
    		mapView.invalidate();
    		renderLayer.requestRedraw(); 
    		
    }
    
    public void downloadFinished(int id, Object addData)
    {
    	switch(id)
    	{
    		case 0:
    			if(mapFile!=null)
    			{
    			    File mf = new File(mapFile);
    			    if(mf.exists())
    			        setupMap(mf);
    			}
    			break;
    			
    		case 1:
    		    if(addData!=null)
                {
                        Point osgb=null;
                        String gsq="";
                        if(this.location!=null)
                        {
                                
                            osgb = this.proj.project(new Point(this.location.longitude,this.location.latitude));
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
    		    Log.d("OpenTrail","server sent back: " + addData);
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
    		downloadMapFile(this.location.longitude,this.location.latitude);
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
    
    public void downloadMapFile(String mapName, String addMsg)
    {		
    	dfTask = new DownloadBinaryFilesTask(this,  new String[] 
                    { "http://www.free-map.org.uk/data/android/"+mapName+".map" }, 
                    	new String[] { sdcard+"/opentrail/"+mapName+".map" }, 
                    	addMsg + "Download "+mapName+".map? Warning: 150-300+MB file if downloading all of England " +
                    	            "or Wales; wifi extremely strongly advised!", this, 1);
    	dfTask.setAdditionalData(mapName);
    	dfTask.setDialogDetails("Downloading...","Downloading map file...");
    	dfTask.confirmAndExecute();
    }
    
    public void showMapDialog()
    {
    	final String[] values = { "england","wales",
    	                          "sw","sx","sy","sz","tv",
    							  "ss","st","su","tq","tr",
    							  "so","sp","tl","tm",
    							  "sj","sk","tf","tg",
    							  "sd","se","ta",
    							  "nx","ny","nz" };
    							  
    
    	new AlertDialog.Builder(this).setTitle("Select the map to download.").
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
    	else if (item.getItemId()==R.id.downloadMapMenuItem)
    	{
    	    showMapDialog();
    	}
    	else if (item.getItemId()==R.id.selectMapMenuItem)
    	{
    	    Intent intent = new Intent(this,FileChooser.class);
            startActivityForResult(intent, 0);
    	}
    	else if (item.getItemId()==R.id.downloadLocalMapMenuItem)
    	{
    	    downloadLocalMapFile();
    	}
    	else if(!mapSetup)
    	{
    		DialogUtils.showDialog(this,"Cannot perform this action until a map is loaded.");
    		retcode=false;
    	}
    	else
    	{
    		Intent intent = null;
    		
    		switch(item.getItemId())
    		{	
    			case R.id.myLocationMenuItem:
    				gotoMyLocation();
    				break;
    			
    			case R.id.inputAnnotationMenuItem:
    				if(this.location!=null)
    					launchInputAnnotationActivity(this.location.latitude,
    						this.location.longitude);
    				else
    				{
    				    
    				    this.location = new LatLong(50.9, 1.4);
    				    
    					DialogUtils.showDialog(this,"Location not known yet");
    				}
    			
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
    					Point p = this.proj.project(new Point(location.longitude,location.latitude));
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
    			
    				if(gpsService.getRecordingWalkroute()!=null)
    				    Log.d("OpenTrail", "recording/stop recording: walkroute stage size="  + gpsService.getRecordingWalkroute().getStages().size());
    				
    				if(recordingWalkroute)
    				{
    					
    					overlayHandler.removeWalkroute(true);
    					if(mapSetup)
    					{
    					  
    					
    					    mapView.invalidate();
    					}
    				
    					
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
    		extras.putBoolean("recordingWalkroute", recordingWalkroute);
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
    				LatLong currentCentre = mapView.getModel().mapViewPosition.getCenter();
    				Bundle extras = i.getExtras();
    	
    				
    				mapFile = sdcard+"/opentrail/"+extras.getString("mapFile");
    				
    				File mf=new File(mapFile);
    				if(mf.exists())
    				{
    					// necessary to stop old cache hanging around which draws tiles from cache, not from new mapfile
    					Log.d("newmapsforge", "replaceTileLayer()");
    					replaceTileLayer();
    					Log.d("newmapsforge", "resetting mapfile to : " + mapFile);
    					setupMap(mf,true);
    					mapView.getModel().mapViewPosition.setCenter(currentCentre);
    					gotoMyLocation();
    					renderLayer.requestRedraw(); 
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
    				    boolean isWalkrouteAnnotation = extras.getBoolean("walkrouteAnnotation");
    				    
    					String id=extras.getString("ID"),description=extras.getString("description");
    					LatLong gp = new LatLong(extras.getDouble("lat"),extras.getDouble("lon"));
    					//Point p = this.proj.project(new Point(extras.getDouble("lon"),extras.getDouble("lat")));
    					Point p = new Point(extras.getDouble("lon"),extras.getDouble("lat"));
    					                 
    					Marker item = MapsforgeUtil.makeTappableMarker(this, isWalkrouteAnnotation ?
					           getResources().getDrawable(R.drawable.marker) : 
					           getResources().getDrawable(R.drawable.annotation) , gp, description);
    							
    					/*
    							gp,(id.equals("0") ? "New annotation":
    							"Annotation #"+id),description);
    							*/
    				
    					mapView.getLayerManager().getLayers().add(item);
    					int idInt = id.equals("0")? -(annCacheMgr.size()+1):Integer.parseInt(id);
    				
    			
    					
    					mapView.invalidate();
    					
    					Walkroute curWR = gpsService.getRecordingWalkroute();
    					
                        if(isWalkrouteAnnotation && this.recordingWalkroute && curWR!=null)
                        {
                            curWR.addStage(p, description);
                            
                            Log.d("OpenTrail", "Added walkroute annotation. Size of stages=" + curWR.getStages().size());
                        }
    					
                        else if(idInt<0)
    					{
    						try
    						{
    						    Annotation ann=new Annotation(idInt,p.x,p.y,description);
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
    					overlayHandler.setPOI(poi);
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
    				
    				
    					Walkroute recordingWalkroute = gpsService.getRecordingWalkroute();
    					if(recordingWalkroute!=null)
    					{
    						recordingWalkroute.setTitle(title);
    						recordingWalkroute.setDescription(description);
    						
    					
    					
    						AsyncTask<String,Void,Boolean> addToCacheTask = new AsyncTask<String,Void,Boolean>()
    						{
    						    
    						    Walkroute recWR;
    						    String errMsg; // stat;
    						    public Boolean doInBackground(String...fname)
    						    {
    					            recWR = gpsService.getRecordingWalkroute();
                                    
                                   //stat = "starting doInBackground";
                                    
    					
    						        try
    						        {
    						            wrCacheMgr.addRecordingWalkroute(recWR);
    						            wrCacheMgr.renameRecordingWalkroute(fname[0]);
    						            gpsService.clearRecordingWalkroute();
    						            //stat += " done.";
    						        }
    						        catch(IOException e)
    						        {
    						            errMsg = e.toString();
    						            return false;
    						        }
    						        return true;
    						    }
    						    
    						    protected void onPostExecute(Boolean result)
    						    {
    						        if(!result)
    						            DialogUtils.showDialog(OpenTrail.this, "Unable to save walk route: error=" + errMsg);
    						        else
    						        {
    						            overlayHandler.removeWalkroute(true);
    		                        //    mapView.invalidate();
    		                            DialogUtils.showDialog(OpenTrail.this, "Successfully saved walk route.");
    						        }
    						    }
    						};
    						addToCacheTask.execute(fname);
    						
    					}
    					else
    					{
    						DialogUtils.showDialog(this, "No recorded walk route");
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
    		mapView.getModel().mapViewPosition.setCenter(this.location);
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
    	}
    	else
    	{
    		DialogUtils.showDialog(this,"Location unknown");
    	}
    }
    
 
    
    public void loadAnnotationOverlay()
    {
    	if(overlayHandler!=null)
    		Shared.pois.operateOnAnnotations(overlayHandler);
    	overlayHandler.requestRedraw();
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
    
    	Log.d("newmapsforge", "receiving location: lon=" + lon + " lat=" + lat);
    	LatLong p = new LatLong(lat,lon);
		Point pt = new Point(lon,lat);
		
		
		if(refresh)
		{
			
			try
			{
				Walkroute recordingWalkroute = gpsService.getRecordingWalkroute();
				if(recordingWalkroute!=null && recordingWalkroute.getPoints().size()!=0)
					overlayHandler.setWalkroute(recordingWalkroute);
				
				
			}
			catch(Exception e)
			{
				Toast.makeText(this, "Unable to read GPS track for drawing", Toast.LENGTH_LONG).show();
			}
				
		}
	
		
				
		Point osgb = this.proj.project(pt);
		String gridsq = new OSRef(osgb.x,osgb.y).toSixFigureString().substring(0,2).toLowerCase();
		
		// If the current map file is a grid square map file, and we change grid square,
		// change the map (or prompt user to download new map). This will not happen if we
		// are using england.map or wales.map.
		Log.d("newmapsforge", "curGridsq=" + curGridsq + " gridsq=" + gridsq + " mapFile=" + mapFile);
		if(!curGridsq.equals(gridsq) && (mapFile==null ||
		        mapFile.equals(sdcard+"/opentrail/"+curGridsq+".map")))
		{
			Log.d("newmapsforge", "YES");
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
					mapView.getModel().mapViewPosition.setZoomLevel((byte)readZoom);
				//mapView.redrawTiles();
				//renderLayer.requestRedraw();
			}
		}
    	this.location = new LatLong(lat,lon);
    	if(prefGPSTracking==true)
    	{
    	    
    		if(mapView!=null && mapSetup)
    			mapView.getModel().mapViewPosition.setCenter(p);
    	}
    	if(prefAutoDownload && poiDeliverer.needNewData(pt))
    	{
    		if(poiDeliverer.isCache(pt))
    			Toast.makeText(this, "Loading data from cache", Toast.LENGTH_SHORT).show();
    		else 
    			Toast.makeText(this, "Loading data from web", Toast.LENGTH_SHORT).show();
    		startPOIDownload(false, false);
    		
    	}
    	
    	overlayHandler.requestRedraw();
    	alertDisplayMgr.update(pt);
    	
    }
   
    public void noGPS()
    {
    	//this.location=null;
    }

    public void receivePOIs(FreemapDataset ds)
    {
        if(ds!=null)
        {
            Shared.pois = ds;
            
            waitingForNewPOIData=false;
            alertDisplayMgr.setPOIs(Shared.pois);
            loadAnnotationOverlay();
        }
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
    	overlayHandler.setWalkroute(walkroute);
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
    	
    	if(walkroute!=null)
    	{
    		
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    		String username = prefs.getString("prefUsername",""), password = prefs.getString("prefPassword", "");
    		float dpDist = Float.parseFloat(prefs.getString("prefDPDist", "5.0"));
    		dfTask = new WRUploadTask(OpenTrail.this,walkroute,
    		           "http://www.free-map.org.uk/0.6/ws/wr.php",  
										"Upload walk route?", OpenTrail.this, 3, dpDist);
    		
    		
    		if(!(username.equals("")) && !(password.equals("")))
    		    ((HTTPUploadTask)dfTask).setLoginDetails(username, password);
    		
    	    dfTask.setDialogDetails("Uploading...", "Uploading walk route...");
    		dfTask.confirmAndExecute();
    	}
    }
    
    // I don't think there's a way to clear the cache (when changing map files) without doing this
    public void replaceTileLayer()
    {
    	
    	
    	
    	tileCache.destroy();
    	overlayHandler.removeAllOverlays(false);  	
    	renderLayer.onDestroy();
    	
    	tileCache = AndroidUtil.createTileCache
				(this, "mapcache",
					mapView.getModel().displayModel.getTileSize(),
					1f,
					mapView.getModel().frameBufferModel.getOverdrawFactor());
    	renderLayer = getTileRendererLayer
				(tileCache, mapView.getModel().mapViewPosition, false, true,
					AndroidGraphicFactory.INSTANCE);
    	renderLayer.setXmlRenderTheme(renderTheme);
    	
		
    	// overlayHandler.addTileRendererLayer() and addAllOverlays() done in setupMap() (called after this) so not done here
    }
   
    public TileRendererLayer getTileRendererLayer(TileCache tileCache, MapViewPosition tilePosition, boolean transparent, boolean labels,
			AndroidGraphicFactory factory)
    {

    	return new TileRendererLayer(tileCache, tilePosition, transparent, labels, factory)
    	{
    		LatLong tp;
    	
    		public boolean onLongPress(LatLong tapPos, org.mapsforge.core.model.Point layerXY, 
								org.mapsforge.core.model.Point xy)
    		{
    			tp = tapPos;
    			
    			new AlertDialog.Builder(OpenTrail.this).setMessage("Add an annotation at this location?").
					setNegativeButton("Cancel",null).
					setPositiveButton("OK", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface i, int which)
						{
							OpenTrail.this.launchInputAnnotationActivity(tp.latitude, tp.longitude);
						}
					}).show();
		
    			return true;
    		}
		};
    }
    
    public void about()
    {
      
    	DialogUtils.showDialog(this,"OpenTrail 0.2-buggy-pre-alpha, using Mapsforge 0.5. Uses OpenStreetMap data, copyright 2013 " +
    											"OpenStreetMap contributors, Open Database Licence. Uses " +
    											"Ordnance Survey OpenData LandForm Panorama contours, Crown Copyright." +
    											"Person icon taken from the osmdroid project. Annotation icon based on " +
    											"OpenStreetMap viewpoint icon.");
    }
    
    public void onSaveInstanceState(Bundle state)
    {
        
      	LatLong gp = mapView.getModel().mapViewPosition.getCenter();
    	state.putDouble("lat", gp.latitude);
    	state.putDouble("lon", gp.longitude);
    	state.putInt("zoom",mapView.getModel().mapViewPosition.getZoomLevel());
    	state.putString("mapFile",mapFile);
    	state.putInt("walkrouteIdx", walkrouteIdx);
    	state.putBoolean("recordingWalkroute", recordingWalkroute);
    	state.putBoolean("waitingForNewPOIData",waitingForNewPOIData);
     	state.putInt("recordingWalkrouteId", recordingWalkrouteId);
     	
    }   
}
