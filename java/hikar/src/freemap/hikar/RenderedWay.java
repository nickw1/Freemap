package freemap.hikar;

import freemap.data.Point;
import freemap.data.Way;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.FloatBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

public class RenderedWay {

	FloatBuffer vertexBuffer;
	ShortBuffer indexBuffer;
	float[] colour;
	static HashMap<String,float[]> colours;
	short[] indices;
	float[] vertices;
	static float[] road, path, bridleway, track, cycleway, byway;
	
	static {
		colours = new HashMap<String,float[]>();
		road = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
		path = new float[] { 0.0f, 1.0f, 0.0f, 1.0f };
		bridleway = new float[] { 0.67f, 0.33f, 0.0f, 1.0f };
		track = new float[] { 1.0f, 0.5f, 0.0f, 1.0f };
		cycleway = new float[] { 0.0f, 0.0f, 1.0f, 1.0f };
		byway = new float[] { 1.0f, 0.0f, 0.0f, 1.0f };
		colours.put("designation:public_footpath", path );
		colours.put("designation:public_bridleway", bridleway );
		colours.put("designation:public_byway", byway);
		colours.put("designation:restricted_byway", byway );
		colours.put("designation:byway_open_to_all_traffic", byway );
		colours.put("highway:footway", path );
		colours.put("highway:path", path );
		colours.put("highway:bridleway", bridleway );
		colours.put("highway:track", track );
		colours.put("highway:cycleway", cycleway );
	}
	
	public RenderedWay(Way w,float width)
	{

		float dx, dy, dxperp=0.0f, dyperp=0.0f, len;
		int nPts = w.nPoints();
		ByteBuffer buf = ByteBuffer.allocateDirect(nPts*6*4);
		buf.order(ByteOrder.nativeOrder());
		vertexBuffer = buf.asFloatBuffer();
		ByteBuffer ibuf = ByteBuffer.allocateDirect((nPts-1)*6*2);
		ibuf.order(ByteOrder.nativeOrder());
		indexBuffer = ibuf.asShortBuffer();
		
		vertices = new float[nPts*6];
		indices = new short[(nPts-1)*6];
		

		for(int i=0; i<nPts-1; i++)
		{
		
		
			dx=(float)(w.getPoint(i+1).x - w.getPoint(i).x);
			dy=(float)(w.getPoint(i+1).y - w.getPoint(i).y);
			len=(float)(w.getPoint(i).distanceTo(w.getPoint(i+1)));
			dxperp = -(dy*(width/2))/len;
			dyperp = (dx*(width/2))/len;
			vertices[i*6] = (float)(w.getPoint(i).x+dxperp);
			vertices[i*6+1] = (float)(w.getPoint(i).y+dyperp);
			vertices[i*6+2] = (float)w.getPoint(i).z;
			vertices[i*6+3] = (float)(w.getPoint(i).x-dxperp);
			vertices[i*6+4] = (float)(w.getPoint(i).y-dyperp);
			vertices[i*6+5] = (float)w.getPoint(i).z;
			
			/*
			for(int j=0; j<6; j++)
				System.out.println("Vertex : " + (i*6+j)+ " position:" +vertices[i*6+j]);
			*/
			
		}
		int k=nPts-1;
		vertices[k*6] = (float)(w.getPoint(k).x+dxperp);
		vertices[k*6+1] = (float)(w.getPoint(k).y+dyperp);
		vertices[k*6+2] = (float)w.getPoint(k).z;
		vertices[k*6+3] = (float)(w.getPoint(k).x-dxperp);
		vertices[k*6+4] = (float)(w.getPoint(k).y-dyperp);
		vertices[k*6+5] = (float)w.getPoint(k).z;
		
		for(int i=0; i<nPts-1; i++)
		{
			indices[i*6] = (short)(i*2);
			indices[i*6+1] = (short)(i*2 + 1);
			indices[i*6+2] = (short)(i*2 + 2);
			indices[i*6+3] = (short)(i*2 + 1);
			indices[i*6+4] = (short)(i*2 + 3);
			indices[i*6+5] = (short)(i*2 + 2);
		}
		
		vertexBuffer.put(vertices);
		indexBuffer.put(indices);
		vertexBuffer.position(0);
		indexBuffer.position(0);
		
		String highway = w.getValue("highway"), designation = w.getValue("designation");
		if(designation!=null && colours.get("designation:"+designation)!=null)
			colour=colours.get("designation:"+designation);
		else //if(highway!=null)
		{
			if(colours.get("highway:"+highway)!=null)
				colour=colours.get("highway:"+highway);
			else
				colour = road;
		}
	}
	
	public void draw(GPUInterface gpuInterface)
	{
	    
		if(colour!=null)
		{
		    gpuInterface.setUniform4fv("uColour", colour);
			gpuInterface.drawBufferedData(vertexBuffer, indexBuffer, 12, "aVertex");
			
			/* commented out already in gles 1.0 version
			gl.glFrontFace(GL10.GL_CCW);
			gl.glEnable(GL10.GL_CULL_FACE);
			gl.glCullFace(GL10.GL_BACK);
			*/
			
			/* old gles 1.0 stuff
			
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glVertexPointer(3,GL10.GL_FLOAT,0,vertexBuffer);
			gl.glDrawElements(GL10.GL_TRIANGLES,indices.length,GL10.GL_UNSIGNED_SHORT,indexBuffer);
			
			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glDisable(GL10.GL_CULL_FACE);
			*/
		}
	}

	Point getAveragePosition()
	{
		Point p=new Point();
		int nPoints=vertexBuffer.limit() / 3;
		for(int i=0; i<vertexBuffer.limit(); i+=3)
		{
			p.x += vertexBuffer.get(i);
			p.y += vertexBuffer.get(i+1);
			
		}
		p.x /= nPoints;
		p.y /= nPoints;
		
		return p;
	}
	
	public double distanceTo(Point p)
	{
		return getAveragePosition().distanceTo(p);
	}
}
