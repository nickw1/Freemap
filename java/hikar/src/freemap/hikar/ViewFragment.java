package freemap.hikar;

import freemap.data.Point;
import freemap.proj.Proj4ProjectionFactory;
import android.app.Fragment;
import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.util.Log;
import java.util.HashMap;
import freemap.datasource.Tile;
import freemap.data.Projection;



public class ViewFragment extends Fragment 
    implements LocationProcessor.Receiver,DownloadDataTask.Receiver,
    OpenGLView.RenderedWayVisitor,
    SensorInput.SensorInputReceiver, PinchListener.Handler {

    OsmDemIntegrator integrator;
    OpenGLView glView;
    DownloadDataTask downloadDataTask;
    LocationProcessor locationProcessor;
    SensorInput sensorInput;
    String tilingProjID;
    Point locDisplayProj;
    long lineOfSightTestFinish;
    boolean doingLineOfSightTest;
    int demType;
    String[] tilingProjIDs = { "epsg:27700", "epsg:4326" };
    TileDisplayProjectionTransformation trans;
    String lfpUrl, srtmUrl, osmUrl;
    
    
    public ViewFragment()
    {
        tilingProjID = "";
        setRetainInstance(true);
        sensorInput = new SensorInput(this);
        demType = OsmDemIntegrator.HGT_OSGB_LFP;
        trans = new TileDisplayProjectionTransformation ( null, null, 1.0 );
        lfpUrl = "http://www.free-map.org.uk/downloads/lfp/";
        srtmUrl = "http://www.free-map.org.uk/ws/";
        osmUrl = "http://www.free-map.org.uk/0.6/ws/";
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
            glView.getRenderer().setProjectionTransformation (trans);                             
        }
        else
        {
            integrator = null;
        }
    }
    
    public void receiveLocation(Location loc) {
    
        Log.d("hikar","received location");
        if(integrator!=null)
        {
            Log.d("hikar","OsmDemIntegrator exists so doing something with it");
            Point p = new Point(loc.getLongitude(), loc.getLatitude());
            locDisplayProj = trans.getDisplayProj().project(p);
          
            Log.d("hikar","location in display projection=" + locDisplayProj);
            glView.getRenderer().setCameraLocation((float)locDisplayProj.x, (float)locDisplayProj.y);
        
            double height = integrator.getHeight(new Point(loc.getLongitude(), loc.getLatitude()));
            glView.getRenderer().setHeight((float)height);
            ((Hikar)getActivity()).getHUD().setHeight((float)height);
            ((Hikar)getActivity()).getHUD().invalidate();
        
            if(integrator.needNewData(p))
            {
                downloadDataTask = new DownloadDataTask(this.getActivity(), this, integrator);
                downloadDataTask.setDialogDetails("Loading...", "Loading data...");
                downloadDataTask.execute(p);
            }
        }
    }
    
    public void noGPS() { }
    
    public void receiveData(DownloadDataTask.ReceivedData data)
    {
        Log.d("hikar", "Setting render data: " + System.currentTimeMillis());
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
            trans.setMultiplier(displayProjectionID.equals("epsg:3857") || displayProjectionID.equals("epsg:3785") ||
                                displayProjectionID.equals("epsg:900913") ? 0.1 : 1.0 );
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
}
