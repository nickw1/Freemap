package freemap.datasource;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import freemap.data.Point;


public abstract class DataSource  {
	protected FileFormatter formatter;
	String basePath;
	

	public DataSource(String basePath, FileFormatter formatter)
	{
		System.out.println("StreamDataSource constructor: basePath="+basePath);
		this.basePath=basePath;
		this.formatter=formatter;
	}
	
	public DataSource(String filename)
	{
		this.basePath=filename;
	}
	
	public Object getData(Point bottomLeft, DataInterpreter interpreter) throws Exception
	{
	    return getData(bottomLeft, interpreter, null);
	}
	
	public Object getData(Point bottomLeft, DataInterpreter interpreter, String cacheFile) throws Exception
	{
		InputStream in = getInputStream(bottomLeft);
		
		System.out.println("DataSource.getData(): cacheFile=" + cacheFile);
		if(cacheFile!=null)
		{
		    FileOutputStream out = new FileOutputStream(cacheFile);  
		    byte[] bytes = new byte[1024];
		    int nRead;
		    while((nRead = in.read(bytes)) > -1)
		        out.write(bytes, 0, nRead);
		    out.flush();
		    out.close();
		    in.close();  
		    in = new FileInputStream(cacheFile);
		    System.out.println("wrote to CacheFile, now reading from it");
		}
		
		Object data= interpreter.getData(in);
		in.close();
		return data;
	}
	
	public Object getData(DataInterpreter interpreter) throws Exception
	{
		return interpreter.getData(getInputStream(basePath));
	}
	
	protected InputStream getInputStream(Point bottomLeft) throws IOException
	{
	
		String filename = basePath + "/" + formatter.format(bottomLeft);
		InputStream is = getInputStream(filename);
		return is;
	}
	
	public FileFormatter getFormatter()
	{
		return formatter;
	}
	
	protected abstract InputStream getInputStream(String filename) throws IOException;
}



