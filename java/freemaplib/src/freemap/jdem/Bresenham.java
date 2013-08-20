package freemap.jdem;

import java.util.ArrayList;

public class Bresenham {

   
    
    public static int[] getPath(int targetX, int targetY)
    {
        int dx = Math.abs(targetX), dy = Math.abs(targetY);
        int[] path = new int[dx>dy ? dx:dy];
        double slope = (double)dy / (double) dx;
        int ax1 = (dx>dy)?0:1;
        int ax2= ax1==1 ? 0:1;
        
        int sgn1 = Math.signum((double)
        return path;
    }
    
}
