package freemap.spatialite;

import freemap.data.Point;
import freemap.data.Projection;
import freemap.datasource.BaseTileDeliverer;
import freemap.datasource.TiledData;

public class SpatialiteTileDeliverer extends BaseTileDeliverer {

    String dbName, dbPath;
    
    public SpatialiteTileDeliverer(String name,int tileWidth,int tileHeight,Projection proj, String dbName,
                                    String dbPath)
    {
        super (name, tileWidth, tileHeight, proj);
        this.dbName = dbName;
        this.dbPath = dbPath;
    }
    
    public TiledData getDataFromSource(Point origin) 
    {
        return null;
    }
}
