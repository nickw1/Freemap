package freemap.hikar;

import freemap.data.Projection;
import freemap.data.Point;

public class TileDisplayProjectionTransformation {

    Projection tilingProj, displayProj;
    double multiplier;
    
    public TileDisplayProjectionTransformation (Projection tilingProj, Projection displayProj, double multiplier)
    {
        this.tilingProj = tilingProj;
        this.displayProj = displayProj;
        this.multiplier = multiplier;
    }
    
    public Point tileToDisplay (Point p)
    {
        Point projected = new Point(p.x, p.y, p.z);
        if(!(tilingProj==null && displayProj==null || tilingProj.getID().equals(displayProj.getID())))
        { 
            
            if(tilingProj != null)
                projected = tilingProj.unproject(projected);
            if(displayProj != null)
                projected = displayProj.project(projected);
        }
        projected.x *= multiplier;
        projected.y *= multiplier;
        projected.z *= multiplier;
        
        return projected;
       
    }
    
    public void setTilingProj(Projection tilingProj)
    {
        this.tilingProj = tilingProj;
    }
    
    public void setDisplayProj(Projection displayProj)
    {
        this.displayProj = displayProj;
    }
    
    public Projection getTilingProj()
    {
        return tilingProj;
    }
    
    public Projection getDisplayProj()
    {
        return displayProj;
    }
    
    public void setMultiplier (double multiplier)
    {
        this.multiplier = multiplier;
    }
    
    
    public double getMultiplier()
    {
        return multiplier;
    }
}
