package freemap.jdem;

import freemap.data.Point;
import freemap.data.Projection;

import java.io.FileOutputStream;

import java.io.IOException;

import freemap.datasource.TiledData;

public class DEM implements freemap.datasource.TiledData { 
	private Point bottomLeft, topRight;
	int ptWidth, ptHeight;
	double spacing;
	int[] heights;
	Projection proj;
	
	public DEM (Point bottomLeft, int ptWidth, int ptHeight, double spacing)
	{
		initialise(bottomLeft,ptWidth,ptHeight,spacing,null);
	}
	
	public DEM (Point bottomLeft, int ptWidth, int ptHeight, double spacing, Projection proj)
	{
		initialise(bottomLeft,ptWidth,ptHeight,spacing,proj);
	}
	
	private void initialise(Point bottomLeft, int ptWidth, int ptHeight, double spacing, Projection proj)
	{
		this.bottomLeft=bottomLeft;
		
		topRight = new Point();
		this.ptWidth=ptWidth;
		this.ptHeight=ptHeight;
		this.spacing=spacing;
		this.proj=proj;
		topRight.x = bottomLeft.x + (ptWidth-1)*spacing;
		topRight.y = bottomLeft.y + (ptWidth-1)*spacing;
	}
	
	public void setHeights(int[] heights)
	{
		this.heights=heights;
	}
	
	// Uses bilinear interpolation
	// Based on Footnav code
	public double getHeight(double lon, double lat,Projection coordProj)
	{
		Point p = new Point(lon,lat);
	
		
		/*
		
		System.out.println("getHeight(): lon="+lon+ " lat="+lat+ " coordProj=" + coordProj +
				" proj=" + proj);
		*/
		
		if((!(proj==null && coordProj==null)) && (!(proj!=null && proj.equals(coordProj))))
		{
			// unproject the input to lon/lat if it's not lon/lat
		
			//System.out.println("p was: " + p);
			if(coordProj!=null)
			{
				p=coordProj.unproject(p);
			}
		
			//System.out.println("unprojected p: " + p);
			// Project lon/lat into the native projection of the DEM
			if(proj!=null)
			{
				p=proj.project(p);
			}
			
		}
		//System.out.println("Projected point: " + p+" bottomLeft=" + bottomLeft);
		
		int xIdx = (int)((p.x-bottomLeft.x) / spacing),
			yIdx = ptHeight-((int)Math.ceil((p.y - bottomLeft.y) / spacing));
		
		//System.out.println("Indices: "+xIdx+" " + yIdx);
		double x1,x2,y1,y2;
		double h1,h2,h3,h4;

		if(xIdx>=0 && yIdx>=0 && xIdx<ptWidth-1 && yIdx<ptHeight-1)
		{
			h1 = heights[yIdx*ptWidth+xIdx];
			h2 = heights[yIdx*ptWidth+xIdx+1];
			h3 = heights[yIdx*ptWidth+xIdx+ptWidth];
			h4 = heights[yIdx*ptWidth+xIdx+ptWidth+1];
			
			x1 = bottomLeft.x + xIdx*spacing;
			x2 = x1 + spacing;
			
			y1 = bottomLeft.y + (ptHeight-yIdx)*spacing;
			y2 = y1 - spacing;
			
			//System.out.println("x,y bounds " + x1 + " " + y1+ " " +x2 + " " +y2);
			//System.out.println("heights " + h1 + " " + h2+ " " +h3 + " " +h4);
			
			double propX = (p.x-x1)/(x2-x1);
			
			double htop = h1*(1-propX) + h2*propX,
				hbottom = h3*(1-propX) + h4*propX;
			
			double propY = (p.y-y2)/(y1-y2);
			
			double h = hbottom*(1-propY) + htop*propY;
			
			//System.out.println("*******************************height is: " + h);
			return h;
		}
		
		return -1;
	}
	
	public String toString()
	{
		String s= "bottomLeft: " + bottomLeft + " spacing=" + spacing + " heights=" +
			"[";
		
		for(int i=0; i<ptHeight; i++)
		{
			s+="Row " + i + ":";
			for(int j=0; j<ptWidth; j++)
			{
				s+=heights[i*ptWidth+j]+ " ";
			}
			s+="\n";
		}
		return s;
	}
	
	public boolean pointWithin(Point lonLat,Projection coordProj)
	{
		Point p = reprojectPoint(lonLat, coordProj);	
		return p.x >= bottomLeft.x && p.x <= topRight.x && p.y >= bottomLeft.y && p.y <= topRight.y;
	}
	
	public void save(String filename) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(filename);
		for(int i=0; i<heights.length; i++)
		{
			fos.write(heights[i]%256);
			fos.write(heights[i]/256);
		}
	}
	
	// DEMs don't reproject themselves
	public void reproject(Projection proj)
	{
	}
	
	// TODO
	public void merge(TiledData other)
	{
		
	}
	
	public Point getBottomLeft()
	{
	    return bottomLeft;
	}
	
	public Point getTopRight()
	{
	    return topRight;
	}
	
	public Projection getProjection()
	{
	    return proj;
	}
	
	// NEW
	public Point reprojectPoint(Point p, Projection coordProj)
    {
        if((!(proj==null && coordProj==null)) && (!(proj!=null && proj.equals(coordProj))))
        {
            // unproject the input to lon/lat if it's not lon/lat
        
            
            if(coordProj!=null)
            {
                p=coordProj.unproject(p);
            }
        
            
            // Project lon/lat into the native projection of the DEM
            if(proj!=null)
            {
                p=proj.project(p);
            }
            
        }
        return p;
    }
    
	public int[] pointToGridPosition(Point lonLat, Projection coordProj)
    {
        // bottomLeft ptWidth ptHeight spacing
        Point p = new Point(lonLat.x, lonLat.y);
        reprojectPoint(p,coordProj);
        int[] gridPos = new int[2];
        gridPos[0] = (int)((p.x-bottomLeft.x) / spacing);
        gridPos[1] = ptHeight-1-((int)Math.ceil((p.y - bottomLeft.y) / spacing));
        return gridPos;
    }
    
    public int gridPositionToIndex(int[] gridPos)
    {
        return gridPos[1]*ptWidth + gridPos[0];
    }
    
    public double getHeight(int[] gridPos)
    {
        int idx = gridPositionToIndex(gridPos);
        return idx>=0 && idx<heights.length ? heights[idx]:null;
    }
    
    public boolean isInSight(Point p1, Point p2, Projection coordProj)
    {
        if (pointWithin(p1,coordProj) && pointWithin(p2,coordProj))
        {
            double startHeight = getHeight(p1.x, p1.y, coordProj), endHeight = getHeight(p2.x,p2.y,coordProj);
            int[] gridPos1 = pointToGridPosition(p1,coordProj), gridPos2 = pointToGridPosition(p2,coordProj);
            
            int[][] path = Bresenham.getPath(gridPos1, gridPos2);
            int curIndex;
            
            double htInc = (endHeight - startHeight) / path.length, expHt = startHeight;
            
            for(int i=0; i<path.length; i++)
            {
                curIndex = gridPositionToIndex(path[i]);
                if(heights[curIndex] > expHt)
                    return false;
                expHt += htInc;
            }
            return true;
        }
        return false;
    }
    
    public int getPtWidth()
    {
        return ptWidth;
    }
    
    public int getPtHeight()
    {
        return ptHeight;
    }
    
    public Point getPoint(int col, int row)
    {
        Point p = new Point();
        p.x = bottomLeft.x + col*spacing;
        p.y = topRight.y - row*spacing;
        p.z = heights[row*ptWidth+col];
        return p;
    }
    
    public Point getPoint(int index)
    {
        return getPoint(index%ptWidth, index/ptHeight);
    }
    // NEW END
}

