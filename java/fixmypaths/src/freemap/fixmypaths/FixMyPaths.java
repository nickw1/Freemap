package freemap.fixmypaths;

import org.apache.http.NameValuePair;
import org.mapsforge.android.maps.MapActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
// Credits for icons
// res/drawable/person.png is taken from osmdroid, cropped
// res/drawable/annotation.png is modified from the standard OSM viewpoint icon.

import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;

import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;


import org.mapsforge.core.GeoPoint;
import org.mapsforge.android.maps.MapView;

import freemap.andromaps.DialogUtils;
import freemap.andromaps.MapLocationProcessor;
import freemap.andromaps.MapLocationProcessorWithListener;
import freemap.andromaps.LocationDisplayer;
import freemap.andromaps.DownloadFilesTask;
import freemap.andromaps.DownloadBinaryFilesTask;
import freemap.andromaps.DownloadTextFilesTask;


import freemap.datasource.FreemapDataset;
import freemap.data.Point;
import freemap.data.Annotation;
import freemap.datasource.AnnotationCacheManager;

import freemap.andromaps.DataCallbackTask;
import freemap.andromaps.ConfigChangeSafeTask;
import freemap.andromaps.HTTPUploadTask;
import freemap.andromaps.HTTPCommunicationTask;
import freemap.andromaps.HTTPCommunicationTask.Callback;

import android.util.Log;

import java.io.IOException;
import org.apache.http.message.BasicNameValuePair;

public class FixMyPaths extends MapActivity implements MapLocationProcessor.LocationReceiver,
			FindROWTask.ROWReceiver, DownloadFilesTask.Callback,
			DownloadProblemsTask.ProblemsReceiver, MapView.OnTouchListener {
	

	MapView view;
	MapLocationProcessorWithListener locationProcessor;
	DataDisplayer locationDisplayer;
	HTTPCommunicationTask dfTask;
	FindROWTask frTask;
	DownloadProblemsTask dpTask;
	String sdcard, styleFilename, mapFilename;
	File mapFile, styleFile;
	GeoPoint location;
	Problem currentProblem;
	double problemLat, problemLon;
	FreemapDataset problems;
	AnnotationCacheManager annCacheMgr;
	int annId;
	int xDown = -256, yDown = -256;
	GeoPoint initPos;
	
	public static class SavedData
	{
		public HTTPCommunicationTask dfTask;
		public DataCallbackTask<?,?> dcTask;
	}
	
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        view = new MapView(this);
     	view.setClickable(true);
    	view.setBuiltInZoomControls(true);
    	view.setOnTouchListener(this);
    	
    	SharedPreferences prefs = null;
    	SavedData savedData = (SavedData)getLastNonConfigurationInstance();
    	if(savedData!=null)
    	{
    		if(savedData.dcTask!=null)
    			savedData.dcTask.reconnect(this, this);
    		else if(savedData.dfTask!=null)
    			savedData.dfTask.reconnect(this);
    	}
    	
    	if ((prefs=getPreferences(Context.MODE_PRIVATE))!=null)
    	{
    		initPos = new GeoPoint (prefs.getFloat("latitude", 51.0f),
    									prefs.getFloat("longitude", -1.0f));
    		view.getController().setZoom(prefs.getInt("zoom", 14));
    	}
    	else
    	{
    		initPos = new GeoPoint(51.0f, -1.0f);
    		view.getController().setZoom(14);
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
        	annCacheMgr = new AnnotationCacheManager(sdcard+"/openhants/problemCache/");
        	try
        	{
        		this.problems = annCacheMgr.getAnnotationsAsDataset();
        	}
        	catch(Exception e)
        	{
        		DialogUtils.showDialog(this, "Unable to read saved annotations: " + e.getMessage());
        	}
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
    
    public void onDestroy()
    {
    	super.onDestroy();
    	SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = prefs.edit();
    	if(location!=null)
    	{
    		editor.putFloat("lat",(float)location.getLatitude());
    		editor.putFloat("lon",(float)location.getLongitude());
    	}
    	editor.putInt("zoom", view.getMapPosition().getZoomLevel());
    	editor.commit();
    }
    
    public void downloadFinished(int taskId, Object addData)
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
    			
    		case 2:
    			annCacheMgr.deleteCache();
    			locationDisplayer.clear();
    			locationDisplayer.requestRedraw();
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
    			downloadFinished(taskId,null);
    			break;
    	}
    	
    }
    
    public void downloadError(int taskId)
    {
    	new AlertDialog.Builder(this).setPositiveButton("OK",null).
    		setMessage("The download could not be completed").show();
    }
    
    public void noGPS()
    {
    	// todo
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
        		view.setCenter(initPos);
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
        	locationProcessor = new MapLocationProcessorWithListener(this,this,locationDisplayer);
        	locationProcessor.startUpdates(5000, 5);
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
    			else if(location==null)
    			{
    				new AlertDialog.Builder(this).setMessage("Location not known yet.").
						setPositiveButton("OK",null).setCancelable(false).show();
    			}
    			else
    			{
    				findNearbyROW(location.getLongitude(),location.getLatitude());
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
    			retcode=true;
    			break;
    			
    		case R.id.menuItemUploadSavedProblems:
    			uploadProblems();
    			retcode=true;
    			break;
    			
    		case R.id.menuItemAbout:
    			about();
    			retcode=true;
    			break;
    	}
    	return retcode;
    }
    
    public void findNearbyROW(double lon, double lat)
    {
    		Log.d("OpenHants","findNearbyRow()");
    		if(frTask==null || frTask.getStatus()!=AsyncTask.Status.RUNNING)
    		{
    			Log.d("OpenHants","creating task");
    			frTask = new FindROWTask(this, this);
    			currentProblem = new Problem(lon,lat);
    			frTask.execute(new GeoPoint(lat,lon));	
    		}
    		else
    		{
    			new AlertDialog.Builder(this).setMessage("Already downloading").
    				setPositiveButton("OK", null).setCancelable(false).show();
    		}
    	
    }
    
    private void launchReportProblemActivity()
    {
    	Intent i = new Intent(this,ReportProblemActivity.class);
    	Bundle extras = new Bundle();
    	for(HashMap.Entry<String,String> keyval: currentProblem.getROWEntrySet())
    		extras.putString(keyval.getKey(), keyval.getValue());
    	extras.putDouble("lat", currentProblem.getLatitude());
    	extras.putDouble("lon", currentProblem.getLongitude());
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
    				Bundle extras = intent.getExtras();
    				if(extras.getBoolean("freemap.openhants.success")==true)
    				{
    					if(extras.getBoolean("freemap.openhants.needtocache")==true)
    					{
    						try
    						{
    							Annotation annotation = new Annotation(-(annCacheMgr.size()+1),
    										currentProblem.getLongitude(),currentProblem.getLatitude(),
    										extras.getString("freemap.openhants.description"),
    										extras.getString("freemap.openhants.type"));
    							annotation.putExtra("row_id", 
    									currentProblem.getROWProperty("gid"));
    							annCacheMgr.addAnnotation(annotation);
    							this.problems.add(annotation);	
    							loadProblemOverlay();
    					    	locationDisplayer.requestRedraw();
    						}
    						catch(java.io.IOException e) 
    						{ 
    							new AlertDialog.Builder(this).setMessage("Error saving: " + e.getMessage()).
        							setPositiveButton("OK",null).setCancelable(false).show();
    						}
    					}
    					new AlertDialog.Builder(this).setMessage("Successfully reported problem.").
    						setPositiveButton("OK",null).setCancelable(false).show();
    					
    					
    					break;
    					
    				}
    				else
    				{
    					new AlertDialog.Builder(this).setMessage("Error reporting problem.").
							setPositiveButton("OK",null).setCancelable(false).show();
    				}
    		}
    	}
    }
    
    public void receiveLocation(double lon, double lat, boolean refresh)
    {
    	location = new GeoPoint(lat,lon);
    	view.setCenter(location);
    }
    
    public void receiveROW(HashMap<String,String> row)
    {
    	if(row!=null)
    	{
    		if(currentProblem!=null) //should never actually be null
    		{
    			currentProblem.setROW(row);
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
    	}
    	else
    	{
    		new AlertDialog.Builder(this).setMessage("No right of way found").
    			setCancelable(false).setPositiveButton("OK", null).show();
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
    				new AlertDialog.Builder(this).setMessage("Add a problem at this location?").
    					setNegativeButton("Cancel",null).
    					setPositiveButton("OK", new DialogInterface.OnClickListener()
    					{
    						public void onClick(DialogInterface i, int which)
    						{
    							GeoPoint p = view.getProjection().fromPixels(x,y);
    							findNearbyROW(p.getLongitude(),p.getLatitude());
    						}
    					}).show();
    				
    				retcode=true;
    			}
    			xDown = yDown = -256;
    			break;
    	}
    	return retcode;
    }
    
    
    public void uploadProblems()
    {
    	try
    	{
    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    		String name = prefs.getString("name",""), email=prefs.getString("email","");
    		if(name.equals("") || email.equals(""))
    		{
    			DialogUtils.showDialog(this,"Please specify name and email in the preferences.");
    		}
    		else if(annCacheMgr.isEmpty())
    		{
    			DialogUtils.showDialog(this,"No saved problems to upload.");
    		}
    		else
    		{
    			String problemsXML = annCacheMgr.getAllAnnotationsXML();
    			ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
    			postData.add(new BasicNameValuePair("action","addMultiProblems"));
    			postData.add(new BasicNameValuePair("reporter_name",name));
    			postData.add(new BasicNameValuePair("reporter_email",email));
    			postData.add(new BasicNameValuePair("data",problemsXML));
    			Log.d("OpenHants","Data to be uploaded: " + problemsXML);
    			if(dfTask==null || dfTask.getStatus()!=AsyncTask.Status.RUNNING)
    			{
    				dfTask = new HTTPUploadTask(this,"http://www.fixmypaths.org/row.php",postData,
    													"Upload all saved problems?", this, 2);
    				dfTask.confirmAndExecute();
    			}
    		}
    	}
    	catch(IOException e)
    	{
    		new AlertDialog.Builder(this).setPositiveButton("OK", null).
    			setMessage("Error uploading: " + e.getMessage()).show();
    	}
    }
    
    public void about()
    {
    	DialogUtils.showDialog(this,"FixMyPaths!. Uses OpenStreetMap data, copyright 2012 " +
    											"OpenStreetMap contributors, CC-by-SA."+
    											"Ordnance Survey OpenData LandForm Panorama contours, "+
    											"Crown Copyright and database right. Uses Hampshire " +
    											"County Council rights of way data, copyright "+
    											"Hampshire County Council, Open Government Licence");
    }
}