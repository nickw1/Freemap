package freemap.datasource;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import freemap.data.Feature;
import freemap.data.GoogleProjection;
import freemap.data.POI;
import freemap.data.Point;
import freemap.data.Projection;
import freemap.data.Way;

public abstract class XMLDataHandler extends DefaultHandler  {
	protected Object data;
	
	public abstract void reset(); // for initialising the data, e.g. if we want to load multiple tiles
	
	public Object getData()
	{
		return data;
	}
}

