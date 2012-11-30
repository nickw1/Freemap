package freemap.opentrail;

import freemap.data.Walkroute;
import java.io.IOException;
import java.io.FileInputStream;
import freemap.datasource.XMLDataInterpreter;
import freemap.datasource.WalkrouteHandler;
import org.xml.sax.SAXException;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.File;

// manage caching walkroutes
public class WalkrouteCacheManager {

	String cacheDir;
	
	public WalkrouteCacheManager(String cacheDir)
	{
		this.cacheDir = cacheDir;
	}
	
	public void addWalkrouteToCache(Walkroute wr) throws IOException
	{
		String xml = wr.toXML();
		DataOutputStream out = new DataOutputStream(new FileOutputStream(cacheDir+"/"+wr.getId()+".xml"));
		out.writeBytes(xml);
		out.close();
	}
	
	public boolean isInCache(int wrId)
	{
		return new File(cacheDir+"/"+ wrId + ".xml").exists();
	}
	
	public Walkroute getWalkrouteFromCache(int id) throws IOException, SAXException
	{
		FileInputStream in = new FileInputStream(cacheDir+"/"+ id + ".xml");
		XMLDataInterpreter interpreter = new XMLDataInterpreter(new WalkrouteHandler());
		Walkroute wr = (Walkroute)interpreter.getData(in);
		in.close();
		return wr;
	}
}
