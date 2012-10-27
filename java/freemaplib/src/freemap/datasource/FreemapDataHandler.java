package freemap.datasource;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import freemap.data.Feature;
import freemap.data.GoogleProjection;
import freemap.data.POI;
import freemap.data.Point;
import freemap.data.Projection;
import freemap.data.Way;
import freemap.data.ProjectionFactory;
import freemap.data.Annotation;

public class FreemapDataHandler extends XMLDataHandler
{
	
	Projection proj;
	boolean inProjection,inAnnotation,inDescription;
	Point curPoint;
	String curTag, curKey, curVal;
	Feature curFeature;
	Annotation curAnnotation;
	boolean doUnproject;
	ProjectionFactory factory;
	int curID;
	String curDescription;
	FreemapDataset data;
	
	class DefaultProjectionFactory implements ProjectionFactory
	{
		public Projection generate(String str)
		{
			Projection proj=null;
			if(str.equalsIgnoreCase("EPSG:900913") || str.equalsIgnoreCase("EPSG:3785") ||
					str.equalsIgnoreCase("google"))
			{
				proj = new GoogleProjection();
			}
			
			return proj;
		}
	}
	
	public FreemapDataHandler()
	{
		init(new DefaultProjectionFactory());
	}
	
	public FreemapDataHandler(ProjectionFactory factory)
	{
		init(factory);
	}
	
	private void init(ProjectionFactory factory)
	{
		
		reset();
		curPoint=new Point();
		doUnproject=false;
		this.factory=factory;
	}
	
	
	public void reset()
	{
		data=new FreemapDataset();
	}
	
	
	public void startElement(String uri,String localName,String qName,Attributes attributes)
		throws SAXException
	{
		curTag = (localName.equals("")) ? qName:localName;
		
		if(curTag.equals("way"))
		{
			curFeature = new Way();
		}
		else if(curTag.equals("projection"))
		{
			inProjection=true;
		}
		else if (curTag.equals("point") || curTag.equals("poi") || curTag.equals("annotation"))
		{
			curPoint.x = Double.parseDouble(attributes.getValue("x"));
			curPoint.y = Double.parseDouble(attributes.getValue("y"));
			curPoint.z = (attributes.getValue("z")==null) ? -1:Double.parseDouble(attributes.getValue("z"));
			if(proj!=null && doUnproject==true)
			{
				curPoint = proj.unproject(curPoint);
			}
			
			if(curTag.equals("poi"))
			{
				curFeature = new POI(curPoint.x,curPoint.y);
			}
			else if (curTag.equals("annotation"))
			{
				curID = Integer.parseInt(attributes.getValue("id"));
				inAnnotation=true;
			}
		}
		else if (curTag.equals("tag"))
		{
			
			curKey = attributes.getValue("k");
			curVal = attributes.getValue("v");
			curFeature.addTag(curKey,curVal);
		}
		else if (curTag.equals("description") && inAnnotation)
		{
			inDescription=true;
			curDescription="";
	
		}
	}
	
	public void endElement(String uri,String localName,String qName) 
		throws SAXException
	{
		curTag = (localName.equals("")) ? qName:localName;
	
		if(curTag.equals("projection"))
		{
			inProjection=false;
		}
		else if (curTag.equals("point"))
		{
			((Way)curFeature).addPoint(curPoint.x,curPoint.y,curPoint.z);
		}
		else if (curTag.equals("way"))
		{
			((FreemapDataset)data).add((Way)curFeature);
		}
		else if (curTag.equals("poi"))
		{
			((FreemapDataset)data).add((POI)curFeature);
		
		}
		else if (curTag.equals("annotation"))
		{
			((FreemapDataset)data).add(new Annotation(curID,curPoint.x,curPoint.y,curDescription));
			inAnnotation=false;
		}
		else if (curTag.equals("description") && inAnnotation)
		{
			inDescription=false;
		}
	}
	
	public void characters(char[] ch,int start,int length)
		throws SAXException
	{
		String str = new String(ch,start,length);

		if(inProjection && factory!=null) 
		{
			proj = factory.generate(str);
		}
		else if (inDescription)
		{
			curDescription+=str;
		}
	}
	
	public Object getData()
	{
		return data;
	}
}