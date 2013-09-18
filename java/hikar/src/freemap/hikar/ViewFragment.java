package freemap.hikar;

import freemap.data.Point;
import freemap.datasource.FreemapDataset;
import freemap.proj.OSGBProjection;
import freemap.data.Projection;
import freemap.jdem.HGTTileDeliverer;
import freemap.jdem.DEM;
import android.app.Fragment;
import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.AsyncTask;


public class ViewFragment extends Fragment 
    implements LocationProcessor.Receiver,DownloadDataTask.Receiver,
    OpenGLView.RenderedWayVisitor,
    SensorInput.SensorInputReceiver, PinchListener.Handler {

    OsmDemIntegrator integrator;
    OpenGLView glView;
    DownloadDataTask downloadDataTask;
    LocationProcessor locationProcessor;
    SensorInput sensorInput;
    Projection proj;
    Point locOSGB;
    long lineOfSightTestFinish;
    boolean doingLineOfSightTest;
    
    
    public ViewFragment()
    {
        proj = new OSGBProjection();
        setRetainInstance(true);
        sensorInput = new SensorInput(this);
        integrator = new OsmDemIntegrator(proj.getID()); 
    
    }
    
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        glView = new OpenGLView(activity);  
        sensorInput.attach(activity);
        locationProcessor = new LocationProcessor(activity,this,5000,10);
        glView.setOnTouchListener(new PinchListener(this));
        FreemapDataset data = integrator.getCurrentOSMData();
        DEM dem = integrator.getCurrentDEM();
        setHFOV();
        if(data!=null)
            glView.getRenderer().setRenderData(new DownloadDataTask.ReceivedData(data, dem));
    }
    
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
 
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState)
    {
        return glView;
    }
    
    public void onResume() {
        super.onResume();
        locationProcessor.startUpdates();
        glView.getRenderer().onResume();
        sensorInput.start();
    }

    public void onPause() {
        super.onPause();
        locationProcessor.stopUpdates();
        sensorInput.stop();
        glView.getRenderer().onPause();
    }

    public void onDetach() {
        super.onDetach();
        sensorInput.detach();
    }
    
    public void receiveLocation(Location loc) {
        Point p = new Point(loc.getLongitude(), loc.getLatitude());
        locOSGB = proj.project(p);
        glView.getRenderer().setCameraLocation((float)locOSGB.x, (float)locOSGB.y);
        double height = integrator.getHeight(locOSGB);
        glView.getRenderer().setHeight((float)height);
        ((Hikar)getActivity()).getHUD().setHeight((float)height);
        ((Hikar)getActivity()).getHUD().invalidate();
        
        if(integrator.needNewData(p))
        {
            downloadDataTask = new DownloadDataTask(this.getActivity(), this, integrator);
            downloadDataTask.setDialogDetails("Loading...", "Loading data...");
            downloadDataTask.execute(p);
        }
        
        else if(integrator!=null && integrator.getDEM()!=null && !doingLineOfSightTest 
                && System.currentTimeMillis() - lineOfSightTestFinish > 10000)
        {
            /*
            AsyncTask<OpenGLView.RenderedWayVisitor,Void,Boolean> t = new AsyncTask<OpenGLView.RenderedWayVisitor,Void,Boolean>()
            {
                protected void onPreExecute()
                {
                    doingLineOfSightTest = true;
                }
                
                protected Boolean doInBackground(OpenGLView.RenderedWayVisitor... v)
                {        
                    glView.getRenderer().operateOnRenderedWays(v[0]);
                    return true;
                }
                
                protected void onPostExecute(Boolean result)
                {
                    lineOfSightTestFinish = System.currentTimeMillis();
                    doingLineOfSightTest = false;
                }
            };
            t.execute(this);
              */
        }
      
        
        
    }
    
    public void noGPS() { }
    
    public void receiveData(DownloadDataTask.ReceivedData data)
    {
        glView.getRenderer().setRenderData(data);
    }
    
    public void receiveSensorInput(float[] matrix, float[] orientation)
    {  
        glView.getRenderer().setOrientMtx(matrix);
        ((Hikar)getActivity()).getHUD().setOrientation(orientation);
        ((Hikar)getActivity()).getHUD().invalidate();
    }
    
    public void onPinchIn()
    {
        glView.getRenderer().changeHFOV(5.0f);
        setHFOV();
    }
     
    public void onPinchOut()
    {
        glView.getRenderer().changeHFOV(-5.0f);
        setHFOV();
    }
    
    private void setHFOV()
    {
        ((Hikar)getActivity()).getHUD().setHFOV(glView.getRenderer().getHFOV());
        ((Hikar)getActivity()).getHUD().invalidate();
    }
    
    public void toggleCalibrate()
    {
        glView.getRenderer().toggleCalibrate();
    }
    
    public void setCameraHeight(float cameraHeight)
    {
        android.util.Log.d("hikar","camera height=" + cameraHeight);
        glView.getRenderer().setCameraHeight(cameraHeight);
    }
    
    public void visit(RenderedWay rw)
    {
        
        HGTTileDeliverer dem = integrator.getDEM();
        if(locOSGB!=null && dem!=null && rw.isValid())
        {   
            int nVisibles = 0;
            float[] wayVertices = rw.getWayVertices();
            for(int i=0; i<wayVertices.length; i+=3)
            {
          
                boolean los = dem.lineOfSight(locOSGB, new Point(wayVertices[i],wayVertices[i+1],wayVertices[i+2]));
                if(los)
                {
                  nVisibles++; 
                  // rw.setVtxDisplayStatus(i, los);
                }
            }
            rw.setDisplayed(nVisibles > 1);
        }
       
    }
}
