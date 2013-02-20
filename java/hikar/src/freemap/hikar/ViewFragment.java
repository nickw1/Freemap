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
    implements LocationProcessor.Receiver,DownloadDataTask.Receiver{

    OsmDemIntegrator integrator;
    OpenGLView glView;
    DownloadDataTask downloadDataTask;
    LocationProcessor locationProcessor;
    Projection proj;
    
    public ViewFragment()
    {
        proj = new OSGBProjection();
    }
    
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        glView = new OpenGLView(activity);  
        locationProcessor = new LocationProcessor(activity,this,5000,10);
    }
    
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        integrator = new OsmDemIntegrator("epsg:27700");  
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState)
    {
        return glView;
    }
    
    public void onStart() {
        super.onStart();
        locationProcessor.startUpdates();
    }

    public void onStop() {
        super.onStop();
        locationProcessor.stopUpdates();
    }

    public void receiveLocation(Location loc) {
        Point p = new Point(loc.getLongitude(), loc.getLatitude());
        Point projected = proj.project(p);
        glView.getRenderer().setCameraLocation((float)projected.x, (float)projected.y);
        glView.getRenderer().setHeight(300.0f);
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
        android.util.Log.d("hikar","received data");
        glView.getRenderer().setRenderData(data);
    }
}
