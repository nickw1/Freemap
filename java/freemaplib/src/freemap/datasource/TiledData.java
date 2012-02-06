package freemap.datasource;

import java.io.IOException;
import freemap.data.Projection;

public interface TiledData {

	public void save(String filename) throws IOException;
	public void reproject(Projection proj);
	public void merge(TiledData otherData);
}
