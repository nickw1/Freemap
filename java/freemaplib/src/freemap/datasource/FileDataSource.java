package freemap.datasource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class FileDataSource extends DataSource
{
	public FileDataSource(String basePath,FileFormatter formatter)
	{
		super(basePath,formatter);
	}
	
	public FileDataSource(String filename)
	{
		super(filename);
	}
	
	protected InputStream getInputStream(String filename) throws IOException
	{
		FileInputStream fis = new FileInputStream(filename);
		return fis;
	}
}
