package freemap.hikar;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.location.Location;
import android.os.AsyncTask;

import freemap.data.Point;
import freemap.datasource.FreemapDataset;


public class Hikar extends Activity implements LocationProcessor.Receiver,DownloadDataTask.Receiver
{
    OpenGLView glView;
    OsmDemIntegrator integrator;
    LocationProcessor locationProcessor;
    
    DownloadDataTask downloadDataTask;
    
    public class SavedData
    {
        DownloadDataTask dataTask;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glView = new OpenGLView(this);
        integrator = new OsmDemIntegrator("epsg:27700");
        setContentView(glView);
        
        locationProcessor = new LocationProcessor(this,this,5000,10);
        
        SavedData savedData;
        
        if((savedData=(SavedData)getLastNonConfigurationInstance())!=null)
        {
            if(savedData.dataTask!=null)
            {
                downloadDataTask = savedData.dataTask;
                downloadDataTask.reconnect(this, this);
            }
        }
    }

    public void onStart() {
        locationProcessor.startUpdates();
    }
    
    public void onStop() {
        locationProcessor.stopUpdates();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void receiveLocation(Location loc) {
        Point p = new Point(loc.getLongitude(), loc.getLatitude());
        if(integrator.needNewData(p))
        {
            downloadDataTask = new DownloadDataTask(this, this, integrator);
            downloadDataTask.setDialogDetails("Downloading...", "Downloading new data...");
            downloadDataTask.execute(p);
        }
    }

    public void noGPS() { }
    
    public void receiveData(FreemapDataset data)
    {
        glView.getRenderer().setRenderData(data);
    }
    
    public Object onRetainNonConfigurationInstance()
    {
        SavedData data = new SavedData();
        if(downloadDataTask!=null && downloadDataTask.getStatus()==AsyncTask.Status.RUNNING)
        {
            downloadDataTask.disconnect();
            data.dataTask = downloadDataTask;
        }
        return data;
    }
}
