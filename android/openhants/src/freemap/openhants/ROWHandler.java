package freemap.openhants;

import freemap.datasource.XMLDataHandler;
import java.util.HashMap;
import org.xml.sax.Attributes;

public class ROWHandler extends XMLDataHandler {

	HashMap<String,String> theROW;
	String curDistrict, curParish, curRowType, curRouteno, curInfo, curParishRow, curTag, curText;
	
	public ROWHandler()
	{
		reset();
	}
	
	public void reset()
	{
		theROW = new HashMap<String,String>();
	}
	
	
	public Object getData()
	{
		return theROW;
	}
	
	public void startElement(String uri, String name,String qName, Attributes attrs)
	{	
		curTag = (name.equals("")) ? qName:name;
		curText = "";
	}
	
	public void endElement(String uri,String name,String qName)
	{
		theROW.put(curTag,curText);
	}
	
	public void characters(char[] ch, int start, int end)
	{
		curText += new String(ch,start,end);
	}
}
