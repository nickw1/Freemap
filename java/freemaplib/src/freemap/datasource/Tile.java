package freemap.datasource;

import freemap.data.Point;

public class Tile {
    public Point origin;
    public TiledData data;
    public boolean isCache;
    
    public Tile(Point origin,TiledData data, boolean isCache)
    {
        this.origin=origin;
        this.data=data;
        this.isCache=isCache;
    }
}
