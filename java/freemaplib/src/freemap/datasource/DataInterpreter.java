package freemap.datasource;

import java.io.InputStream;

// Allows XML and non-XML data sources to have a common interface
public interface DataInterpreter {
	public Object getData(InputStream in) throws Exception;
}
