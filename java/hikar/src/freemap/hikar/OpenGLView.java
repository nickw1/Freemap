package freemap.hikar;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.content.Context;

import java.util.ArrayList;

import freemap.data.Way;
import freemap.data.Point;
import freemap.datasource.FreemapDataset;

public class OpenGLView extends GLSurfaceView {
    
   DataRenderer renderer;
    
  
    
    class DataRenderer implements GLSurfaceView.Renderer, FreemapDataset.WayVisitor {
        
        float hFov;
        float[] orientMtx;
        ArrayList<RenderedWay> renderedWays;
        boolean calibrate;
        GLRect calibrateRect;
        float xDisp, yDisp, zDisp, height;
        
        public DataRenderer()
        {
            hFov = 40.0f;
            renderedWays = new ArrayList<RenderedWay>();
            
            zDisp = 2.0f;
            height = 0.0f;
                
            // calibrate with an object 50cm long and 1m away
            calibrateRect = new GLRect(new float[]{0.25f,0.0f,-1.0f,-0.25f,0.0f,-1.0f,-0.25f,0.05f,-1.0f,0.25f,0.05f,-1.0f}, 
                                    new float[]{1.0f,1.0f,1.0f,1.0f});
        }
        
        public void onSurfaceCreated(GL10 gl,EGLConfig config)
        {
            gl.glClearColor(0.0f,0.0f,0.5f,0.0f);
            gl.glClearDepthf(1.0f);
            gl.glEnable(GL10.GL_DEPTH_TEST);
            gl.glDepthFunc(GL10.GL_LEQUAL);
        }
        
        public void onDrawFrame(GL10 gl)
        {
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            float aspectRatio = (float)getWidth()/(float)getHeight();
            GLU.gluPerspective(gl, hFov/aspectRatio, aspectRatio, 0.1f, 1000.0f);
        
        
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            
            gl.glTranslatef(0.0f,0.0f,-zDisp);
            calibrateRect.draw(gl);
            
            /*
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
            }
            */
        }
        
        public void onSurfaceChanged(GL10 gl, int width, int height)
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
        
        public void setOrientMtx(float[] orientMtx)
        {
            this.orientMtx = orientMtx;
        }
        
        public void setRenderData(FreemapDataset d)
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
        
        public void setHFOV(float hFov)
        {
            this.hFov = hFov;
        }
        
        public void changeHFOV(float amount)
        {
            this.hFov += amount;
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
        
    }
    
    public OpenGLView(Context ctx)
    {
        super(ctx);
        renderer=new DataRenderer();
        setRenderer(renderer);
    }
    
    public DataRenderer getRenderer()
    {
        return renderer;
    }
}
