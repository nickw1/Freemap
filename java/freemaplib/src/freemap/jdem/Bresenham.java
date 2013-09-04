
package freemap.jdem;

public class Bresenham {

   
    // Input: array of x,y coords of source and target points.ex
	// This will work for all 8 "octants" because:
	// - ax1 and ax2 store the longer and shorter axis appropriately; we use ax1 and ax2 to identify
	//   count1 (the count along the longer axis) and count2 (the count along the shorter axis). 
	// - sgn1 and sgn2 represent the sign of the direction of travel along the longer and shorter
	//   axes respectively.
	// - d2 is the slope value to change count2 by. This will be the slope if ax1 is x and 1/slope if
	//   ax2 is x.
	// To decouple from a given use case, it returns, as output, an array of the coords along each
	// axis for the line. It does not give values in an array such as a DEM; this is done
	// separately. 
	
    public static int[][] getPath( int[] origin, int[] target)
    {
    	// Find the axis with the largest distance and give the path array that capacity - it has
    	// to store the coords of the other axis for each integer. Call this the "first axis"
        int dx = Math.abs(target[0]-origin[0]), dy = Math.abs(target[1]-origin[1]);
        int[][] path = new int[dx>dy ? dx+1:dy+1][2];
        
        // ax1 is the longest axis, 0 means it's x, 1 means it's y
        int ax1 = dx>dy?0:1;
        
        // ax2 is the other axis
        int ax2= ax1==1 ? 0:1;
        
        // sign of axis 1 and 2
        int sgn1 = (int)Math.signum((double)target[ax1]-origin[ax1]), sgn2 = 
                (int)Math.signum((double)target[ax2]-origin[ax2]); 
        
        // Calculate the slope (unsigned)
        double slope = (double)dy / (double) dx;
        
        // change in the second axis per step - depends on whether first axis is x or y
        double d2 = ax1==0 ? slope: 1/slope, err=0.0;
        
        int count1=0, count2=0, index=0;
        
        while(origin[ax1]+count1 != target[ax1]+sgn1)
        {
        	path[index][ax1] = origin[ax1] + count1;
        	
        	// Put the indices of the shorter axis in an array
        	path[index++][ax2] = origin[ax2] + count2;
        	
        	
        	// Increase value of longer axis by sign
        	count1 += sgn1;
        	
        	// Increase error in count2 by change in second axis per step (remember d2 is unsigned)
        	err += d2;
        	
        	// If err exceeds 1 we know we can increase count2 by the sign of the direction of the shorter 
        	// axis. Since err is always positive we reduce it by 1. 
        	if(err > 1.0)
        	{
        		count2 += sgn2;
        		err -= 1.0;
        	}
        }
        
        return path;
    }
}