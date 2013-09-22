package freemap.datasource;

import java.io.IOException;
import java.io.InputStream;

import freemap.data.Point;

public class RawDataSource {
	

	public static String doLoad(InputStream fis) throws IOException
	{
		
		byte[] bytes = new byte[1024];
		StringBuffer text = new StringBuffer();
		
		int bytesRead;
		
		while((bytesRead=fis.read(bytes,0,1024))>=0)
		{
		    text.append(new String(bytes,0,bytesRead));
		}
		
		return text.toString();
	}
}
