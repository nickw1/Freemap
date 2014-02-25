package freemap.opentrail;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import freemap.andromaps.HTTPUploadTask;
import freemap.data.Walkroute;

// need to subclass HTTPUploadTask as loading gpx from file is also a lengthy process
// so we need to put it in the AsyncTask

public class WRUploadTask extends HTTPUploadTask {

    Walkroute walkroute;
    double dpDist;
   
    
    public WRUploadTask(Context ctx,  Walkroute walkroute, String url, 
            String alertMsg, Callback callback, int taskId, double dpDist)
    {
        super(ctx, url, null, alertMsg, callback, taskId);
        this.walkroute=walkroute;
        this.dpDist=dpDist;
    }
    
    public String doInBackground (Void... unused)
    {
        Walkroute simplified = walkroute.simplifyDouglasPeucker(dpDist);
        String gpx = simplified.toXML();
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("action","add"));
        postData.add(new BasicNameValuePair("route", gpx));
        postData.add(new BasicNameValuePair("format", "gpx"));
        setPostData(postData);
        return super.doInBackground(unused);
    }
}
