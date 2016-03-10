package freemap.datasource;


import org.xml.sax.Attributes;
import freemap.data.TrackPoint;
import java.util.ArrayList;
import freemap.data.WalkrouteSummary;

public class WalkroutesHandler extends XMLDataHandler{
	ArrayList<WalkrouteSummary> routes = new ArrayList<WalkrouteSummary>();
	String curTag, routeName,routeDescription, routeId, strDist;
	boolean  inName, inDesc,inWpt, inId, inExtensions, inDistance;
	TrackPoint curPoint = new TrackPoint();
	double curDistance;
	
	public void startElement(String uri,String localName, String qName,Attributes attrs)
	{
		curTag = (localName.equals("")) ? qName:localName;
		System.out.println("opening tag: " +curTag);
		if(curTag.equals("wpt"))
		{
			inWpt=true;
			routeName = "";
			routeDescription = "";
			
			if(attrs.getValue("lat")!=null && attrs.getValue("lon")!=null)
			{
				curPoint.x = Double.parseDouble(attrs.getValue("lon"));
				curPoint.y = Double.parseDouble(attrs.getValue("lat"));
			}
		}
		
		else if (curTag.equals("name"))
		{
			inName = true;
			routeName = "";
		}
		else if (curTag.equals("desc"))
		{
			inDesc = true;
			routeDescription = "";
		}
		else if (curTag.equals("cmt"))
		{
			inId = true;
			routeId = "";
		}
		else if (curTag.equals("extensions"))
		{
			inExtensions = true;
		}
		else if (curTag.equals("distance") && inExtensions)
		{
			inDistance = true;
			strDist = "";
		}
	}
	
	public void endElement(String uri,String localName,String qName)
	{
		String closingTag = (localName.equals("")) ? qName: localName;
		System.out.println("closingTag: " + closingTag);
		if(closingTag.equals("wpt"))
		{
			WalkrouteSummary curRoute = new WalkrouteSummary(routeName,routeDescription);
			curRoute.setStart(curPoint);
			curRoute.setId(Integer.parseInt(routeId));
			System.out.println("Distance=" + curDistance);
			curRoute.setDistance(curDistance);
			routes.add(curRoute);
			inWpt = false;
		}
		else if (closingTag.equals("name"))
		{
			inName=false;
		}
		else if (closingTag.equals("desc"))
		{
			inDesc=false;
		}
		else if(closingTag.equals("cmt"))
		{
			inId = false;
		}
		else if (closingTag.equals("extensions"))
		{
			inExtensions = false;
		}
		else if (closingTag.equals("distance"))
		{
			inDistance = false;
			System.out.println("Found a closing distance tag: " + strDist);
			curDistance = strDist.equals("")  ? -1.0 : Double.parseDouble(strDist);
		}
	}

	public void characters(char[] ch, int start, int length)
	{
		String text=new String(ch,start,length);
		if(inName)
		{	
			routeName += text;
		}
		else if (inDesc)
		{
			routeDescription += text;
		}
		else if (inId)
		{
			routeId += text;
		}
		else if (inDistance)
		{
			strDist += text;
		}
	}
	
	public Object getData()
	{
		return routes;
	}
	
	public void reset()
	{
		routes = new ArrayList<WalkrouteSummary>();
	}
}

