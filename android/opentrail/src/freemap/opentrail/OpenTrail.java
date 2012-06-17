package freemap.opentrail;

// Credits for icons
// res/drawable/person.png is taken from osmdroid.
// res/drawable/annotation.png is modified from the standard OSM viewpoint icon.

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import java.io.File;
import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.core.GeoPoint;
import android.app.AlertDialog;
import java.io.IOException;
import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.MenuItem;
import android.view.Menu;
import android.view.MenuInflater;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import org.mapsforge.android.maps.overlay.ArrayItemizedOverlay;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.android.maps.overlay.ItemizedOverlay;
import android.graphics.drawable.Drawable;
import android.content.DialogInterface;
import java.util.HashMap;

import freemap.data.Annotation;
import freemap.data.POI;
import freemap.data.Point;
import freemap.datasource.FreemapDataHandler;
import freemap.datasource.FreemapDataset;
import freemap.datasource.FreemapFileFormatter;
import freemap.datasource.TileDeliverer;
import freemap.datasource.WebDataSource;
import freemap.datasource.XMLDataInterpreter;

import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
import java.io.PrintWriter;



public class OpenTrail extends MapActivity implements freemap.datasource.FreemapDataset.AnnotationVisitor{
	
	String mapFile, sdcard;
	MapView mapView;
	GPSListener gpsListener;
	ArrayItemizedOverlay overlay;
	OverlayItem myLocOverlayItem, lastAddedPOI;
	boolean tracking;
	DownloadPOIsThread t;
	DownloadHandler handler;
	FreemapFileFormatter formatter;
	TileDeliverer poiDeliverer;
	String cachedir;
	boolean prefGPSTracking, prefAutoDownload, prefAnnotations;
	Drawable personIcon, annotationIcon, markerIcon;
	HashMap<Integer,OverlayItem> indexedAnnotations;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
  
        super.onCreate(savedInstanceState);
        try
        {
        	sdcard = Environment.getExternalStorageDirectory().getAbsolutePath() + "/opentrail";
        	
        	mapFile = sdcard + "/west_sussex.map";
        	mapView = new MapView(this); 
        	mapView.setClickable(true);
        	mapView.setBuiltInZoomControls(true);
        	
    
        	
        	
        	File styleFile = new File (sdcard+"/freemap.xml");
        	if(!styleFile.exists())
        	{
        		downloadStyleFile();
        	}
        	else
        	{
        		mapView.setRenderTheme(styleFile);
        	}
        	setContentView(mapView);
        	
        	
        	
        	personIcon = getResources().getDrawable(R.drawable.person);
        	annotationIcon = getResources().getDrawable(R.drawable.annotation);
        	markerIcon = getResources().getDrawable(R.drawable.marker);	
        	
        	overlay = new ArrayItemizedOverlay(getResources().getDrawable(R.drawable.person));
        	
        	handler = new DownloadHandler();
        	GeoPoint pos = new GeoPoint(51.05,-0.72);
        	SavedConfig lastConfig = (SavedConfig)getLastNonConfigurationInstance();
        	if(lastConfig!=null)
        	{
        		if(lastConfig.properties!=null)
        		{
        			Log.d("OpenTrail", "zoom="+lastConfig.properties.getInt("zoom"));
        			Log.d("OpenTrail", "lat="+lastConfig.properties.getDouble("lat"));
        			Log.d("OpenTrail", "lon="+lastConfig.properties.getDouble("lon"));
        			Log.d("OpenTrail", "mapFile="+lastConfig.properties.getString("mapFile"));
        			pos = new GeoPoint(lastConfig.properties.getDouble("lat"),
        								lastConfig.properties.getDouble("lon"));
        			mapView.getController().setZoom(lastConfig.properties.getInt("zoom"));
        			mapFile = lastConfig.properties.getString("mapFile");
        		}
        		
        		if(lastConfig.downloadThread != null)
        		{
        			t = lastConfig.downloadThread;
        			t.reconnect(handler);
        		}
        	}
        	
        	mapView.setMapFile(new File(mapFile));
        	mapView.setCenter(pos);
        
        	mapView.getOverlays().add(overlay);
        
        	
        	
        	Proj4ProjectionFactory factory=new Proj4ProjectionFactory();
        	String proj="epsg:27700";
    		Shared.proj = factory.generate(proj);
    		cachedir=makeCacheDir(proj);
    		FreemapFileFormatter formatter=new FreemapFileFormatter(Shared.proj.getID());
    		formatter.setScript("bsvr.php");
    		formatter.selectPOIs("place,amenity,natural");
    		formatter.selectAnnotations(true);
    		WebDataSource ds=new WebDataSource("http://www.free-map.org.uk/0.6/ws/",formatter);
    		poiDeliverer=new TileDeliverer("poi",ds, new XMLDataInterpreter
    				(new FreemapDataHandler()),5000,5000,Shared.proj,cachedir);
        	
        	Shared.pois = new FreemapDataset();
        	indexedAnnotations = new HashMap<Integer,OverlayItem> ();
        	
       
       
        }
        catch(IOException e)
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
     	
     	gpsListener=new GPSListener();
    	gpsListener.startUpdates();
     	
     	if(prefAnnotations==false && oldPrefAnnotations==true)
     		mapView.getOverlays().remove(overlay);
     	else if (prefAnnotations==true && oldPrefAnnotations==false)
     		mapView.getOverlays().add(overlay);
    }
    
    public void onStop()
    {
    	super.onStop();
    	gpsListener.stopUpdates();
    	gpsListener = null;
    }
    
    public void onDestroy()
    {
    	super.onDestroy();
    	indexedAnnotations.clear();
    	overlay.clear();
    	indexedAnnotations = null;
    	overlay = null;
    	mapView = null;
    	
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
    				overlay.addItem(item);
    				Annotation ann=new Annotation(Integer.parseInt(id),p.x,p.y,description);
    				Shared.pois.add(ann);
    				mapView.invalidate();
    				break;
    			case 2:
    				extras = i.getExtras();
    				POI poi = Shared.pois.getPOIById(Integer.parseInt(extras.getString("osmId")));
    				Log.d("OpenTrail","found POI="+poi);
    				if(poi!=null)
    					displayPOI(poi);
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
    		if(t==null)
    		{
    			t=new DownloadPOIsThread(this,handler);
    			t.createDialog();
    			t.start();
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
    
    public void displayPOI(POI poi)
    {
    	if(lastAddedPOI!=null)
    		overlay.removeItem(lastAddedPOI);
    	Log.d("OpenTrail","Found POI: " + poi.getPoint());
    	Point unproj = Shared.proj.unproject(poi.getPoint());
    	Log.d("OpenTrail","unprojected: " + unproj);
    	GeoPoint gp = new GeoPoint(unproj.y,unproj.x);
    	mapView.setCenter(gp);
    	String name=poi.getValue("name");
    	name=(name==null) ? "unnamed":name;
    	OverlayItem item = new OverlayItem(gp,name,name,ItemizedOverlay.boundCenterBottom(markerIcon));
    	overlay.
\

    	overlay.on
    	lastAddedPOI = item;
    }
    
    public void loadAnnotationOverlay()
    {
    	Shared.pois.operateOnAnnotations(this);
    }
    
    public void visit(Annotation ann)
    {
    	if(indexedAnnotations.get(ann.getId()) == null)
    	{
    		Point unproj = Shared.proj.unproject(ann.getPoint());
    		GeoPoint gp = new GeoPoint(unproj.y,unproj.x);
    		OverlayItem item = new OverlayItem(gp,"Annotation #"+ann.getId(),ann.getDescription(),
    				ItemizedOverlay.boundCenterBottom(annotationIcon));
    		overlay.addItem(item);
    		
    		indexedAnnotations.put(ann.getId(), item);
    	}
    }
    
    public Object onRetainNonConfigurationInstance()
    {
    	SavedConfig config = new SavedConfig();
    	
    	config.properties = new Bundle();
    	
  
    	GeoPoint gp = mapView.getMapPosition().getMapCenter();
 
    	
    	config.properties.putDouble("lat", gp.getLatitude());
    	config.properties.putDouble("lon", gp.getLongitude());
    	config.properties.putInt("zoom",mapView.getMapPosition().getZoomLevel());
    	config.properties.putString("mapFile",mapFile);
    	
    	Log.d("OpenTrail", "Saving properties");
    	if(t!=null)
    	{
    		t.disconnect();
    		config.downloadThread = t;
    	}
    	return config;
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
    
    private void downloadStyleFile()
    {
    	new AlertDialog.Builder(this).setMessage("No style file found. Download?").setPositiveButton("OK",
    			new DialogInterface.OnClickListener() { public void onClick(DialogInterface i,int which) { doDownloadStyleFile(); } } ).
    		setCancelable(true).setNegativeButton("Cancel",null).show();
    }
    
    private void doDownloadStyleFile()
    {
    	DownloadThread dsfThread = null;
    	Handler downloadStyleFileHandler = new Handler()
    	{
    		public void handleMessage(Message msg)
    		{
    			Bundle b = msg.getData();
    			new AlertDialog.Builder(OpenTrail.this).setMessage(b.getString("content")).setPositiveButton("OK",null).
					setCancelable(false).show();
    			if(b.getBoolean("success")==true)
    			{
    				try
    				{
    					mapView.setRenderTheme(new File(sdcard+"/freemap.xml"));
    					mapView.redrawTiles();
    				}
    				catch(FileNotFoundException e)
    				{
    					new AlertDialog.Builder(OpenTrail.this).setPositiveButton("OK",null).
    						setCancelable(false).setMessage("Unable to read style file from SD card").show();
    				}
    			}
    		}
    	};
    	
    	dsfThread = new DownloadThread(this,downloadStyleFileHandler)
    	{
    		public void run()
    		{
    			boolean success=true;
    			String content = "";
    			try
    			{
    				InputStream in = HTTPDownloader.getStream("http://www.free-map.org.uk/data/android/freemap.xml");
    				PrintWriter writer = new PrintWriter(new FileWriter(sdcard+"/freemap.xml"));
    				BufferedReader reader=new BufferedReader(new InputStreamReader(in));
    				String line;
    				while((line=reader.readLine()) != null)		
    				{
    					Log.d("OpenTrail", line);
    					System.out.println(line);
    					writer.println(line);
    				}
    				writer.close();
    				content="Successfully downloaded";
    			}
    			catch(IOException e)
    			{
    				success=false;
    				content=e.getMessage();
    				
    			}
    			finally
    			{
    				dlg.dismiss();
    				Message m = new Message();
    				Bundle details = new Bundle();
    				details.putBoolean("success",success);
    				details.putString("content",content);
    				m.setData(details);
    				handler.sendMessage(m);
    			}
    		}
    	};
    	dsfThread.setDialogDetails("Downloading style","Downloading style file...");
    	dsfThread.createDialog();
    	dsfThread.start();
    }
    
    class GPSListener implements LocationListener
    {
    	LocationManager mgr;
    	public GPSListener()
    	{
    		mgr = (LocationManager)getSystemService(LOCATION_SERVICE);
    	
    	}
    	public void startUpdates()
    	{
    		mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
    		if(myLocOverlayItem!=null)
    		{
    			overlay.addItem(myLocOverlayItem);
    		}
    	}
    	
    	public void stopUpdates()
    	{
    		mgr.removeUpdates(this);
    		if(myLocOverlayItem != null)
    		{
    			overlay.removeItem(myLocOverlayItem);
    		}
    	}
    	
    	public void onLocationChanged(Location loc)
    	{
    		GeoPoint p = new GeoPoint(loc.getLatitude(),loc.getLongitude());
    		
    		Shared.location = loc;
    		
    		if(myLocOverlayItem==null)
    		{
    			myLocOverlayItem = new OverlayItem(p,"My location","My location",ItemizedOverlay.boundCenterBottom(personIcon));//,personIcon);
    			overlay.addItem(myLocOverlayItem);
    		}
    		else
    		{
    			myLocOverlayItem.setPoint(p);
    		}
    		if(prefGPSTracking==true)
    			mapView.setCenter(p);
    		if(prefAutoDownload && poiDeliverer.needNewData(new Point(loc.getLongitude(),loc.getLatitude())))
    	    	startPOIDownload();
    		
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
    
    public class DownloadPOIsThread extends DownloadThread
    {   	
    	public DownloadPOIsThread(Context ctx,Handler h)
    	{
    		super(ctx,h);
    		setDialogDetails("Downloading POIs","Please wait, downloading POIs...");
    	}
    	
    	public void run()
    	{
    		String msg="Successfully downloaded";
    		boolean success=true;
    		Message m = new Message();
    		Point p = new Point(Shared.location.getLongitude(),Shared.location.getLatitude());
    		try
    		{
    			poiDeliverer.updateSurroundingTiles(p,true);
    			Log.d("OpenTrail","Data: " + poiDeliverer.getData());
    			Shared.pois = (FreemapDataset)poiDeliverer.getAllData();
    		}
    		catch(Exception e)
    		{
    			msg="Error downloading: " + e;
    			success=false;
    		}
    		finally
    		{
    			dlg.dismiss();
    			Bundle b = new Bundle();
    			b.putString("msg",msg);
    			b.putBoolean("success", success);
    			m.setData(b);
    			Log.d("OpenTrail","Sending message: " + msg);
    			this.handler.sendMessage(m);

    		}
    	}
    }
    
    public class DownloadHandler extends Handler
    {
    	public void handleMessage(Message msg)
    	{
    		new AlertDialog.Builder(OpenTrail.this).setMessage
    			(msg.getData().getString("msg")).setCancelable(false).
    			setPositiveButton("OK",null).show();
    		if(msg.getData().getBoolean("success")==true)
    		{
    			loadAnnotationOverlay();
    		}
    		t=null;
    	}
    }
    
    public class SavedConfig
    {
    	public Bundle properties;
    	public DownloadPOIsThread downloadThread;
    }
}