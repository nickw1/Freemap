
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

import java.io.PrintStream;


import com.jhlabs.map.proj.ProjectionFactory;
import com.jhlabs.map.Point2D;
import com.jhlabs.map.proj.Projection;

public class ReprojectHandler extends DefaultHandler {

	PrintStream out;
	boolean unproj;
	Projection proj;
	double multiplier;
	
	public ReprojectHandler(PrintStream out, String projId, boolean unproj, double multiplier)
	{
		this.out=out;
		this.unproj=unproj;
		if(projId!=null)
			proj=ProjectionFactory.getNamedPROJ4CoordinateSystem(projId);
		this.multiplier=multiplier;
	}
	
	public void startElement(String uri,String name,String qName,Attributes attrs)
	{
		String tag = (name.equals("")) ? qName: name;
		out.print("<"+tag);
		double lat = Double.MAX_VALUE, lon = Double.MAX_VALUE;
		
		for(int i=0; i<attrs.getLength(); i++)
		{
			if(attrs.getQName(i).equals("lat"))
			{
				lat = Double.parseDouble(attrs.getValue(i));
				
			}
			else if (attrs.getQName(i).equals("lon"))
			{
				lon = Double.parseDouble(attrs.getValue(i));	
			}
			else
			{
				String encoded=attrs.getValue(i).replace("&","&amp;").replace("\"", "&quot;").
						replace("<","&lt;").replace(">","&gt;");
				out.print(" "+attrs.getQName(i)+"=\""+encoded+"\"");
			}
			
		}
		if(lat!=Double.MAX_VALUE && lon!=Double.MAX_VALUE)
		{
			// allows conversion to a range acceptable to osmosis - i.e. 10^4 metres rather than metres
			double inverseMultiplier = (unproj) ? 1.0 / multiplier : 1.0,
					forwardMultiplier = (unproj) ? 1.0 : multiplier;
			Point2D.Double outPt = doReproject(lon*inverseMultiplier,lat*inverseMultiplier);
			
			out.print(" lat=\"" + outPt.y*forwardMultiplier + "\" lon=\"" + outPt.x*forwardMultiplier + "\"");
		}
		out.print(">");
	}
	
	public void endElement(String uri, String name, String qName)
	{	
		String tag = (name.equals("")) ? qName: name;
		out.print("</" + tag + ">");
	}
	
	public void characters(char[] ch, int start, int length)
	{
		String encoded=new String(ch,start,length).replace("&","&amp;").replace("\"", "&quot;").
				replace("<","&lt;").replace(">","&gt;");
		out.print(encoded);
	}
	
	private Point2D.Double doReproject(double x, double y)
	{
		if(proj!=null)
		{
			Point2D.Double point = new Point2D.Double(x,y), out=new Point2D.Double();
			return unproj? proj.inverseTransform(point, out): proj.transform(point, out);	
		}
		return new Point2D.Double(x,y);
	}
	
	
}
