package freemap.andromaps;


import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.InputStream;
import freemap.datasource.DataInterpreter;
import freemap.datasource.FreemapDataset;
import freemap.datasource.RawDataSource;
import freemap.data.Way;
import freemap.data.POI;
import freemap.data.Feature;
import java.util.Iterator;
import android.util.Log;
import java.util.ArrayList;

import java.io.FileInputStream;

public class GeoJSONDataInterpreter implements DataInterpreter {

    
    public Object getData (InputStream in) throws Exception
    {
        FreemapDataset dataset = new FreemapDataset();
        
        String json = RawDataSource.doLoad(in);
        

        
        JSONObject jsonObj = new JSONObject(json);
        JSONArray coordinates;
        JSONObject properties, feature, geometry;
        Feature curFeature = null;
        ArrayList<Way> curWays = null;

       
        
        JSONArray features = jsonObj.getJSONArray("features");
        for(int i=0; i<features.length(); i++)
        {
            feature = features.getJSONObject(i);
            geometry  = feature.getJSONObject("geometry");
            coordinates = geometry.getJSONArray("coordinates");
            String type = geometry.getString("type");
            if(type.equals("Point"))
            {   
                curFeature = makePOI(coordinates);
                properties = feature.getJSONObject("properties");      
                addProperties (curFeature, properties); 
                dataset.add((POI)curFeature);
            }
            else if (type.equals("LineString") || type.equals("MultiLineString") ||
                        type.equals("Polygon") || type.equals("MultiPolygon"))
            {
            	//Log.d("jsontest", "feature type=" + type);
                curWays = makeWay(coordinates, type, feature.getJSONObject("properties")); 
                //Log.d("jsontest", "NUMBER OF CURWAYS: " + curWays.size());
                for(int j=0; j<curWays.size(); j++)
            		dataset.add(curWays.get(j));
            }  	
        }
        
        return dataset;
    }
    
    
    // IMPORTANT cannot deal with MultiLineStrings or MultiPolygons yet
    // This is used from the SpatialiteTileDeliverer
    public static Feature jsonToFeature(String json) throws JSONException
    {
    	Feature f = null;
        JSONObject jsonObj = new JSONObject(json);
        String geomType = jsonObj.getString("type");
        
        if(geomType.equals("LineString") || geomType.equals("Polygon"))
        {
           JSONArray arr = jsonObj.getJSONObject("geometry").getJSONArray("coordinates"); // 130315 added geometry I presume
           ArrayList<Way> ways = makeWay(arr, geomType, jsonObj.getJSONObject("properties"));
           f = ways.size() >= 1 ? ways.get(0) : null;
        }  
        else if (geomType.equals("Point"))
        {
            JSONArray arr = jsonObj.getJSONObject("geometry").getJSONArray("coordinates");
            if(arr.length() >= 2)
            {
                f = makePOI(arr);
            }
        }
        return f;
    } 
    
    private static Way wayFromLinestring(JSONArray arr) throws JSONException
    {
    	return wayFromLinestring(arr,null);
    }
    
    private static Way wayFromLinestring( JSONArray arr, Way w) throws JSONException
    {
    	Way way = (w==null) ? new Way() : w;
    	
        for(int i=0; i<arr.length(); i++)
        {
            JSONArray curCoord = arr.getJSONArray(i);
            double x = curCoord.getDouble(0), y = curCoord.getDouble(1);
            way.addPoint(x, y);
        }
        return way;
    }
    
    private static POI makePOI(JSONArray coordinates) throws JSONException
    {
        return new POI(coordinates.getDouble(0), coordinates.getDouble(1),
            coordinates.length()==3 ? coordinates.getDouble(2): -1);
    }
    
    private static ArrayList<Way> makeWay(JSONArray coords, String geomType, JSONObject properties) throws JSONException
    {
        //Log.d("jsontest", "makeWay: geomType=" + geomType);
        ArrayList<Way> ways = new ArrayList<Way>();
        
        if(geomType.equals("LineString"))
        {
        	//Log.d("jsontest", "calling wayFromLinestring");
        	Way w = wayFromLinestring(coords);
        	//Log.d("jsontest", "done. adding properties");
        	addProperties (w, properties);
        	//Log.d("jsontest", "done.");
        	ways.add(w);
        }
        // We cannot deal with polygons with holes just yet, so just take the first LinearRing
        else if (geomType.equals("Polygon"))
        {
        	Way polygon =  wayFromLinestring(coords.getJSONArray(0));
            addProperties(polygon, properties);
            ways.add(polygon);
        }
        else if(geomType.equals("MultiLineString") || geomType.equals("MultiPolygon"))
		{
			for(int i=0; i<coords.length(); i++)
			{
				ways.addAll(makeWay ((JSONArray)coords.get(i), geomType.substring(5), properties));
			}
		}
        //Log.d("jsontest", "makeWay finished");
        return ways;
    }
    
    
    
    private static void addProperties (Feature f, JSONObject properties) throws JSONException
    {
        
        Iterator keys = properties.keys();
        String key;
        while(keys.hasNext())
        {
            key=(String)keys.next();
            f.addTag(key, properties.getString(key));
        }
    }
}

