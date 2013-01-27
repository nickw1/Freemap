package freemap.datasource;

import freemap.data.Annotation;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.ArrayList;
import org.xml.sax.SAXException;

public class AnnotationCacheManager {

	File cachedir;
	
	public AnnotationCacheManager(String cachedir)
	{
		this.cachedir=new File(cachedir);
		if(!this.cachedir.exists())
			this.cachedir.mkdirs();
	}
	
	public void addAnnotation(Annotation ann) throws IOException
	{
		PrintWriter pw = new PrintWriter(new FileWriter(cachedir.getAbsolutePath()+"/"+(-ann.getId())+".xml"));
		ann.save(pw);
		pw.close();
	}
	
	public String getAllAnnotationsXML() throws IOException
	{
		String[] filenames = cachedir.list();
		String xml = "";
		if(filenames.length>0)
		{
			xml = "<rdata>";
			BufferedReader reader = null;
			String line="";
			for(int i=0; i<filenames.length; i++)
			{
				reader = new BufferedReader(new InputStreamReader
					(new FileInputStream(cachedir.getAbsolutePath()+"/"+filenames[i])));
				while((line=reader.readLine())!=null)
					xml += line;
			}
			xml += "</rdata>";
		}
		return xml;
	}
	
	public void deleteCache()
	{
		File[] files = cachedir.listFiles();
		for(int i=0; i<files.length; i++)
			files[i].delete();
	}
		
	public boolean isEmpty()
	{
		return cachedir.list().length==0;
	}
	
	public int size()
	{
		return cachedir.list().length;
	}
	
	public ArrayList<Annotation> getAnnotations() throws IOException, SAXException
	{
		FreemapDataset ds = getAnnotationsAsDataset();
		return ds.getAnnotations();
	}
	
	public FreemapDataset getAnnotationsAsDataset() throws IOException,SAXException
	{
		String xml = getAllAnnotationsXML();
		if(!(xml.equals("")))
		{
			XMLDataInterpreter interpreter = new XMLDataInterpreter(new FreemapDataHandler());
			FreemapDataset ds=(FreemapDataset)interpreter.getData(getAllAnnotationsXML());
			return ds;
		}
		return new FreemapDataset();
	}
}
