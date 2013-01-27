package freemap.datasource;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Date;
import java.text.ParseException;

import freemap.data.Walkroute;
import org.xml.sax.Attributes;
import freemap.data.TrackPoint;

public class WalkrouteHandler extends XMLDataHandler{
	Walkroute theRoute;
	String curTag, routeName,routeDescription,stageName,stageDescription,routeNumber,curTime;
	boolean inTrk, inName, inDesc,inWpt,inTrkpt,inNumber,inTime;
	TrackPoint curPoint;
	SimpleDateFormat timestampFormat;
	
	public WalkrouteHandler()
	{
		timestampFormat = new SimpleDateFormat("yyyy-MM-dd 'T'HH:mm:ss'Z'");
		timestampFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
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
				curPoint = new TrackPoint(Double.parseDouble(attrs.getValue("lon")),
									Double.parseDouble(attrs.getValue("lat")));
				if(curTag.equals("trkpt"))
				{
					curTime = "";
					inTrkpt = true;
				}
				else
				{
					inWpt = true;
				}
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
		else if (curTag.equals("time")&& inTrkpt)
		{
			inTime = true;
			
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
		else if (closingTag.equals("trkpt"))
		{
			inTrkpt = false;
			try
			{
				Date date = timestampFormat.parse(curTime);
				curPoint.setTime(date.getTime());
			}
			catch(ParseException e) { }
			theRoute.addPoint(curPoint);
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
		else if (closingTag.equals("time") && inTrkpt)
		{
			inTime = false;
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
		else if (inTime)
		{
			curTime += text;
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
