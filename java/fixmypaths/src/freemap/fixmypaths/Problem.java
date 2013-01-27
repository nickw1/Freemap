package freemap.fixmypaths;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class Problem {
	HashMap<String,String> row;
	double lat,lon;
	
	public Problem(double lon, double lat)
	{
		this.lat=lat;
		this.lon=lon;
		
	}
	
	public void setROW(HashMap<String,String> row)
	{
		this.row=row;
		
	}
	
	public String getROWProperty(String key)
	{
		return row.get(key);
	}
	
	public double getLatitude()
	{
		return lat;
	}
	
	public double getLongitude()
	{
		return lon;
	}
	
	public Set<HashMap.Entry<String,String>> getROWEntrySet()
	{
		return row.entrySet();
	}
}
