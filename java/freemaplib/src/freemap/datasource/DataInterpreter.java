package freemap.datasource;

import java.io.InputStream;
import java.io.IOException;

// Allows XML and non-XML data sources to have a common interface
public interface DataInterpreter {
	public Object getData(InputStream in) throws Exception;
}
