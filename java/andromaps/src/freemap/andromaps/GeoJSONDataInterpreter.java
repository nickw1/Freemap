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
        try
        {
        String json = RawDataSource.doLoad(in);
        System.out.println("Loaded JSON=" + json);
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
                curFeature = new POI(coordinates.getDouble(0), coordinates.getDouble(1),
                            coordinates.length()==3 ? coordinates.getDouble(2): -1);
            }
            else if (type.equals("LineString"))
            {
                curFeature = new Way();
                buildWay((Way)curFeature, coordinates);
                
            }
            else if (type.equals("MultiLineString") || type.equals("Polygon"))
            {
                curFeature = new Way();
                for(int j=0; j<coordinates.length(); j++)
                {
                    buildWay((Way)curFeature, coordinates.getJSONArray(j));
                }
            }
            
            if(curFeature!=null)
            {
                properties = feature.getJSONObject("properties");
                Iterator keys = properties.keys();
                String key;
                while(keys.hasNext())
                {
                    key=(String)keys.next();
                    curFeature.addTag(key, properties.getString(key));
                }
                
                if(type.equals("Point"))
                    dataset.add((POI)curFeature);
                else
                    dataset.add((Way)curFeature);
            }
        }
        }
        catch (JSONException e)
        {
            Log.e("hikar",e.getMessage());
            e.printStackTrace();
        }
        return dataset;
    }
    
    private void buildWay(Way way, JSONArray coordinates) throws JSONException
    {
        for(int j=0; j<coordinates.length(); j++)
        {
            way.addPoint(coordinates.getJSONArray(j).getDouble(0),
                                   coordinates.getJSONArray(j).getDouble(1),
                                   coordinates.getJSONArray(j).length()==3 ?
                                           coordinates.getJSONArray(j).getDouble(2) : -1.0);
        }
    }
}

