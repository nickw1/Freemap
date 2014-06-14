package freemap.hikar;

import freemap.andromaps.DialogUtils;
import freemap.data.Point;
import freemap.proj.Proj4ProjectionFactory;
import android.app.Fragment;
import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.opengl.Matrix;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.util.Log;
import android.hardware.GeomagneticField;
import java.util.HashMap;
import freemap.datasource.Tile;
import freemap.data.Projection;



public class ViewFragment extends Fragment 
    implements LocationProcessor.Receiver,DownloadDataTask.Receiver,
    OpenGLView.RenderedWayVisitor,
    SensorInput.SensorInputReceiver, PinchListener.Handler 
{

    OsmDemIntegrator integrator;
    OpenGLView glView;
    DownloadDataTask downloadDataTask;
    LocationProcessor locationProcessor;
    SensorInput sensorInput;
    String tilingProjID;
    Point locDisplayProj;
    long lineOfSightTestFinish;
    boolean doingLineOfSightTest, activated;
    int demType;
    String[] tilingProjIDs = { "epsg:27700", "epsg:4326" };
    TileDisplayProjectionTransformation trans;
    String lfpUrl, srtmUrl, osmUrl;
    GeomagneticField field;
    float orientationAdjustment;
    double lastLon, lastLat;
   

    
    
    public ViewFragment()
    {
        tilingProjID = "";
        setRetainInstance(true);
        sensorInput = new SensorInput(this);
        demType = OsmDemIntegrator.HGT_OSGB_LFP;
        trans = new TileDisplayProjectionTransformation ( null, null );
        lfpUrl = "http://www.free-map.org.uk/downloads/lfp/";
        srtmUrl = "http://www.free-map.org.uk/ws/";
        osmUrl = "http://www.free-map.org.uk/0.6/ws/";
        lastLon = -181;
        lastLat = -91;
    }
    
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        
        // We don't do any reprojection on RenderedWays if the display projection and tiling projection
        // are the same
        glView = new OpenGLView(activity);
        sensorInput.attach(activity);
        locationProcessor = new LocationProcessor(activity,this,5000,10);
        glView.setOnTouchListener(new PinchListener(this));
        
        if(integrator!=null)
        {
            HashMap<String, Tile> data = integrator.getCurrentOSMTiles();
            HashMap<String, Tile> dem = integrator.getCurrentDEMTiles();
            setHFOV();
            if(data!=null && dem!=null)
                glView.getRenderer().setRenderData(new DownloadDataTask.ReceivedData(data, dem));
        }
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
    
    public void setActivate (boolean activate)
    {
        if(activate)
        {
            Proj4ProjectionFactory fac = new Proj4ProjectionFactory();
            trans.setTilingProj(fac.generate(tilingProjID));   
            integrator = new OsmDemIntegrator(trans.getTilingProj(), demType, lfpUrl, srtmUrl, osmUrl);   
            
            if(false)
                setLocation (-1.3814, 50.9261, true);
            
            // If we received a location but weren't activated, now load data from the last location
            else if(lastLon >= -180 && lastLon <= 180 && lastLat >= -90 && lastLat <= 90)
                setLocation(lastLon, lastLat, true);
        }
        else
        {
            integrator = null;
            glView.getRenderer().deactivate();
        }
        activated = activate;
    }
    
    public void receiveLocation(Location loc)
    {
        setLocation(loc.getLongitude(),loc.getLatitude(), true);
    }
    
    public void setLocation(double lon, double lat)
    {
        setLocation (lon, lat, false);
    }
    
    private void setLocation(double lon, double lat, boolean gpsLocation)
    {
        if(gpsLocation)
        {
            lastLon = lon;
            lastLat = lat;
        }
        
        if(integrator!=null)
        {
           
            Point p = new Point(lon, lat);
            double height = integrator.getHeight(p);
            p.z = height;
            
            // We assume we won't travel far enough in one session for magnetic north to change much
            if(field==null)
            {
                field = new GeomagneticField ((float)lat, (float)lon, 
                                            0, System.currentTimeMillis());
            }
            
            locDisplayProj = trans.getDisplayProj().project(p);
          
            Log.d("hikar","location in display projection=" + locDisplayProj);
            glView.getRenderer().setCameraLocation(p);
        
         
       
            ((Hikar)getActivity()).getHUD().setHeight((float)height);
            ((Hikar)getActivity()).getHUD().invalidate();
        
            if(integrator.needNewData(p) && downloadDataTask==null)
            {
                downloadDataTask = new DownloadDataTask(this.getActivity(), this, integrator, gpsLocation);
                downloadDataTask.setDialogDetails("Loading...", "Loading data...");
                downloadDataTask.setShowDialogOnFinish(true);
                downloadDataTask.execute(p);
            }
        }
    }
    
    public void noGPS() { }
    
    public void receiveData(DownloadDataTask.ReceivedData data, boolean sourceGPS)
    {
        Log.d("hikar", "received data");
        if (data!=null && sourceGPS) // only show data if it's a gps location, not a manual entry
        {
            glView.getRenderer().setRenderData(data);   
        }
        else if (data==null)
            DialogUtils.showDialog(this.getActivity(), "Warning - received data is null!");
        else if (!sourceGPS)
            DialogUtils.showDialog(this.getActivity(), "Notice - sourceGPS is false");
        
        downloadDataTask = null;
    }
    
    public void receiveSensorInput(float[] glR)
    { 
        float[] orientation = new float[3];
        
        float magNorth = field==null ? 0.0f : field.getDeclination(),
                actualAdjustment = magNorth + orientationAdjustment;
        Matrix.rotateM (glR, 0, actualAdjustment, 0.0f, 0.0f, 1.0f);
        
        SensorManager.getOrientation(glR, orientation);
         
        glView.getRenderer().setOrientMtx(glR);
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
        // old line of sight stuff - removed
    }
    
    public boolean setDEM (int demType)
    {
        
        this.demType = demType;
        if(!(tilingProjID.equals(tilingProjIDs[demType])))
        {
            tilingProjID = tilingProjIDs[demType];
            return true;
        }
        
        return false;
    }
    
    public boolean setDisplayProjectionID (String displayProjectionID)
    {
        Proj4ProjectionFactory fac = new Proj4ProjectionFactory();
        Projection proj = fac.generate(displayProjectionID);
        if(proj!=null)
        {
           
            trans.setDisplayProj(proj); 
            glView.getRenderer().setProjectionTransformation (trans);  
            return true;
        }
        return false;
    }
    
    public boolean setDataUrls (String lfpUrl, String srtmUrl, String osmUrl)
    {
        boolean change=!(this.lfpUrl.equals(lfpUrl)) || !(this.srtmUrl.equals(srtmUrl)) || !(this.osmUrl.equals(osmUrl));
        this.lfpUrl = lfpUrl;
        this.srtmUrl = srtmUrl;
        this.osmUrl = osmUrl;
        return change;
    }
    
    public void changeOrientationAdjustment(float amount)
    {
        orientationAdjustment += amount;
    }
    
    public float getOrientationAdjustment()
    {
        return orientationAdjustment;
    }
}
