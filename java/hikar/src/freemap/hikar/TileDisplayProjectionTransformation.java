package freemap.hikar;

import freemap.data.Projection;
import freemap.data.Point;

public class TileDisplayProjectionTransformation {

    Projection tilingProj, displayProj;
    double multiplier;
    Point displacement;
    
    public TileDisplayProjectionTransformation (Projection tilingProj, Projection displayProj, double multiplier)
    {
        this.tilingProj = tilingProj;
        this.displayProj = displayProj;
        this.multiplier = multiplier;
        this.displacement = new Point (0.0, 0.0, 0.0);
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
        
        applyMultiplier(projected);
      
        
        return projected;
       
    }
    
    public Point lonLatToDisplay (Point p)
    {
        Point projected = new Point(p.x, p.y, p.z);
        if(!(displayProj==null  || displayProj.getID().equals("epsg:4326")))
        {    
        
            projected = displayProj.project(projected);
        }
        
        applyMultiplier(projected);
       
        return projected;
    }
    
    private void applyMultiplier(Point projected)
    {
        projected.x *= multiplier;
        projected.y *= multiplier;
        projected.z *= multiplier;

        projected.x += displacement.x;
        projected.y += displacement.y;
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
    
    public void setDisplacement (double dx, double dy)
    {
        displacement.x = dx;
        displacement.y = dy;
    }
}
