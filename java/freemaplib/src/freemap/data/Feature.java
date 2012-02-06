package freemap.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Feature extends Projectable {
	protected HashMap<String,String> tags;
	protected int osmId;
	
	
	public Feature()
	{
		tags=new HashMap<String,String>();
	}
	
	public void addTag(String k,String v)
	{		
		tags.put(k,v);
	}
	
	public String getValue(String key)
	{
		return tags.get(key);
	}
	
	public int getId()
	{
		String id=tags.get("osm_id");
		return (id!=null) ? Integer.parseInt(id) : 0;
	}
	
	public String toString()
	{
		return "Tags: " + tags.toString();
	}
	
	protected String tagsAsXML()
	{
		String xml="";
		Set<Map.Entry<String,String> > t = tags.entrySet();
		for(Map.Entry<String, String> e: t)
		{
			xml+="<tag k=\"" + e.getKey()+"\" v=\"" + e.getValue() + "\" />\n";
		}
		return xml;
 	}
	
	public void reproject(Projection proj)
	{
		this.proj=proj;
	}
	
	public boolean containsKey(String k)
	{
		return tags.containsKey(k);
	}
}
