package freemap.opentrail;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;

import org.osmdroid.views.MapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.views.overlay.SimpleLocationOverlay;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.content.SharedPreferences;
import android.app.AlertDialog;
import android.os.Handler;
import android.os.Message;
import java.util.HashMap;
import freemap.datasource.FreemapDataHandler;
import freemap.datasource.FreemapFileFormatter;
import freemap.datasource.TileDeliverer;
import freemap.datasource.WebDataSource;
import freemap.datasource.XMLDataInterpreter;
import freemap.data.Point;
import android.content.Context;
import android.util.Log;
import freemap.datasource.FreemapDataset;
import freemap.data.POI;
import java.util.ArrayList;
import freemap.data.Annotation;
import java.io.File;

import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

public class OpenTrail extends Activity implements OpenTrailLocationListener.Observer,
				FreemapDataset.AnnotationVisitor {

	MapView map;
	HashMap<String,OnlineTileSourceBase> tileSources;
	
	String mapSrcId;
	
	SimpleLocationOverlay whereAmI;
	LocationManager mgr;
	
	boolean gpsTracking, prefAnnotations, prefAutoDownload;
	
	
	public OpenTrailLocationListener listener;
	
	TileDeliverer poiDeliverer;
	
	
	public static int count=0;
	
	
	Handler handler;
	
	DownloadPOIsThread t;
	boolean firstGPSLoc;
	
	ItemizedIconOverlay<OverlayItem> poiOverlay, annotationsOverlay;
	ItemizedIconOverlay.OnItemGestureListener<OverlayItem> markerGestureListener;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	super.onCreate(savedInstanceState);
    	 setContentView(R.layout.main);
    	
    
       
        map = (MapView)findViewById(R.id.map);
        tileSources = new HashMap<String,OnlineTileSourceBase>();
        tileSources.put("FREEMAP", new XYTileSource
        	("Freemap", null, 0, 16, 256, ".png", "http://tilesrv.sucs.org/ofm/"));
        tileSources.put("FREEMAP_HOME",new XYTileSource
            	("Freemap (Home rendered)", null, 0, 16, 256, ".png", 
            			"http://www.free-map.org.uk/images/tiles/"));
        tileSources.put("NPE", new XYTileSource
    		("NPE", null, 0, 16, 256, ".png", "http://a.ooc.openstreetmap.org/npe/",
    				"http://b.ooc.openstreetmap.org/npe/","http://c.ooc.openstreetmap.org/npe/"));
        tileSources.put("OS7",new XYTileSource
    		("OS 7th Series", null, 0, 16, 256, ".jpg", "http://a.ooc.openstreetmap.org/os7/",
    				"http://b.ooc.openstreetmap.org/os7/","http://c.ooc.openstreetmap.org/os7"));
        tileSources.put("OS1",new XYTileSource
			("OS First Edition", null, 0, 16, 256, ".jpg", "http://a.ooc.openstreetmap.org/os1/",
					"http://b.ooc.openstreetmap.org/os1/","http://c.ooc.openstreetmap.org/os1/"));
       
		handler = new DownloadHandler();
		
        GeoPoint defaultLocation=null;
        SavedConfig savedConfig = (SavedConfig)getLastNonConfigurationInstance();
        if (savedConfig!=null)
        {
        	if(savedConfig.properties!=null)
        	{
        		map.getController().setZoom(savedConfig.properties.getInt("zoom"));
        		defaultLocation=new GeoPoint(savedConfig.properties.getInt("lat"),
        									savedConfig.properties.getInt("lon"));
        	}
        	
        	if(savedConfig.downloadThread!=null)
        	{
        		t=savedConfig.downloadThread;
        		t.reconnect(handler);
        	}
        }
        else
        {
        
        	defaultLocation = new GeoPoint(51.05, -0.72);
        	map.getController().setZoom(13);
        	        
        }
        map.getController().setCenter(defaultLocation);

        
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        
        
        listener=new OpenTrailLocationListener();
      
        listener.startUpdates(this);

        
        Shared.location=null;
       
        gpsTracking=false;
        prefAnnotations=true;
        prefAutoDownload=false;
      
        
        whereAmI = new SimpleLocationOverlay(this);
        whereAmI.setLocation(defaultLocation);
        map.getOverlays().add(whereAmI);    
        
       
        Toast.makeText(this,"Map data copyright OpenStreetMap Contributors, licenced under CC-by-SA", Toast.LENGTH_LONG).show();		
    
        
        String projID="epsg:27700";
       
        String cachedir = makeCacheDir(projID);
        
        Proj4ProjectionFactory factory=new Proj4ProjectionFactory();
		Shared.proj = factory.generate(projID);
		FreemapFileFormatter formatter=new FreemapFileFormatter(Shared.proj.getID());
		formatter.selectPOIs("place,amenity,natural");
		formatter.selectAnnotations(true);
		WebDataSource ds=new WebDataSource("http://www.free-map.org.uk/ws/",formatter);
		poiDeliverer=new TileDeliverer("poi",ds, new XMLDataInterpreter
				(new FreemapDataHandler()),5000,5000,Shared.proj,cachedir);

		firstGPSLoc = true;
		
		markerGestureListener = new  ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() 
		{
			public boolean onItemSingleTapUp(int i,OverlayItem item)
			{
				Toast.makeText(OpenTrail.this,item.mDescription,Toast.LENGTH_SHORT).show();
				return true;
			}
			
			public boolean onItemLongPress(int i,OverlayItem item)
			{
				Toast.makeText(OpenTrail.this,item.mDescription,Toast.LENGTH_LONG).show();
				return true;
			}
		};
		
		annotationsOverlay=new ItemizedIconOverlay<OverlayItem>
			(this,new ArrayList<OverlayItem>(),markerGestureListener);
		map.getOverlays().add(annotationsOverlay);

		// will be case if we do an onCreate due to orientation change
		if(Shared.pois != null)
			loadAnnotationOverlay();	
    }
    
    
    public void onStart()
    {
    	super.onStart();
    	
    	listener.setObserver(this);
    	  
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	if(prefs!=null)
    	{
    		boolean gpsTrackingNew = prefs.getBoolean("prefGPSTracking", true);
    		mapSrcId = prefs.getString("prefMapSource","FREEMAP");
    		if(gpsTrackingNew!=gpsTracking)
    		{
    			if(gpsTrackingNew==true)
    			{
    				listener.startUpdates(this);
    			}
    			else
    			{
    				listener.stopUpdates(this);
    			}
    			gpsTracking=gpsTrackingNew;
    		}
    		
    		boolean prefAnnotationsNew = prefs.getBoolean("prefAnnotations",true);
    		if (prefAnnotationsNew!=prefAnnotations)
    		{
    			annotationsOverlay.setEnabled(prefAnnotationsNew);
    			prefAnnotations=prefAnnotationsNew;
    		}    
    		prefAutoDownload=prefs.getBoolean("prefAutoDownload",false);
    	}
    	else
    	{
    		gpsTracking=true;
    		mapSrcId="FREEMAP";
    	}
    	updateDisplayedLayer();
    	
    }
    
    public void onDestroy()
    {
    	super.onDestroy();
    	listener.stopUpdates(this);
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
    
    private void updateDisplayedLayer()
    {
    	map.getTileProvider().setTileSource(tileSources.get(mapSrcId));
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater=getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    		
    	boolean retcode=false;
    	Intent intent = null;
    	switch(item.getItemId())
    	{
    		case R.id.myLocationMenuItem:
    			gotoMyLocation();
    			break;
    			
    		case R.id.inputAnnotationMenuItem:
    			if(Shared.location!=null)
    			{
    				intent = new Intent(this,InputAnnotationActivity.class);
    				startActivityForResult(intent,0);
    			}
    			else
    			{
    				new AlertDialog.Builder(this).setMessage("Location unknown").
    					setPositiveButton("OK",null).setCancelable(false).show();
    			}
    			retcode= true;
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
    			startActivityForResult(intent,1);
    			break;
    	}
    	
    	return retcode;
    }
    
    protected void onActivityResult(int requestCode,int resultCode,Intent intent)
    {
    	Bundle extras=null;
    	if(resultCode==RESULT_OK)
    	{
    		switch(requestCode)
    		{
    			case 0:
    				extras = intent.getExtras();
    				String id=extras.getString("ID"),description=extras.getString("description");
    				GeoPoint gp = new GeoPoint(extras.getDouble("lat"),extras.getDouble("lon"));
    				Point p = Shared.proj.project(new Point(extras.getDouble("lon"),extras.getDouble("lat")));
    				OverlayItem item = new OverlayItem("Annotation #"+id,description,gp);
    				item.setMarker(getResources().getDrawable(R.drawable.annotation));
    				annotationsOverlay.addItem(item);
    				Annotation ann=new Annotation(Integer.parseInt(id),p.x,p.y,description);
    				Shared.pois.add(ann);
    				map.invalidate();
    				break;
    			case 1:
    				extras = intent.getExtras();
    				POI poi = Shared.pois.getPOIById(Integer.parseInt(extras.getString("osmId")));
    				Log.d("OpenTrail","found POI="+poi);
    				if(poi!=null)
    					displayPOI(poi);
    				break;
    		}
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
    
    public Object onRetainNonConfigurationInstance()
    {
    	SavedConfig config=null;
    	if(t!=null)
    	{
    		t.disconnect();
    		config=new SavedConfig();
    		config.downloadThread=t;
    	}
    	
    	if(Shared.location!=null)
    	{
    		Bundle b = new Bundle();
    		b.putDouble("lon", Shared.location.getLongitude());
    		b.putDouble("lat", Shared.location.getLatitude());
    		b.putInt("zoom",map.getZoomLevel());
    		if(config==null)
    			config=new SavedConfig();
    		config.properties=b;
    	}
    	return config;
    }
    
    public void receive(Location loc)
    {
 
    	Shared.location=loc;
    	GeoPoint curLocAsGP = new GeoPoint(loc.getLatitude(),loc.getLongitude());
    	if(firstGPSLoc)
    	{
    		gotoMyLocation();
    		firstGPSLoc=false;
    	}
    	//map.getController().setCenter(curLocAsGP);
    	whereAmI.setLocation(curLocAsGP);
    	map.invalidate();
    	if(prefAutoDownload && poiDeliverer.needNewData(new Point(loc.getLongitude(),loc.getLatitude())))
    		startPOIDownload();
    }
    
    public void gotoMyLocation()
    {
    	if(Shared.location!=null)
    	{
    		GeoPoint curLocAsGP = new GeoPoint(Shared.location.getLatitude(),Shared.location.getLongitude());
    		map.getController().setCenter(curLocAsGP);
    	}
    	else
    	{
    		new AlertDialog.Builder(this).setMessage("Location not known yet").
    			setCancelable(false).setPositiveButton("OK",null).show();
    	}
    }
    
    public void displayPOI(POI poi)
    {
    	if(poiOverlay!=null)
    	{
    		poiOverlay.removeAllItems();
    	}
    	else
    	{
    		poiOverlay=new ItemizedIconOverlay<OverlayItem>(this,new ArrayList<OverlayItem>(),
    						markerGestureListener);    		
    	}
    	Point poiLL = Shared.proj.unproject(poi.getPoint());
    	GeoPoint gpLL = new GeoPoint(poiLL.y,poiLL.x);
    	String desc=poi.getValue("description");
    	if(desc==null||desc.equals(""))
    		desc=poi.getValue("name");
    	poiOverlay.addItem(new OverlayItem(poi.getValue("name"),desc,gpLL));
    	map.getOverlays().add(poiOverlay);
    	map.getController().setCenter(gpLL);
    }
    
    public void loadAnnotationOverlay()
    {
    	Shared.pois.operateOnAnnotations(this);
    }
    
    public void visit(Annotation ann)
    {
    	Point lonLat = Shared.proj.unproject(ann.getPoint());
    	GeoPoint gp = new GeoPoint(lonLat.y,lonLat.x);
    	OverlayItem i = new OverlayItem("Annotation #"+ann.getId(),ann.getDescription(),gp);
    	i.setMarker(getResources().getDrawable(R.drawable.annotation));
    	annotationsOverlay.addItem(i);
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