package freemap.jdem;



import freemap.data.Point;
import freemap.data.Projection;
import freemap.datasource.DataInterpreter;
import freemap.datasource.DataSource;
import freemap.datasource.TiledData;

public class HGTTileDeliverer extends freemap.datasource.CachedTileDeliverer {

	int demWidth, demHeight;
	double demRes;
	
	public HGTTileDeliverer(String name,DataSource ds, DataInterpreter interpreter,int tileWidth,int tileHeight,
			Projection proj, int demWidth, int demHeight, double demRes,String cachedir)
	{
		super(name,ds,interpreter,tileWidth,tileHeight,proj,cachedir);
		this.demWidth = demWidth;
		this.demHeight = demHeight;
		this.demRes = demRes;
	}

	
	
	protected TiledData dataWrap(Point origin,Object rawData)
	{
		if(rawData!=null)
		{
		   
			DEM dem = new DEM(origin,demWidth,demHeight,demRes,proj);
		
			dem.setHeights((int[])rawData);
			
			return dem;
		}
		return null;
	}
	
	// NEW
	public boolean lineOfSight(Point p1, Point p2)
    {
       // Get the DEMs corresponding to p1 and p2
       DEM dem1 = (DEM)getData(p1), dem2 = (DEM)getData(p2);
       
       //System.out.println("lineOfSight from " + p1 + " to " + p2);
       if(dem1 != null && dem2 != null)
       {
           int demw = dem1.getPtWidth()-1, demh = dem1.getPtHeight()-1; // -1 for boundary points
           int[] ggp1 = getGlobalGridPos (dem1, p1), ggp2 = getGlobalGridPos (dem2, p2);
           //System.out.println("Global grid pos for p1: " + ggp1[0] + " " + ggp1[1]);
           //System.out.println("Global grid pos for p2: " + ggp2[0] + " " + ggp2[1]);
           double srcHt = p1.z < 0 ? dem1.getHeight(p1.x,p1.y,proj): p1.z,
                   destHt = p2.z < 0 ? dem2.getHeight(p2.x,p2.y,proj): p2.z; // points can have precalculated ht
           int[] tileOrigin = new int[2], localGridPos = new int[2], curTileID = new int[2];
       
           //System.out.println("Heights: " + srcHt +" " + destHt);
           if(ggp1!=null && ggp2!=null)
           {
               int[][] path = Bresenham.getPath(ggp1, ggp2);
               //System.out.println("Got path");
               double ht, htInc = (destHt - srcHt) / path.length, expHt = srcHt;
               for(int i=0; i<path.length; i++)
               {
                   curTileID[0] = (int)Math.floor((double) path[i][0] / demw);
                   curTileID[1] = (int)Math.floor((double)path[i][1] / demh);
                   
                   // Find the DEM corresponding to the current path point
                   DEM curDEM = (DEM)tileIDToData(curTileID);
                   if(curDEM!=null)
                   {
                       //System.out.println("Path current point: " + path[i][0] + " " +path[i][1]  + " current tile ID: " + curTileID[0] + " "+ curTileID[1]);
                       
                       // Find the origin of the tile in global grid points
                       tileOrigin[0] = curTileID[0] * demw;
                       tileOrigin[1] = curTileID[1] * demh;
                       
                       //System.out.println("Tile origin in global grid points: " + tileOrigin[0] + " " + tileOrigin[1]);
                       
                       // Use this to get local grid points
                       localGridPos[0] = path[i][0] - tileOrigin[0];
                       localGridPos[1] = path[i][1] - tileOrigin[1];
                       
                       //System.out.println("Local grid pos: " + localGridPos[0] +" " + localGridPos[1]);
                       
                       // Use the local grid points to find the height
                       ht = curDEM.getHeight(localGridPos);
                       //System.out.println("Height=" + ht + " expHt=" + expHt);
                       
                       // If the surface is above the line-of-sight, return false
                      
                       if(ht > expHt)
                           return false;
                       
                   }  
                   expHt += htInc;
               }
               return true;
           }
       }
       return false;
    }
    
    public int[] getGlobalGridPos(DEM data, Point p)
    {
     
        int[] gridPos = new int[2];
      
        if(data!=null)
        {  
            // Grid position of point on this DEM
            gridPos = data.pointToGridPosition(p, proj);
            
            
            // get the ID of this tile (the x of the ID increases by 1 rightwards, the y downwards)
            int[] tileID = getTileID(p);
           
            
            // convert to a global grid position
            // Number of points in DEM minus 1 (because boundary points repeated)
            gridPos[0] += tileID[0]*(data.getPtWidth()-1);
            gridPos[1] += tileID[1]*(data.getPtHeight()-1);
            
           
        }
        return gridPos;
    }
	// NEW END
}
