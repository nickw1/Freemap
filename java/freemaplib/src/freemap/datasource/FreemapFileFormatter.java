package freemap.datasource;

import freemap.data.Point;
import freemap.data.GoogleProjection;

// Updates:
// 26/11/11 can now select desired ways or POIs
// 03/12/11 can now select annotations

public class FreemapFileFormatter extends FileFormatter{

	String projID, selectedWays, selectedPOIs;
	boolean doAnnotations;
	String script;
	
	
	public FreemapFileFormatter(String projID)
	{
		this.projID=projID;
		script="rsvr.php";
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
	
	public String format(Point bottomLeft)
	{
		Point tileBottomLeft = new Point();
		tileBottomLeft.x = ( ((int)bottomLeft.x)/5000 ) * 5000;
		tileBottomLeft.y = ( ((int)bottomLeft.y)/5000 ) * 5000;
		return script+"?bbox="+tileBottomLeft.x+","+tileBottomLeft.y+","+(tileBottomLeft.x+5000)+","+(tileBottomLeft.y+5000)+
			(selectedWays==null?"":"&way="+selectedWays)+
			(selectedPOIs==null?"":"&poi="+selectedPOIs)+
			(doAnnotations==true?"&annotation=1":"")+
			 "&inProj="+projID.replace("epsg:","")+"&outProj=epsg:4326";
		
	}
}
