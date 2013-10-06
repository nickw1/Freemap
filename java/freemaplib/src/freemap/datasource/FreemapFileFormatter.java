package freemap.datasource;

import freemap.data.Point;
import java.util.HashMap;

// Updates:
// 26/11/11 can now select desired ways or POIs
// 03/12/11 can now select annotations

public class FreemapFileFormatter extends FileFormatter{

	String projID, selectedWays, selectedPOIs, format;
	boolean doAnnotations;
	String script;
	HashMap<String,String> keyvals;
	int tileWidth, tileHeight;
	
	public FreemapFileFormatter(String projID)
	{
	    this(projID, "xml", 5000, 5000);
	}
	
	public FreemapFileFormatter(String projID, String format)
	{
	    this(projID, format, 5000, 5000);
	}
	
	public FreemapFileFormatter(String projID, String format, int tileWidth, int tileHeight)
	{
		this.projID=projID;
		this.format=format;
		script="rsvr.php";
		keyvals = new HashMap<String, String>();
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
	}
	
	public void setScript(String script)
	{
		this.script=script;
	}
	
	public void selectWays(String waytypes)
	{
		selectedWays=waytypes;
	}
	
	public void selectPOIs(String poitypes)
	{
		selectedPOIs=poitypes;
	}
	
	public void selectAnnotations(boolean a)
	{
		doAnnotations=a;
	}
	
	public void unselectWays()
	{
		selectedWays=null;
	}
	
	public void unselectPOIs()
	{
		selectedPOIs=null;
	}
	
	public void addKeyval(String k, String v)
	{
	    keyvals.put(k, v);
	}
	
	public String format(Point bottomLeft)
	{
		Point tileBottomLeft = new Point();
		tileBottomLeft.x = ( ((int)bottomLeft.x)/tileWidth ) * tileWidth;
		tileBottomLeft.y = ( ((int)bottomLeft.y)/tileHeight ) * tileHeight;
		String url = script+"?bbox="+tileBottomLeft.x+","+tileBottomLeft.y+","+(tileBottomLeft.x+tileWidth)+","
		        +(tileBottomLeft.y+tileHeight)+
			(selectedWays==null?"":"&way="+selectedWays)+
			(selectedPOIs==null?"":"&poi="+selectedPOIs)+
			(doAnnotations==true?"&annotation=1":"")+
			 "&format="+format+"&inProj="+projID.replace("epsg:","")+"&outProj=epsg:4326";
		for(java.util.Map.Entry<String,String> entry: keyvals.entrySet())
		    url += "&" + entry.getKey() + "=" + entry.getValue();
		return url;
	}
}
