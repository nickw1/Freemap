package freemap.datasource;


import freemap.data.Walkroute;
import org.xml.sax.Attributes;
import freemap.data.TrackPoint;
import java.util.ArrayList;

public class WalkroutesHandler extends XMLDataHandler{
	ArrayList<Walkroute> routes = new ArrayList<Walkroute>();
	String curTag, routeName,routeDescription, routeId;
	boolean  inName, inDesc,inWpt, inId;
	TrackPoint curPoint = new TrackPoint();
	
	public void startElement(String uri,String localName, String qName,Attributes attrs)
	{
		curTag = (localName.equals("")) ? qName:localName;
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
	}
	
	public void endElement(String uri,String localName,String qName)
	{
		String closingTag = (localName.equals("")) ? qName: localName;
	
		if(closingTag.equals("wpt"))
		{
			Walkroute curRoute = new Walkroute(routeName,routeDescription);
			curRoute.addPoint(curPoint);
			curRoute.setId(Integer.parseInt(routeId));
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
	}
	
	public Object getData()
	{
		return routes;
	}
	
	public void reset()
	{
		routes = new ArrayList<Walkroute>();
	}
}

