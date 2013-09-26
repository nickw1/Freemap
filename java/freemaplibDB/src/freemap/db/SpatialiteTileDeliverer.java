package freemap.db;

import freemap.data.Point;
import freemap.data.Projection;
import freemap.data.Way;
import freemap.data.Feature;
import freemap.datasource.TiledData;
import freemap.datasource.FreemapDataset;
import freemap.andromaps.GeoJSONDataInterpreter;
import jsqlite.Stmt;


public class SpatialiteTileDeliverer extends freemap.datasource.BaseTileDeliverer {

    String dbName, dbPath;
    jsqlite.Database db;
    
    public SpatialiteTileDeliverer(String name,int tileWidth,int tileHeight,Projection proj, String dbName,
                                    String dbPath)
    {
        super (name, tileWidth, tileHeight, proj);
        this.dbName = dbName;
        this.dbPath = dbPath;
     
        db = new jsqlite.Database();
        
       
    }
    
    public TiledData getDataFromSource(Point origin) throws Exception
    {
        System.out.println("SpatialTileDeliverer: Opening: " + dbPath+"/"+dbName);
        db.open(dbPath+"/"+dbName, jsqlite.Constants.SQLITE_OPEN_READONLY);
        FreemapDataset dataset = new FreemapDataset();
       String projID = proj.getID().replace("epsg:","");
 
        int ox = (int)origin.x, oy = (int)origin.y;
        Point blLL = proj.unproject(origin);
        Point trLL = proj.unproject(new Point(ox+tileWidth, oy+tileHeight));
       
        String box = ox + " " + oy + "," + (ox+tileWidth) + " " + oy + 
                    "," + (ox+tileWidth) + " " + (oy+tileHeight) +
                    "," + ox + " " + (oy+tileHeight) +
                    "," + ox + " " + oy;
        
        String sql = "select asgeojson(transform(geometry,"+projID+")),id,sub_type " +
                    "from ln_highway where intersects( GeomFromText('POLYGON(("+box+"))'," + projID
                    + "),transform(geometry,"+projID+"))";
        /*
        String sql = "select asgeojson(transform(geometry," + projID+")),id,sub_type " +
                    "from ln_highway where id in " +
                    "(select id from idx_ln_highway_geometry where xmin > " +blLL.x +
                    " and xmax < " + trLL.x +
                    " and ymin > " + blLL.y +
                    " and ymax < " + trLL.y + ")";
        */
        System.out.println("SQL is: " + sql);
       
        Stmt stmt = db.prepare(sql);
        System.out.println("Start time: " + System.currentTimeMillis());
        while(stmt.step())
        {
            
            String json = stmt.column_string(0);
            Feature f = GeoJSONDataInterpreter.jsonToFeature(json);
            f.addTag("osm_id", String.valueOf(stmt.column_string(1)));
            f.addTag("highway", stmt.column_string(2));
            dataset.add((Way)f);
            
           
        }
        System.out.println("End time: " + System.currentTimeMillis());
        db.close();
        //System.out.println("Dataset loaded from DB is: " + dataset);
        
        return dataset;
    }
}
