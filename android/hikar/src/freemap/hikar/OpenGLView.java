package freemap.hikar;

import android.graphics.PixelFormat;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.opengl.GLSurfaceView.Renderer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.FloatBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import java.util.ArrayList;

import android.opengl.GLSurfaceView;
import android.util.Log;

import android.content.Context;

import freemap.datasource.OSMRenderData;
import freemap.data.Way;
import freemap.data.Point;

import android.opengl.Matrix;

public class OpenGLView extends GLSurfaceView implements Renderer, OSMRenderData.WayVisitor {
	// http://blog.jayway.com/2009/12/04/
	// opengl-es-tutorial-for-android-%E2%80%93-part-ii-building-a-polygon/
	
	float[] orientMtx;
	float hFov;
	float xDisp, yDisp, zDisp, height;
	boolean hfovAltered, calibrate;
	
	ArrayList<RenderedWay> renderedWays;
	
	GLRect calibrateRect;
	
	ByteBuffer fullTexture;
	int texID,texSize;
	float[] square;
	FloatBuffer squareBuffer;
	byte[] feedTexture;
	FloatBuffer texCoordsBuffer, squareCoordsBuffer;
	
	int previewWidth, previewHeight;

	
	public OpenGLView(Context ctx)
	{
		super(ctx);
		this.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // needed for transparency
	        
	    this.setRenderer(this);
	    this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
	        
	    hFov = 40.0f; // htc hero - in field estimation 
	    renderedWays = new ArrayList<RenderedWay>();
	        
	    zDisp = 2.0f;
	    height = 0.0f;
	        
	    // calibrate with an object 50cm long and 1m away
	    calibrateRect = new GLRect(new float[]{0.25f,0.0f,-1.0f,-0.25f,0.0f,-1.0f,-0.25f,0.05f,-1.0f,0.25f,0.05f,-1.0f}, 
	        					new float[]{1.0f,1.0f,1.0f,1.0f});
	        
	    texSize = 512;

	}
	
	public void setHFOV(float hFov)
	{
		this.hFov = hFov;
	}
	
	public void changeHFOV(float amount)
	{
		this.hFov += amount;
		hfovAltered=true;
	}

	
	public void onSurfaceCreated(GL10 gl,EGLConfig config)
	{
		gl.glClearColor(0.0f,0.0f,0.0f,0.0f);
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		
		/* texture stuff commented out for the moment
		gl.glEnable(GL10.GL_TEXTURE_2D);
		initTexture(gl);
		setSquareCoords();
		*/
	}
	
	public void setCalibrate(boolean cal)
	{
		calibrate=cal;
	}
	
	public boolean getCalibrate()
	{
		return calibrate;
	}
	
	public void toggleCalibrate()
	{
		calibrate = !calibrate;
	}

	public void onDrawFrame(GL10 gl)
	{
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		float aspectRatio = (float)getWidth()/(float)getHeight();
		GLU.gluPerspective(gl, hFov/aspectRatio, aspectRatio, 0.1f, 1000.0f);
		hfovAltered=false;
	
	
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		
		/* commented out for the moment
		// Draw the camera feed as a texture
		if(feedTexture!=null)
		{
			gl.glEnable(GL10.GL_TEXTURE_2D);
		
			doTexture(gl);
			drawTexture(gl);
			
			gl.glDisable(GL10.GL_TEXTURE_2D);
		}
		*/
		
		// End draw camera feed
	
		
		if(calibrate)
		{
			calibrateRect.draw(gl);
		}
		else
		{
		
	
		
			Point p = new Point((double)xDisp,(double)yDisp,(double)height);
			if(this.orientMtx!=null)
				gl.glMultMatrixf(orientMtx,0);
			
		
			if(renderedWays.size()>0)
			{
				//Log.d("OpenTrail","Have some rendered ways");
				gl.glTranslatef(-xDisp,-yDisp,-height-zDisp);
				synchronized(renderedWays)
				{
					for(RenderedWay rWay: renderedWays)
					{
						//Log.d("OpenTrail","p=" + p.x+" "+p.y+" "+p.z+" average P="+a.x+" "+a.y+" " +a.z+" distance=" + rWay.distanceTo(p));
						if(rWay.distanceTo(p) <= 1000.0f)
						{
							//Log.d("OpenTrail","Drawing a rendered way");
							rWay.draw(gl);
						}		
					}
				}
			}
			else
			{
			 
				gl.glTranslatef(0.0f,0.0f,-zDisp);
				Way w=new Way();
				w.addPoint(0.0f, 0.0f, 0.0f);
				w.addPoint(-10.0f, 0.0f, 0.0f);
				w.addPoint(-10.0f, 10.0f, 0.0f);
				w.addPoint(-20.0f, 10.0f, 0.0f);
				w.addPoint(-20.0f, 20.0f, 0.0f);
				w.addPoint(-30.0f, 20.0f, 0.0f);
				w.addPoint(-30.0f, 30.0f, 0.0f);
				
				RenderedWay rw=new RenderedWay(w,2.0f);
				rw.draw(gl);
				
			}
		}
	}
	
	public void onSurfaceChanged(GL10 gl,int width,int height)
	{
		// need to get the camera parameters
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		float aspectRatio = (float)width/(float)height;
		GLU.gluPerspective(gl, hFov/aspectRatio, aspectRatio, 0.1f, 1000.0f);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
	
	public void setMatrix(float[] mtx)
	{
		this.orientMtx=mtx;
	}
	
	public void setRenderData(OSMRenderData d)
	{
		
		renderedWays = new ArrayList<RenderedWay> ();
		d.operateOnWays(this);
		
	}
	
	public void setCameraLocation(float x,float y)
	{
		xDisp = x;
		yDisp = y;
	}
	
	public void setHeight(float h)
	{
		height=h;
	}
	
	public void visit(Way w)
	{
		synchronized(renderedWays)
		{
			renderedWays.add(new RenderedWay(w,2.0f));
		}
		//Log.d("OpenTrail","Adding rendered way for way with ID: " + w.getValue("osm_id"));
	}
	
	public void setRotation(float azimuth,float pitch,float roll)
	{
		// The azimuth, pitch, roll from the sensors are based on positive
		// =clockwise (despite what the API docs say!!!), so we rotate by +pitch and +roll.
		Matrix.setIdentityM(orientMtx,0);
		Matrix.rotateM(orientMtx,0,azimuth-90.0f,0.0f,0.0f,1.0f);
		Matrix.rotateM(orientMtx,0,pitch,1.0f,0.0f,0.0f);
		Matrix.rotateM(orientMtx,0,roll,0.0f,1.0f,0.0f);
	}
	
	/* texture stuff commented out for the moment - impossible it seems to direct camera feed onto a texture
	 * without opening a preview
	 
	// from nhenze example
	// the feed will occupy *part* of the texture only
	public void newFrame(byte[] yuvs,int previewWidth,int previewHeight)
	{
		if(feedTexture==null)
			feedTexture = new byte[texSize*texSize];
		if(texCoordsBuffer==null || this.previewWidth!=previewWidth || this.previewHeight != previewHeight)
		{
			this.previewWidth=previewWidth;
			this.previewHeight=previewHeight;
			setTextureCoords(previewWidth,previewHeight);
		}
		int feedCount=0, texCount=0;
		for(int row=0; row<previewHeight; row++)
		{
			System.arraycopy(yuvs,feedCount,feedTexture,texCount,previewWidth);
			feedCount+=previewWidth;
			texCount+=texSize;
		}	
	}

	public void initTexture(GL10 gl)
	{	
		int[] txid=new int[1];
		gl.glGenTextures(1,txid,0);// what does the 0 mean?
		texID = txid[0];
		gl.glBindTexture(GL10.GL_TEXTURE_2D,texID);
	}
	
	public void doTexture(GL10 gl)
	{
		synchronized(this)
		{
			gl.glTexImage2D(GL10.GL_TEXTURE_2D,0,
				GL10.GL_LUMINANCE,texSize,texSize,
				0,GL10.GL_LUMINANCE,GL10.GL_UNSIGNED_BYTE,
				ByteBuffer.wrap(feedTexture));
			gl.glTexParameterf(GL10.GL_TEXTURE_2D,
				GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_LINEAR);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D,
				GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_LINEAR);
		}
	}
	
	public void setTextureCoords(int previewWidth, int previewHeight)
	{
		// texcoords 0,0 top left? appears to be so
		float[] texCoords = new float[] { 0.0f, (float)previewHeight/(float)texSize,
									(float)previewWidth/(float)texSize,(float)previewHeight/(float)texSize,
									0.0f,0.0f,
								(float)previewWidth/(float)texSize, 0.0f
									};
		ByteBuffer b = ByteBuffer.allocateDirect(8*4);
		b.order(ByteOrder.nativeOrder());
		texCoordsBuffer = b.asFloatBuffer();
		texCoordsBuffer.put(texCoords);
		texCoordsBuffer.position(0);
	}
	
	public void setSquareCoords()
	{  
        float[] squareCoords = new float[] { -1.0f,-1.0f,-0.5f,1.0f,-1.0f,-0.5f,-1.0f,1.0f,-0.5f,1.0f,1.0f,-0.5f };
        ByteBuffer b = ByteBuffer.allocateDirect(12*4);
        b.order(ByteOrder.nativeOrder());
        squareCoordsBuffer = b.asFloatBuffer();
        squareCoordsBuffer.put(squareCoords);
        squareCoordsBuffer.position(0);
	}
	
	public void drawTexture(GL10 gl)
	{
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3,GL10.GL_FLOAT,0,squareCoordsBuffer);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glTexCoordPointer(2,GL10.GL_FLOAT,0,texCoordsBuffer);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP,0,4);
	
	}
	*/
}
