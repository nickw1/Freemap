package freemap.jdem;

import java.io.InputStream;
import freemap.datasource.DataInterpreter;
import freemap.datasource.TiledData;

public class HGTDataInterpreter extends DEMSource implements DataInterpreter {

	public HGTDataInterpreter( int width,int height, double resolution, int endianness)
	{
		super(width,height,resolution,endianness);
	}
	
	public Object getData (InputStream in) throws Exception
	{
		return load(in);
	}
}
