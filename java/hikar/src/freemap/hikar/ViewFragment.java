package freemap.hikar;

import freemap.data.Point;
import freemap.datasource.FreemapDataset;
import freemap.proj.OSGBProjection;
import freemap.data.Projection;
import android.app.Fragment;
import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public class ViewFragment extends Fragment 
    implements LocationProcessor.Receiver,DownloadDataTask.Receiver,
    SensorInput.SensorInputReceiver, PinchListener.Handler {

    OsmDemIntegrator integrator;
    OpenGLView glView;
    DownloadDataTask downloadDataTask;
    LocationProcessor locationProcessor;
    SensorInput sensorInput;
    Projection proj;
    float hfov;
    
    public ViewFragment()
    {
        proj = new OSGBProjection();
        setRetainInstance(true);
        sensorInput = new SensorInput(this);
        integrator = new OsmDemIntegrator(proj.getID()); 
        hfov = 40.0f;
    }
    
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        glView = new OpenGLView(activity);  
        sensorInput.attach(activity);
        locationProcessor = new LocationProcessor(activity,this,5000,10);
        glView.setOnTouchListener(new PinchListener(this));
        FreemapDataset data = integrator.getCurrentOSMData();
        setHFOV();
        if(data!=null)
            glView.getRenderer().setRenderData(data);
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
        Point projected = proj.project(p);
        glView.getRenderer().setCameraLocation((float)projected.x, (float)projected.y);
        double height = integrator.getHeight(projected);
        glView.getRenderer().setHeight((float)height);
        ((Hikar)getActivity()).getHUD().setHeight((float)height);
        ((Hikar)getActivity()).getHUD().invalidate();
        if(integrator.needNewData(p))
        {
            downloadDataTask = new DownloadDataTask(this.getActivity(), this, integrator);
            downloadDataTask.setDialogDetails("Downloading...", "Downloading new data...");
            downloadDataTask.execute(p);
        }
    }
    
    public void noGPS() { }
    
    public void receiveData(FreemapDataset data)
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
        hfov += 5.0f;
        android.util.Log.d("hikar","onPinchIn(): hfov now: " + hfov);
        setHFOV();
    }
     
    public void onPinchOut()
    {
        hfov -= 5.0f;
        android.util.Log.d("hikar","onPinchOut(): hfov now: " + hfov);
        setHFOV();
    }
    
    private void setHFOV()
    {
        glView.getRenderer().setHFOV(hfov);
        ((Hikar)getActivity()).getHUD().setHFOV(hfov);
        ((Hikar)getActivity()).getHUD().invalidate();
    }
    
    public void toggleCalibrate()
    {
        glView.getRenderer().toggleCalibrate();
    }
}
