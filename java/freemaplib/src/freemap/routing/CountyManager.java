package freemap.routing;


/**
 * Created by nick on 07/07/15.
 */

import java.util.HashMap;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.File;
import freemap.data.Point;

// Manages county polygons
public class CountyManager {

    String dir;

    public static String[] countyNames = new String[]
            { "buckinghamshire", "cambridgeshire", "cheshire", "cornwall", "cumbria",
              "derbyshire", "devon", "dorset", "east-sussex", "east-yorkshire-with-hull",
              "essex", "gloucestershire", "greater-london", "greater-manchester", "hampshire",
              "herefordshire", "hertfordshire", "isle-of-wight", "kent", "lancashire",
              "leicestershire", "norfolk", "north-yorkshire", "nottinghamshire", "oxfordshire",
              "shropshire", "somerset", "south-yorkshire", "staffordshire", "suffolk",
              "surrey", "west-midlands", "west-sussex", "west-yorkshire", "wiltshire" };
    
    County[] counties;

    public CountyManager(String dir)
    {
        counties = new County[countyNames.length];
        File d = new File(dir);
        if(!d.exists())
            d.mkdir();

        this.dir=dir;
    }

    public void downloadOrLoad(String server) throws IOException
    {
        HttpURLConnection conn;
        for(int i=0; i<countyNames.length; i++)
        {
        	String fname = dir+"/"+countyNames[i]+".poly";
        	
        	if(!new File(fname).exists())
        	{
        		URL url = new URL(server + "/" + countyNames[i] + ".poly");
        		conn = (HttpURLConnection)url.openConnection();
        		InputStream in = conn.getInputStream();
        		if(conn.getResponseCode()==200)
        		{
        			String line;
        			PrintWriter pw = new PrintWriter (new FileWriter(fname));
        			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        			while ((line = reader.readLine()) != null)
        				pw.println(line);
        			reader.close();
        			pw.close();
        		}
        		in.close();
        	}
        	counties[i] = new County(countyNames[i]);
        	counties[i].read(fname);
        }
    }

    public County findCounty(Point lonLat) 
    {
        for(int i=0; i<counties.length; i++)
            if(counties[i].pointWithin(lonLat)) 
                return counties[i];          
           
        return null;
    }
    
    public static void main (String[] args) throws IOException
    {
    	CountyManager mgr = new CountyManager("/home/nick/cache/counties");
    	mgr.downloadOrLoad("http://download.geofabrik.de/europe/great-britain/england/");
    	County co = mgr.findCounty(new Point(-1.4, 50.9));
    	if(co!=null)System.out.println(co.getName());
    }
}

