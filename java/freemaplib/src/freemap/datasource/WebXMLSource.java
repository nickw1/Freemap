package freemap.datasource;

import java.io.InputStream;
import java.io.IOException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;
import org.xml.sax.SAXException;

public class WebXMLSource {

	String url;
	XMLDataInterpreter interpreter;
	int statusCode;
	
	public WebXMLSource(String url, XMLDataHandler handler)
	{
		this.url = url;
		interpreter = new XMLDataInterpreter(handler);
	}
	
	public Object getData() throws IOException, SAXException
	{
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);
		statusCode = response.getStatusLine().getStatusCode();
		if(statusCode==200)
		{
			InputStream stream = response.getEntity().getContent();
			return interpreter.getData(stream);
		}
		return null;
	}
	
	public int getStatusCode()
	{
		return statusCode;
	}
	
	/*
	public static void main (String args[])
	{
		try
		{
			WebXMLSource source = new WebXMLSource
				("http://www.free-map.org.uk/0.6/ws/wr.php?action=getByBbox&bbox=-1,51,0,52&format=gpx", new WalkroutesHandler());
			System.out.println(source.getData());
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
	*/
}
