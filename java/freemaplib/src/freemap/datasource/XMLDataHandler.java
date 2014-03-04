package freemap.datasource;

import org.xml.sax.helpers.DefaultHandler;
;

public abstract class XMLDataHandler extends DefaultHandler  {
	//protected Object data;
	
	public abstract void reset(); // for initialising the data, e.g. if we want to load multiple tiles
	
	public abstract Object getData();
}

