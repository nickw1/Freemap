package freemap.hikar;

import freemap.andromaps.DataCallbackTask;
import android.content.Context;
import freemap.data.Point;
import freemap.datasource.FreemapDataset;

public class DownloadDataTask extends DataCallbackTask<Point,Void> {

    OsmDemIntegrator integrator;
    
    public interface Receiver
    {
        public void receiveData(FreemapDataset data);
    }
    
    Receiver receiver;
    
    public DownloadDataTask(Context ctx, Receiver receiver, OsmDemIntegrator integrator)
    {
        super(ctx);
        this.receiver=receiver;
        this.integrator=integrator;
    }
    
    public String doInBackground(Point... p)
    {
        boolean status=false;
        String msg="";
        try
        {
           status = integrator.update(p[0]);
            if(status)
                setData(integrator.getCurrentOSMData());
        }
        catch(java.io.IOException e)
        {
            msg=e.getMessage();
            
        }
        catch(org.xml.sax.SAXException e)
        {
            msg= e.getMessage();
        }
        catch(Exception e)
        {
            android.util.Log.e("hikar", "Internal error: " + e.getStackTrace());
        }
        return (status) ? "Successfully downloaded" : "Error downloading: " + msg;
    }
   
    
    public void receive(Object data)
    {
        if(receiver!=null)
            receiver.receiveData((FreemapDataset)data);
    }
}
