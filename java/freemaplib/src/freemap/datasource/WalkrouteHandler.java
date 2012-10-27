package freemap.datasource;

import freemap.data.Walkroute;
import org.xml.sax.Attributes;
import freemap.data.Point;

public class WalkrouteHandler extends XMLDataHandler{
	Walkroute theRoute;
	String curTag, routeName,routeDescription,stageName,stageDescription,routeNumber;
	boolean inTrk, inName, inDesc,inWpt,inNumber;
	Point curPoint;
	
	public void startElement(String uri,String localName, String qName,Attributes attrs)
	{
		curTag = (localName.equals("")) ? qName:localName;
		if(curTag.equals("trk"))
		{
			inTrk=true;
			theRoute = new Walkroute();
		}
		else if (curTag.equals("wpt") || curTag.equals("trkpt"))
		{
			if(attrs.getValue("lat")!=null && attrs.getValue("lon")!=null)
			{
				curPoint = new Point(Double.parseDouble(attrs.getValue("lon")),
									Double.parseDouble(attrs.getValue("lat")));
			}
			
			if(curTag.equals("trkpt"))
			{
				theRoute.addPoint(curPoint);
			}
			else
			{
				inWpt = true;
			}
		}
		else if (curTag.equals("name"))
		{
			inName = true;
			if(inWpt)
				stageName = "";
			else if (inTrk)
				routeName = "";
		}
		else if (curTag.equals("desc"))
		{
			inDesc = true;
			if(inWpt)
				stageDescription = "";
			else if (inTrk)
				routeDescription = "";
		}
		else if (curTag.equals("number") && inTrk)
		{
			inNumber = true;
			routeNumber = "";
		}
	}
	
	public void endElement(String uri,String localName,String qName)
	{
		String closingTag = (localName.equals("")) ? qName: localName;
	
		if(closingTag.equals("wpt"))
		{
			theRoute.addStage(curPoint,stageDescription);
			inWpt = false;
		}
		else if (closingTag.equals("trk"))
		{
			theRoute.setTitle(routeName);
			theRoute.setDescription(routeDescription);
			routeNumber = (routeNumber.equals("")) ? "0": routeNumber;
			theRoute.setId(Integer.parseInt(routeNumber));
			inTrk = false;
		}
		else if (closingTag.equals("name"))
		{
			inName=false;
		}
		else if (closingTag.equals("desc"))
		{
			inDesc=false;
		}
		else if (closingTag.equals("number"))
		{
			inNumber=false;
		}
	}

	public void characters(char[] ch, int start, int length)
	{
		String text=new String(ch,start,length);
		if(inName)
		{
			if(inWpt)
				stageName += text;
			else if (inTrk)
				routeName += text;
		}
		else if (inDesc)
		{
			if(inWpt)
				stageDescription += text;
			else if (inTrk)
				routeDescription += text;
		}
		else if (inNumber)
		{
			routeNumber += text;
		}
	}
	
	public Object getData()
	{
		return theRoute;
	}
	
	public void reset()
	{
		theRoute = new Walkroute();
	}
	
}
