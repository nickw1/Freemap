package freemap.fixmypaths;

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
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
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
import freemap.andromaps.DownloadBinaryFilesTask;
import freemap.andromaps.DownloadTextFilesTask;

import freemap.datasource.FreemapDataset;
import freemap.data.Point;


import freemap.andromaps.DataCallbackTask;
import freemap.andromaps.ConfigChangeSafeTask;

import android.util.Log;

public class FixMyPaths extends MapActivity implements MapLocationProcessor.MapLocationReceiver,
			FindROWTask.ROWReceiver, DownloadFilesTask.Callback,
			DownloadProblemsTask.ProblemsReceiver {
	

	MapView view;
	MapLocationProcessor locationProcessor;
	DataDisplayer locationDisplayer;
	DownloadFilesTask dfTask;
	FindROWTask frTask;
	DownloadProblemsTask dpTask;
	String sdcard, styleFilename, mapFilename;
	File mapFile, styleFile;
	Location location;
	HashMap<String,String> currentROW;
	FreemapDataset problems;
	
	
	public static class SavedData
	{
		public DownloadFilesTask dfTask;
		public DataCallbackTask<?,?> dcTask;
	}
	
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        view = new MapView(this);
     	view.setClickable(true);
    	view.setBuiltInZoomControls(true);
    	
    	SavedData savedData = (SavedData)getLastNonConfigurationInstance();
    	if(savedData!=null)
    	{
    		if(savedData.dcTask!=null)
    			savedData.dcTask.reconnect(this, this);
    		else if(savedData.dfTask!=null)
    			savedData.dfTask.reconnect(this);
    	}
    	
    	
       
    		// fix for galaxy s3
    		//sdcard = new File("/mnt/extSdCard").exists() ? "/mnt/extSdCard" : Environment.getExternalStorageDirectory().getAbsolutePath();
    		sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
        	File openhantsDir = new File(sdcard+"/openhants");
        	if(!openhantsDir.exists())
        		openhantsDir.mkdir();
        	
        	styleFilename = sdcard + "/openhants/openhants.xml";
        	styleFile = new File(styleFilename);
        	mapFilename = sdcard + "/openhants/hampshire_combined.map";
        	mapFile = new File(mapFilename);
        
        	if(!mapFile.exists())
        	{	
        		Log.d("OpenHants","File does not exist:" + mapFilename);
        		dfTask = new DownloadBinaryFilesTask(this, new String[] { "http://www.free-map.org.uk/data/android/"
        														+"hampshire_combined.map" }, 
        										new String[] { mapFilename },
        										"Download map file? Warning: large file (9MB) - you might want to be "+
        										"in a wifi area to do this", this, 0);
        		dfTask.setDialogDetails("Downloading...","Downloading map file...");
        		dfTask.confirmAndExecute();
        	}
        	else
        	{
        		checkStyleFile();
        	}
    }
    
    public void onStart()
    {
    	super.onStart();
    	view.invalidate();
    	if(locationDisplayer!=null)
    		locationDisplayer.requestRedraw();
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
    
    public void downloadCancelled(int taskId)
    {
    	switch(taskId)
    	{
    		case 0:
    			
    			new AlertDialog.Builder(this).setPositiveButton("OK",
    					
    					new DialogInterface.OnClickListener()
    					{
    						public void onClick(DialogInterface i, int which)
    						{
    							System.exit(0);
    						}
    					}
    					).
    				setMessage("Cannot run without a map file, please install manually." +
    							"Please make an 'openhants' folder inside: " +
    							Environment.getExternalStorageDirectory().getAbsolutePath()+
    							" on the phone and copy the map file to it. See www.fixmypaths.org.").show();	
    	        break;
    	        
    			
    		case 1:
    			styleFile=null;
    			downloadFinished(taskId);
    			break;
    	}
    	
    }
    
    
    public void checkStyleFile()
    {
    	if(!styleFile.exists())
		{
			dfTask = new DownloadTextFilesTask(this, new String[] {"http://www.free-map.org.uk/data/android/openhants.xml"},
        						new String[] { styleFilename }, "Download style file?", this, 1);
	   		dfTask.setDialogDetails("Downloading...","Downloading style file...");
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
    			if(styleFile!=null)
    				view.setRenderTheme(styleFile);
        		setContentView(view);
        		if(mapFile!=null)
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
        	locationDisplayer = new DataDisplayer(this,view,getResources().getDrawable(R.drawable.person),
        							getResources().getDrawable(R.drawable.annotation));
        	locationProcessor = new MapLocationProcessor(this,this,locationDisplayer);
        	locationProcessor.startUpdates();
    }
    
    public Object onRetainNonConfigurationInstance()
    {
    	SavedData saved=null;
    	if(dfTask!=null && dfTask.getStatus()==AsyncTask.Status.RUNNING)
    	{
    		saved=new SavedData();
    		dfTask.disconnect();
    		saved.dfTask=dfTask;
    	}
    	else if (frTask!=null && frTask.getStatus()==AsyncTask.Status.RUNNING)
    	{
    		saved=new SavedData();
    		frTask.disconnect();
    		saved.dcTask=frTask;
    	}
    	else if (dpTask!=null && dpTask.getStatus()==AsyncTask.Status.RUNNING)
    	{
    		saved=new SavedData();
    		dpTask.disconnect();
    		saved.dcTask=dpTask;
    	}
    		
    	
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
    			SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    			if(prefs.getString("name", "").equals("") || prefs.getString("email", "").equals(""))
    			{
    				new AlertDialog.Builder(this).setMessage("Please specify your name and email in the preferences.").
    					setPositiveButton("OK",null).setCancelable(false).show();
    				
    			}
    			else
    			{
    				findNearbyROW();
    				retcode=true;
    			}
    			break;
    		
    			
    		case R.id.menuItemExistingProblems:
    			downloadExistingProblems();
    			retcode=true;
    			break;
    			
    			
    		case R.id.menuItemPreferences:
    			Intent intent = new Intent(this,FixMyPathsPreferenceActivity.class);
    			startActivity(intent);
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
    
    public void downloadExistingProblems()
    {
    	if(location==null)
    	{
    		new AlertDialog.Builder(this).setMessage("Location not known yet").
    			setPositiveButton("OK",null).setCancelable(false).show();
    	}
    	else if (dpTask!=null && dpTask.getStatus()==AsyncTask.Status.RUNNING)
    	{
    		new AlertDialog.Builder(this).setMessage("You're already downloading!").
				setPositiveButton("OK",null).setCancelable(false).show();
    	}
    	else
    	{
    		Point sw = new Point(location.getLongitude()-0.1, location.getLatitude()-0.1),
    			ne = new Point(location.getLongitude()+0.1,location.getLatitude()+0.1);
    		dpTask = new DownloadProblemsTask(this,this);
    		dpTask.execute(sw,ne);
    	}	
    }
    
    public void receiveProblems(FreemapDataset dataset)
    {
    	if(!dataset.getAnnotations().isEmpty())
    	{
    		this.problems = dataset;
    		loadProblemOverlay();
    	}
    }
    
    public void loadProblemOverlay()
    {
    	this.problems.operateOnAnnotations(locationDisplayer);
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
    	if(resultCode==RESULT_OK)
    	{
    		switch(requestCode)
    		{
    			case 0:
    				new AlertDialog.Builder(this).setMessage("Successfully reported problem.").
    					setPositiveButton("OK",null).setCancelable(false).show();
    				break;
    		}
    	}
    }
    
    public void receiveLocation(Location loc)
    {
    	
    	

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