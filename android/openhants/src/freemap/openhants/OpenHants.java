package freemap.openhants;

import org.mapsforge.android.maps.MapActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.app.AlertDialog;
import android.location.Location;


import java.io.File;

import java.io.FileNotFoundException;

import java.util.HashMap;


import org.mapsforge.core.GeoPoint;
import org.mapsforge.android.maps.MapView;

import freemap.andromaps.MapLocationProcessor;
import freemap.andromaps.LocationDisplayer;
import freemap.andromaps.DownloadFilesTask;



public class OpenHants extends MapActivity implements MapLocationProcessor.MapLocationReceiver,
			FindROWTask.ROWReceiver, DownloadFilesTask.Callback {
	

	MapView view;
	MapLocationProcessor locationProcessor;
	LocationDisplayer locationDisplayer;
	DownloadFilesTask dfTask;
	FindROWTask frTask;
	String sdcard, styleFilename, mapFilename;
	File mapFile, styleFile;
	Location location;
	HashMap<String,String> currentROW;
	
	public class SavedData
	{
		public DownloadFilesTask dfTask;
		public FindROWTask frTask;
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        view = new MapView(this);
     	view.setClickable(true);
    	view.setBuiltInZoomControls(true);
    	
    	SavedData data = (SavedData)getLastNonConfigurationInstance();
    	if(data!=null)
    	{
    		if(data.dfTask!=null)
    			data.dfTask.reconnect(this, this);
    		else if (data.frTask!=null)
    			data.frTask.reconnect(this,this);
    	}
    	
       
        
        
        	sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
        	styleFilename = sdcard + "/openhants/openhants.xml";
        	styleFile = new File(styleFilename);
        	mapFilename = sdcard + "/openhants/openhants.map";
        	mapFile = new File(mapFilename);
        	if(!mapFile.exists())
        	{	
        		dfTask = new DownloadFilesTask(this, new String[] { "http://www.free-map.org.uk/data/android/"
        														+"hampshire_highway_natural_row_contour.map" }, 
        										new String[] { mapFilename },
        										"Download map file? Warning: large file (9MB) - you might want to be "+
        										"in a wifi area to do this", this, 0);
        		dfTask.confirmAndExecute();
        	}
        	else
        	{
        		checkStyleFile();
        	}
        
       
    }
    
    public void downloadFinished(int taskId)
    {
    	switch(taskId)
    	{
    		case 0:
    			checkStyleFile();
    	        break;
    	        
    			
    		case 1:
    			setupMap();
    			setupLocation();
    			break;
    	}
    }
    
    public void checkStyleFile()
    {
    	if(!styleFile.exists())
		{
			dfTask = new DownloadFilesTask(this, new String[] {"http://www.free-map.org.uk/data/android/openhants.xml"},
        						new String[] { styleFilename }, "Download style file?", this, 1);
			dfTask.confirmAndExecute();
		}
		else
		{
			setupMap();
			setupLocation();
		}
    }
    
    public void setupMap()
       	
    {
    	try
    	{
        		view.setRenderTheme(styleFile);
        		setContentView(view);
        		view.setMapFile(mapFile);
        		view.setCenter(new GeoPoint(51,-1));
        		view.redrawTiles();
    	}
    	catch(FileNotFoundException e)
    	{
    		new AlertDialog.Builder(this).setPositiveButton("OK",null).
    			setMessage("Style and/or map file not found: " + e.getMessage()).setCancelable(false).show();
    	}
    }
        
    
    public void setupLocation()
    {
        	locationDisplayer = new LocationDisplayer(this,view,getResources().getDrawable(R.drawable.person));
        	locationProcessor = new MapLocationProcessor(this,this,locationDisplayer);
        	locationProcessor.startUpdates();
    }
    
    public Object onRetainNonConfigurationInstance()
    {
    	Object saved=null;
    	if(dfTask!=null && dfTask.getStatus()==AsyncTask.Status.RUNNING)
    		saved=dfTask;
    	else if (frTask!=null && frTask.getStatus()==AsyncTask.Status.RUNNING)
    		saved=frTask;
    	return saved;
    }
    
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	
    	boolean retcode=false;
    	
    	switch(item.getItemId())
    	{
    		case R.id.menuItemReportProblem:
    			findNearbyROW();
    			retcode=true;
    			break;
    	}
    	return retcode;
    }
    
    public void findNearbyROW()
    {
    	if(location!=null)
    	{
    		if(frTask==null || frTask.getStatus()!=AsyncTask.Status.RUNNING)
    		{
    			frTask = new FindROWTask(this, this);
    			frTask.execute(location);
    		}
    		else
    		{
    			new AlertDialog.Builder(this).setMessage("Already downloading").
    				setPositiveButton("OK", null).setCancelable(false).show();
    		}
    	}
    	else
    	{
    		new AlertDialog.Builder(this).setMessage("Location not known yet").
    			setPositiveButton("OK", null).setCancelable(false).show();
    	}
    }
    
    private void launchReportProblemActivity()
    {
    	Intent i = new Intent(this,ReportProblemActivity.class);
    	Bundle extras = new Bundle();
    	for(HashMap.Entry<String,String> keyval: currentROW.entrySet())
    		extras.putString(keyval.getKey(), keyval.getValue());
    	extras.putDouble("lat", location.getLatitude());
    	extras.putDouble("lon", location.getLongitude());
    	i.putExtras(extras);
    	startActivityForResult(i, 0);
    
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
    	if(resultCode==RESULT_OK)
    	{
    		switch(requestCode)
    		{
    			case 0:
    				break;
    		}
    	}
    }
    
    public void receiveLocation(Location loc)
    {
    	loc.setLatitude(51);
    	loc.setLongitude(-1);
    	location = loc;
    	view.setCenter(new GeoPoint(loc.getLatitude(),loc.getLongitude()));
    }
    
    public void receiveROW(HashMap<String,String> row)
    {
    	if(row!=null)
    	{
    		currentROW = row;
    		new AlertDialog.Builder(this).setMessage("Found ROW: type=" +
    					row.get("row_type")+" ID=" + row.get("parish_row")).
    					setCancelable(false).setPositiveButton("OK",
    							new DialogInterface.OnClickListener() 
    							{
									
									@Override
									public void onClick(DialogInterface i, int which) 
									{
										// TODO Auto-generated method stub
										launchReportProblemActivity();
								
									}
								}).show();
    	}
    	else
    	{
    		new AlertDialog.Builder(this).setMessage("No right of way found").
    			setCancelable(false).setPositiveButton("OK", null).show();
    	}
    }
}