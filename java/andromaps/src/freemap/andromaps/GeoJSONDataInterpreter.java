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

public class GeoJSONDataInterpreter implements DataInterpreter {

    
    public Object getData (InputStream in) throws Exception
    {
        FreemapDataset dataset = new FreemapDataset();
        
        String json = RawDataSource.doLoad(in);
        //System.out.println("Loaded JSON=" + json);
        Log.d("hikar", "Loaded JSON, time=" + System.currentTimeMillis());
        JSONObject jsonObj = new JSONObject(json);
        JSONArray coordinates;
        JSONObject properties, feature, geometry;
        Feature curFeature = null;

       
        
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
            }
            else if (type.equals("LineString") || type.equals("MultiLineString") ||
                        type.equals("Polygon"))
            {
                curFeature = makeWay(coordinates, type);    
            }
            
            
            if(curFeature!=null)
            {
                properties = feature.getJSONObject("properties");      
                addProperties (curFeature, properties);
                
                if(type.equals("Point"))
                    dataset.add((POI)curFeature);
                else
                    dataset.add((Way)curFeature);
            }
        }
        
        return dataset;
    }
    
    
    
    public static Feature jsonToFeature(String json) throws JSONException
    {
        JSONObject jsonObj = new JSONObject(json);
        String geomType = jsonObj.getString("type");
        Feature f = null;
        
        if(geomType.equals("LineString") || geomType.equals("MultiLineString") ||
                geomType.equals("Polygon"))
        {
           JSONArray arr = jsonObj.getJSONArray("coordinates");
           f = makeWay(arr, geomType);
        }  
        else if (geomType.equals("Point"))
        {
            JSONArray arr = jsonObj.getJSONArray("coordinates");
            if(arr.length() >= 2)
            {
                f = makePOI(arr);
            }
        }
        return f;
    } 
    
    private static void wayFromLinestring(Way way, JSONArray arr) throws JSONException
    {
        for(int i=0; i<arr.length(); i++)
        {
            JSONArray curCoord = arr.getJSONArray(i);
            double x = curCoord.getDouble(0), y = curCoord.getDouble(1);
            way.addPoint(x, y);
        }
    }
    
    private static POI makePOI(JSONArray coordinates) throws JSONException
    {
        return new POI(coordinates.getDouble(0), coordinates.getDouble(1),
            coordinates.length()==3 ? coordinates.getDouble(2): -1);
    }
    
    private static Way makeWay(JSONArray coords, String geomType) throws JSONException
    {
        Way f = new Way();
        
        if(geomType.equals("LineString"))
            wayFromLinestring((Way)f, coords);
     
 
        else if (geomType.equals("MultiLineString") || geomType.equals("Polygon"))
        {
            
            
            for(int i=0; i<coords.length(); i++)
            {
                wayFromLinestring((Way)f, coords.getJSONArray(i));
            }
        }
        return f;
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

