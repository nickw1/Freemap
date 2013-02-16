package freemap.hikar;

import freemap.data.Point;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import freemap.datasource.OSMRenderData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.location.LocationListener;
import java.util.Timer;
import java.util.TimerTask;

public class Hikar extends Activity {
	
	OpenGLView view;
	CameraView cview;
	CameraOverlay overlay;
	OsmDemIntegrator integrator;
	String projID;
	freemap.data.Projection proj;
	Timer timer;
	DataUpdater dataUpdater;
	DownloadErrorHandler handler;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
	      
		view = new OpenGLView(this);
	    cview=new CameraView(this);
	    overlay=new CameraOverlay(this);	
	    
	    setContentView(view);    
	    addContentView(cview,new LayoutParams(LayoutParams.FILL_PARENT,
	        						LayoutParams.FILL_PARENT));    
	    addContentView(overlay,new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT));
	  
	    integrator=new OsmDemIntegrator("epsg:27700");
	    
		SensorEventListener listener = new HikarSensorListener();
		
		cview.setOnTouchListener(new TouchListener());
	
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		projID = (prefs==null)?"epsg:27700":prefs.getString("prefProjection","epsg:27700");
		proj=new Proj4ProjectionFactory().generate(projID);
		
		
		LocationManager mgr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		LocationListener locationListener = new HikarLocationListener();
		mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		
		timer = new Timer();
		dataUpdater = new DataUpdater();
		timer.scheduleAtFixedRate(dataUpdater, 0L, 5000L);
		
		handler=new DownloadErrorHandler();
		
    }
    
    public void onDestroy()
	{
    	timer.cancel();
		super.onDestroy();
	}
	
	public void receive(OSMRenderData d)
	{
		if(d!=null && view!=null && overlay!=null)
		{
			Log.d("Hikar","Received render data");
			//System.out.println("Render data=" + d);
			overlay.setDataStatus("Loading data");
			view.setRenderData(d);
			overlay.setDataStatus("Received data");
			Log.d("Hikar","Height at x,y is: " + integrator.getHeight(new Point(489600,128500)));
		}
		else
		{
			Log.d("Hikar","one of d, view, overlay is null");
		}
	}
	
	
	public void setLocation(double lon,double lat)
	{
		if(overlay!=null && view!=null)
		{
			Point lonLat = new Point(lon,lat);
			dataUpdater.setLocation(lonLat);
			
			
			Point p = proj.project(lonLat);
			view.setCameraLocation((float)p.x,(float)p.y);
		
			//Log.d("Hikar", " Received location: "+ loc);
			overlay.setLocation(p.x,p.y);
		
			double height = integrator.getHeight(p);
			overlay.setHeight((float)height);
			view.setHeight((float)height);
		}
	}	
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.menu,menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		boolean retcode=false;
		
		switch(item.getItemId())
		{
			case R.id.itemPreferences:
				Intent intent = new Intent(this,PreferencesActivity.class);
				startActivity(intent);
				retcode=true;
				break;
			case R.id.itemCalibrate:	
				view.toggleCalibrate();
				item.setTitle(view.getCalibrate() ? "Calibration off":"Calibrate");
				retcode=true;
				break;
		}
		return retcode;
	}
	
	
	public class HikarLocationListener implements LocationListener
	{
		public void onLocationChanged(Location newLoc)
		{
			if(newLoc!=null)
				Hikar.this.setLocation(newLoc.getLongitude(),newLoc.getLatitude());
		}
		
		 public void onProviderDisabled(String provider) {
		 }
		  
		 public void onProviderEnabled(String provider) {
		 }
		    	
		 public void onStatusChanged(String provider,int status,Bundle extras) {
		 }		
	}
	
	public class DataUpdater extends TimerTask {

		Point currentLocation;
		
		public DataUpdater()
		{
			currentLocation = new Point(-181,-91);
		}
		
		public void run()
		{
			if(currentLocation!=null && currentLocation.x>=-180 && currentLocation.x<=180 &&
					currentLocation.y<=90 && currentLocation.y>=-90)
			{
				if(integrator.update(currentLocation))
					Hikar.this.receive(integrator.getCurrentOSMData());
			}
		}
		
		public void setLocation(Point p)
		{
			currentLocation=p;
		}
	}
	
	//http://www.devx.com/wireless/Article/42482/0/page/3

	public class HikarSensorListener implements SensorEventListener
	{
		
		float[] compassValues, accelValues;
		float[] lastCompassValues=null, lastAccelValues=null;
		float k=0.075f;
		
		
		public HikarSensorListener()
		{
			
		
		
			SensorManager sMgr = (SensorManager)
				Hikar.this.getSystemService(Context.SENSOR_SERVICE);
	
			// Note SensorManager.ORIENTATION gives strange values, Sensor.TYPE_ORIENTATION OK (deprecated!)
			// This is because there is not an actual orientation sensor - the physical sensors actually
			// sense magnetic field and tilt (accelerometer)
			sMgr.registerListener(this,
					sMgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
					SensorManager.SENSOR_DELAY_NORMAL);
			sMgr.registerListener(this,
					sMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_NORMAL);
		}
		
		public void onAccuracyChanged(Sensor s, int i)
		{
			
		}
		
		public void onSensorChanged(SensorEvent ev)
		{
			
			// Two examples (devx.com article and Photos Around)
			// recommend using an exponential smoothing method
			// on the data with a factor of 0.05 or 0.075.
		
			
			float radToDeg = 180.0f / (float)Math.PI;
			
			
			switch(ev.sensor.getType())
			{
				case Sensor.TYPE_MAGNETIC_FIELD:
					//System.out.println("MAGENTIC FIELD SENSED!");
					compassValues = ev.values.clone();
					if(lastCompassValues!=null)
					{
						for(int i=0; i<compassValues.length; i++)
							compassValues[i] = compassValues[i]*k + lastCompassValues[i]*(1-k);
					}
					lastCompassValues = compassValues;
					break;
				
				case Sensor.TYPE_ACCELEROMETER:
					//System.out.println("ACCELEROMETER SENSED!");
					accelValues = ev.values.clone();
					if(lastAccelValues!=null)
					{
						for(int i=0; i<accelValues.length; i++)
							accelValues[i] = accelValues[i]*k + lastAccelValues[i]*(1-k);
					}
					lastAccelValues = accelValues;
					break;
			}
			
			if(compassValues!=null && accelValues!=null)
			{
				float[] rMtx=new float[16], rMtxTrans=new float[16];
				SensorManager.getRotationMatrix(rMtx,null,accelValues,compassValues);
				
				// for landscape - which we assume the app runs in
				SensorManager.remapCoordinateSystem(rMtx,SensorManager.AXIS_Y,SensorManager.AXIS_MINUS_X,rMtxTrans);
							
				float[] orientation = new float[3];
				SensorManager.getOrientation(rMtxTrans,orientation);
				//rMtxTrans=rMtx;
			
			
				
				
				view.setMatrix(rMtxTrans);
				/*
				CameraOverlay overlay = (CameraOverlay)CameraActivity.this.findViewById
					(R.id.cameraoverlay);
				*/
				overlay.setDirection(orientation[0]*radToDeg,
									orientation[1]*radToDeg,
									orientation[2]*radToDeg);
				overlay.invalidate();
				
			}
			if(cview.isReadyFirstTime())
			{
				view.setHFOV(cview.getHFOV());
			}
		}
	}

	
	
	class TouchListener implements android.view.View.OnTouchListener
	{
		float[] downX = { -1,-1} , downY = {-1,-1}, upX={-1,-1}, upY={-1,-1};
		
		boolean dragging;
		
		// see http://www.zdnet.com/blog/burnette/how-to-use-multi-touch-in-android-2-part-3-understanding-touch-events/
		// 1775?tag=content;siu-container
		public boolean onTouch(View v, MotionEvent ev)
		{
			int actionCode=ev.getAction()&MotionEvent.ACTION_MASK;
			for(int i=0; i<ev.getPointerCount(); i++)
			{
		
				if(actionCode==MotionEvent.ACTION_DOWN)
				{
					//Log.d("Hikar","MotionEvent.ACTION_DOWN: i=" + i);
					
				}
				else if(actionCode==MotionEvent.ACTION_POINTER_DOWN)
				{
					downX[i] = ev.getX(i);
					downY[i] = ev.getY(i);
					if(i==1) dragging=true;
				}
				else if(actionCode==MotionEvent.ACTION_MOVE && dragging==true)
				{
					//Log.d("Hikar","Dragging");
					upX[i]=ev.getX(i);
					upY[i]=ev.getY(i);
				}
				else if(actionCode==MotionEvent.ACTION_UP && dragging==true)
				{
					dragging=false;
					//Log.d("Hikar","MotionEvent.ACTION_UP: i=" + i);
					float dxstart,dxend,dystart,dyend;
					dxstart=downX[1]-downX[0];
					dystart=downY[1]-downY[0];
					dxend=upX[1]-upX[0];
					dyend=upY[1]-upY[0];
					float distStart = FloatMath.sqrt(dxstart*dxstart+dystart*dystart),
						distEnd = FloatMath.sqrt(dxend*dxend+dyend*dyend);
					String status="";
					if(Math.abs(distEnd-distStart) > 50.0f)
					{
						if(distStart>distEnd)
						{
							// seems natural to pinch in to increase fov
							status = " pinch in";
							view.changeHFOV(5.0f);
							overlay.changeHFOV(5.0f);
						}
						else
						{
							status = " pinch out";
							view.changeHFOV(-5.0f);
							overlay.changeHFOV(-5.0f);
						}
					}
					Log.d("Hikar","distStart=" + distStart+ " distEnd=" + distEnd + " status=" + status);
					downX[0]=downY[0]=upX[0]=upY[0]=downX[1]=downY[1]=upX[1]=upY[1]=-1.0f;
				}
				else if(actionCode==MotionEvent.ACTION_POINTER_UP)
				{
					upX[i] = ev.getX(i);
					upY[i] = ev.getY(i);
				}
			}
			
			return true;
		}
	}
	
	
    

	
	class DownloadThread extends Thread
	{
		double w,s,e,n;
		ProgressDialog dlg;
		
		public DownloadThread(double w,double s, double e, double n)
		{
			this.w=w; this.s=s; this.e=e; this.n=n;
			dlg=ProgressDialog.show(Hikar.this,"Downloading","Downloading data...",true,false);
		}
		
		public void run()
		{
			
			try
			{
				Point bottomLeft = new Point(w,s),
					topRight = new Point(e,n);
				integrator.forceDownload(bottomLeft, topRight);
			}
			catch(Exception ex)
			{
				Bundle bundle=new Bundle();
				bundle.putString("error",ex.toString());
				Message msg=new Message();
				msg.setData(bundle);
				handler.sendMessage(msg);
			}
			finally
			{
				dlg.dismiss();
			}
		}
	}
	
	class DownloadErrorHandler extends Handler
	{
		public void handleMessage(Message msg)
		{
			Bundle data=msg.getData();
			new AlertDialog.Builder(Hikar.this).setMessage(data.getString("error")).setPositiveButton("OK",null).
				show();
		}	
	}
}