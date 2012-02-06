package freemap.datasource;

import java.io.InputStream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;


public class XMLDataInterpreter implements DataInterpreter {

	XMLDataHandler handler;
	
	public XMLDataInterpreter(XMLDataHandler handler)
	{
		this.handler=handler;
		
	}
	
	public Object getData(InputStream in) throws Exception
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		XMLReader reader = parser.getXMLReader();
		handler.reset();
		reader.setContentHandler(handler);
		reader.parse(new InputSource(in));
		return handler.getData();
	}
}
