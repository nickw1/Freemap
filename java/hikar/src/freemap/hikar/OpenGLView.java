package freemap.hikar;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.content.Context;
import android.os.AsyncTask;
import android.opengl.GLES20;

import java.util.ArrayList;

import freemap.data.Way;
import freemap.data.Point;
import freemap.datasource.FreemapDataset;

public class OpenGLView extends GLSurfaceView {
   
   DataRenderer renderer;
    
  
    
    class DataRenderer implements GLSurfaceView.Renderer, FreemapDataset.WayVisitor {
        
        float hFov;
        float[] modelviewMtx, perspectiveMtx;
        ArrayList<RenderedWay> renderedWays;
        boolean calibrate;
        GLRect calibrateRect;
        float xDisp, yDisp, zDisp, height;
        GPUInterface gpuInterface;
        
        public DataRenderer()
        {
            hFov = 40.0f;
            renderedWays = new ArrayList<RenderedWay>();
            
            zDisp = 2.0f;
            
                
            // calibrate with an object 50cm long and 1m away
            
            calibrateRect = new GLRect(new float[]{0.25f,0.0f,-1.0f,-0.25f,0.0f,-1.0f,-0.25f,0.05f,-1.0f,0.25f,0.05f,-1.0f}, 
                                    new float[]{1.0f,1.0f,1.0f,1.0f});
            
            
            
            modelviewMtx = new float[16];
            perspectiveMtx = new float[16];
            Matrix.setIdentityM(modelviewMtx, 0);
            Matrix.setIdentityM(perspectiveMtx, 0);
        }
        
        public void onSurfaceCreated(GL10 unused,EGLConfig config)
        {
            GLES20.glClearColor(0.0f,0.0f,0.3f,0.0f);
            GLES20.glClearDepthf(1.0f);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glDepthFunc(GLES20.GL_LEQUAL);
            final String vertexShader = 
                    "attribute vec4 aVertex;\n" +
                    "uniform mat4 uPerspMtx, uMvMtx;\n"+
                    "void main(void)\n" +
                    "{\n"+
                    "gl_Position = uPerspMtx * uMvMtx * aVertex;\n" +
                    "}\n",
                    fragmentShader = 
                    "precision mediump float;\n" +
                    "uniform vec4 uColour;\n" + 
                    "void main(void)\n"+
                    "{\n"+
                   // "gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0);\n" +
                    "gl_FragColor = uColour;\n" +
                    "}\n";
            gpuInterface = new GPUInterface(vertexShader, fragmentShader);
        }
        
        public void onDrawFrame(GL10 unused)
        {
            
            Matrix.setIdentityM(perspectiveMtx, 0);
            float aspectRatio = (float)getWidth()/(float)getHeight();
           Matrix.perspectiveM(perspectiveMtx, 0, hFov/aspectRatio, aspectRatio, 0.1f, 1000.0f);
       
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);     
            
            
         
            
            //calibrate = true;
            
            if(calibrate)
            {
                Matrix.setIdentityM(modelviewMtx, 0);
                gpuInterface.sendMatrix(modelviewMtx, "uMvMtx");
                gpuInterface.sendMatrix(perspectiveMtx, "uPerspMtx");
                calibrateRect.draw(gpuInterface); 
            }
            else
            {
                if(renderedWays.size()>0)
                { 
                    //Matrix.translateM(modelviewMtx, 0, 0, 0, -zDisp); // needed????
                    Point p = new Point((double)xDisp,(double)yDisp,(double)height);
                    
                    // NOTE! The result matrix must not refer to the same array in memory as either
                    // input matrix! Otherwise you get strange results.
                    /* we now start with the orientation matrix from the sensors so no need for this anyway
                    if(this.modelviewMtx!=null) 
                        Matrix.multiplyMM(modelviewMtx, 0, modelviewMtx, 0, modelviewMtx, 0);
                    */
                    
                    Matrix.translateM(modelviewMtx, 0, -xDisp, -yDisp, -height-zDisp);
                   
                    gpuInterface.sendMatrix(modelviewMtx, "uMvMtx");
                    gpuInterface.sendMatrix(perspectiveMtx, "uPerspMtx");
                    
                    synchronized(renderedWays)
                    {
                        for(RenderedWay rWay: renderedWays)
                        {                 
                            if(rWay.distanceTo(p) <= 1000.0f)
                            {
                                rWay.draw(gpuInterface); 
                            }       
                        }
                    }
                }
            }
        }
        
        public void onSurfaceChanged(GL10 unused, int width, int height)
        {
            // need to get the camera parameters
            GLES20.glViewport(0, 0, width, height);
            float aspectRatio = (float)width/(float)height;
            Matrix.setIdentityM(perspectiveMtx, 0);
            Matrix.perspectiveM(perspectiveMtx, 0, hFov/aspectRatio, aspectRatio, 0.1f, 1000.0f);
        }
        
       
        
        public void setOrientMtx(float[] orientMtx)
        {
            this.modelviewMtx = orientMtx.clone();
        }
        
        public void setRenderData(FreemapDataset d)
        {
            AsyncTask<FreemapDataset,Void,Boolean> setRenderDataTask = new AsyncTask<FreemapDataset,Void,Boolean> ()
            {
                protected Boolean doInBackground(FreemapDataset... d)
                {
                    renderedWays = new ArrayList<RenderedWay> ();
                    d[0].operateOnWays(DataRenderer.this); 
                    return true;
                }
            };
            setRenderDataTask.execute(d);  
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
            //Log.d("hikar","Adding rendered way for way with ID: " + w.getValue("osm_id"));
        }
        
        // setRotation() removed as duplicates setOrientMtx()
        
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
        
        public float[] getModelviewMtx()
        {
            return modelviewMtx;
        }
        
        public float[] getPerspectiveMtx()
        {
            return perspectiveMtx;
        }
        
        public GPUInterface getGPUInterface()
        {
            return gpuInterface;
        }
    }
    
    public OpenGLView(Context ctx)
    {
        super(ctx);
        setEGLContextClientVersion(2);
        renderer=new DataRenderer();
        setRenderer(renderer);
    }
    
    public DataRenderer getRenderer()
    {
        return renderer;
    }    
}
