package freemap.jdem;

import java.io.IOException;
import java.io.InputStream;

import freemap.data.Point;

public class DEMSource {
	
	Point bottomLeft;
	double spacing;
	int width,height,endianness;
	public static final int BIG_ENDIAN = 0, LITTLE_ENDIAN = 1;
	
	public DEMSource (int width,int height,double spacing, int endianness)
	{
		this.spacing = spacing;
		this.width=width;
		this.height=height;
		this.endianness = endianness;
	}
	
	public void setBottomLeft(Point bottomLeft)
	{
		this.bottomLeft = bottomLeft;
	}
	
	public int[] load(InputStream fis) throws IOException
	{
		return doLoad(fis,0,0,width-1,height-1);
	}
	
	public DEM load (InputStream fis, 
							Point bottomLeftSelected, Point topRightSelected) throws IOException
	{
		int indexW, indexS, indexE, indexN;

		indexW=(int)((bottomLeftSelected.x-bottomLeft.x)/spacing)+1;
		indexS=height-((int)((bottomLeftSelected.y-bottomLeft.y)/spacing)+1);
		indexE=(int)((topRightSelected.x-bottomLeft.x)/spacing);
		indexN=height-((int)((topRightSelected.y-bottomLeft.y)/spacing));
		
		int[] metres = doLoad(fis,indexW,indexS,indexE,indexN);
	
		DEM dem = new DEM (bottomLeftSelected,(indexE-indexW)+1,(indexS-indexN)+1,spacing);
		dem.setHeights(metres);
		//System.out.println("Done");
		return dem;
	}
	
	private int[] doLoad(InputStream fis,
				int indexW,int indexN,int indexE, int indexS) throws IOException
	{
		int ms,ls;
		int ptWidth = (indexE-indexW)+1;
		int ptHeight = (indexS-indexN)+1;
		int mostSignificant = (endianness==LITTLE_ENDIAN) ? 1: 0,
				leastSignificant = (endianness==LITTLE_ENDIAN) ? 0:1; 
		System.out.println("Allocating DEM: Size of metres array: " + ptWidth*ptHeight);
		int[] metres = new int[ptWidth*ptHeight];
		int rawHeight;
		
		//System.out.println("width=" + width+ " ptWIdth=" + ptWidth+  " ptHeight=" + ptHeight+ " spacing=" + spacing);
		byte[] d2= new byte[1024];
		
		int total=0, bytesRead,metreCount=0,metresTotal;
		
		// The input stream seems to be really eccentrically implemented.
		// We sometimes get less than 1024 bytes sent back, and sometimes an
		// odd number which is a right PITA. 

		while((bytesRead=fis.read(d2,0,1024))>0)
		{
			metresTotal=total/2;
			// if this chunk of data contains some heights we're interested in...
			if( isInSelectedArea(metresTotal,indexW,indexN,indexE,indexS) || 
					isInSelectedArea(metresTotal+(bytesRead/2)-1,indexW,indexN,indexE,indexS))
			{
				int i=metresTotal;
				while(i<metresTotal + bytesRead/2)
				{
					if(isInSelectedArea(i,indexW,indexN,indexE,indexS))
					{
						
						
							ls=(int)d2[(i-metresTotal)*2+leastSignificant] & 0xff;
							ms=(int)d2[(i-metresTotal)*2+mostSignificant] & 0xff;
							rawHeight = ls+ms*256;
							metres[metreCount++]=rawHeight>=0 && rawHeight<9000 ? rawHeight: 0; 
					}
					
					
					i++;
				}
				
				// Dealing with the odd number of bytes problem above.
				// Force another byte out of the stream...
				if(bytesRead%2==1)
				{
					//System.out.println("Read in " + bytesRead+" forcing the API to behave and give an even number...");
					int first = d2[(i-metresTotal)*2];
					//System.out.println("Trying to read in byte");
					int z = fis.read(d2,0,1);
					//System.out.println("Done");
					bytesRead+=z;
					int last = d2[0];
					ls = (leastSignificant==0) ? first:last;
					ms = (leastSignificant==0) ? last:first;
					metres[metreCount++]=ls+ms*256;
					//System.out.println("Read in additional byte");
				}	
				total+=bytesRead;
			}
			
			//System.out.println("Read in:  " + bytesRead+", total=" + total);
		}
		
		return metres;
	}
	
	private boolean isInSelectedArea(int metreIndex,int indexW,int indexN,int indexE,int indexS)
	{
		return (metreIndex/width >= indexN && metreIndex/width <= indexS &&
				metreIndex%width >= indexW && metreIndex%width <= indexE);	
	}
}
