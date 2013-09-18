package freemap.hikar;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.content.Context;
import android.os.AsyncTask;
import android.opengl.GLES20;
import android.graphics.SurfaceTexture;


import java.io.IOException;
import android.util.Log;

import java.util.HashMap;

import freemap.data.Way;
import freemap.data.Point;
import freemap.datasource.FreemapDataset;
import freemap.jdem.DEM;

public class OpenGLView extends GLSurfaceView  {
   
   DataRenderer renderer;
    
   public interface RenderedWayVisitor
   {
       public void visit(RenderedWay rw);
   }
    
    class DataRenderer implements GLSurfaceView.Renderer, FreemapDataset.WayVisitor {
        
        float hFov;
        float[] modelviewMtx, perspectiveMtx;
        HashMap<Long,RenderedWay> renderedWays;
        HashMap<String,RenderedDEM> renderedDEMs;
        boolean calibrate;
        GLRect calibrateRect, cameraRect;
        float xDisp, yDisp, zDisp, height;
        GPUInterface gpuInterface, textureInterface;
        float[] texcoords;
        int textureId;
        SurfaceTexture cameraFeed;
        CameraCapturer cameraCapturer;
        
        public DataRenderer()
        {
            hFov = 40.0f;
            renderedWays = new HashMap<Long,RenderedWay>();
            renderedDEMs = new HashMap<String,RenderedDEM>();
            
            zDisp = 1.4f; 
            
                
            // calibrate with an object 50cm long and 50cm away
            float zDist = 0.5f, xLength = 0.5f;
            
            calibrateRect = new GLRect(new float[]{xLength/2, 0.0f, -zDist, -xLength/2, 0.0f, -zDist,
                                                    -xLength/2, 0.05f, -zDist, xLength/2, 0.05f, -zDist}, 
                                    new float[]{1.0f,1.0f,1.0f,1.0f});
            
            
            
            cameraRect = new GLRect(new float[] { -1.0f, 1.0f, 0.0f, 
                                                  -1.0f, -1.0f, 0.0f,
                                                  1.0f, -1.0f, 0.0f,
                                                  1.0f, 1.0f, 0.0f } , null);
      
            
            modelviewMtx = new float[16];
            perspectiveMtx = new float[16];
            Matrix.setIdentityM(modelviewMtx, 0);
            Matrix.setIdentityM(perspectiveMtx, 0);
        }
        
        public void onSurfaceCreated(GL10 unused,EGLConfig config)
        {
            GLES20.glClearColor(0.0f,0.0f,0.3f,0.0f);
            GLES20.glClearDepthf(1.0f);
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
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
                    "gl_FragColor = uColour;\n" +
                    "}\n";
            gpuInterface = new GPUInterface(vertexShader, fragmentShader);
            
            // http://stackoverflow.com/questions/6414003/using-surfacetexture-in-android
            final int GL_TEXTURE_EXTERNAL_OES = 0x8d65;
            int[] textureId = new int[1];
            GLES20.glGenTextures(1, textureId, 0);
            if(textureId[0] != 0)
            {
                GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId[0]);
                GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
                GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            
                // Must negate y when calculating texcoords from vertex coords as bitmap image data assumes
                // y increases downwards
                final String texVertexShader =
                   "attribute vec4 aVertex;\n" +
                   "varying vec2 vTextureValue;\n" +
                   "void main (void)\n" +
                   "{\n" +
                   "gl_Position = aVertex;\n" +
                   "vTextureValue = vec2(0.5*(1.0 + aVertex.x), -0.5*(1.0+aVertex.y));\n" +
                   "}\n",
                   texFragmentShader =
                   "#extension GL_OES_EGL_image_external: require\n" +
                   "precision mediump float;\n" +
                   "varying vec2 vTextureValue;\n" +
                   "uniform samplerExternalOES uTexture;\n" +
                   "void main(void)\n" +
                   "{\n" +
                   "gl_FragColor = texture2D(uTexture,vTextureValue);\n" +
                   "}\n";
                textureInterface = new GPUInterface(texVertexShader, texFragmentShader);
                GPUInterface.setupTexture(textureId[0]);
                textureInterface.setUniform1i("uTexture", 0); // this is the on-gpu texture register not the texture id
                cameraFeed = new SurfaceTexture(textureId[0]);
                cameraCapturer = new CameraCapturer(this);
                onResume();
            }
        }
        
        public void onDrawFrame(GL10 unused)
        {
            
            Matrix.setIdentityM(perspectiveMtx, 0);
            float aspectRatio = (float)getWidth()/(float)getHeight();
            Matrix.perspectiveM(perspectiveMtx, 0, hFov/aspectRatio, aspectRatio, 0.1f, 3000.0f);
       
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);     
            
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            if(cameraCapturer.isActive())
            {
                cameraFeed.updateTexImage();
                float[] tm = new float[16];
                cameraFeed.getTransformMatrix(tm);
            
                textureInterface.select();
                cameraRect.draw(textureInterface);
            }
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            
            gpuInterface.select();
            
            
            if(calibrate)
            {
                Matrix.setIdentityM(modelviewMtx, 0);
                gpuInterface.sendMatrix(modelviewMtx, "uMvMtx");
                gpuInterface.sendMatrix(perspectiveMtx, "uPerspMtx");
                calibrateRect.draw(gpuInterface); 
            }
            else
            {
               
                
                synchronized(renderedDEMs)
                {
                    for (HashMap.Entry<String,RenderedDEM> d: renderedDEMs.entrySet())
                        d.getValue().render(gpuInterface);
                }
                
                
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
                        RenderedWay rWay = null;
                        for(HashMap.Entry<Long, RenderedWay> entry: renderedWays.entrySet())
                        {          
                            rWay = entry.getValue();
                            if(rWay.isDisplayed() && rWay.distanceTo(p) <= 3000.0f)
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
            Matrix.perspectiveM(perspectiveMtx, 0, hFov/aspectRatio, aspectRatio, 0.1f, 3000.0f);
        }
        
        public void onPause()
        {
            cameraCapturer.releaseCamera();
        }
        
        public void onResume()
        {
            if(cameraCapturer!=null)
            {
                cameraCapturer.openCamera();
                float camHfov = cameraCapturer.getHFOV();
                if(camHfov>0.0f)
                    hFov = camHfov;
                try
                {
                    cameraCapturer.startPreview(cameraFeed);
                }
                catch(IOException e)
                {
                    Log.e("hikar","Error getting camera preview: " + e);
                    cameraCapturer.releaseCamera();
                }
            }
        }
        
        public void setOrientMtx(float[] orientMtx)
        {
            this.modelviewMtx = orientMtx.clone();
        }
        
        public void setRenderData(DownloadDataTask.ReceivedData data)
        {
            AsyncTask<DownloadDataTask.ReceivedData,Void,Boolean> setRenderDataTask = 
                    new AsyncTask<DownloadDataTask.ReceivedData,Void,Boolean> ()
            {
                protected Boolean doInBackground(DownloadDataTask.ReceivedData... d)
                {
                    Log.d("hikar","Setting render data");
                    if(d[0].dem != null)
                    {
                        Point p = d[0].dem.getBottomLeft();
                        if(renderedDEMs==null) // do not clear out when we enter a new tile!
                            renderedDEMs = new HashMap<String,RenderedDEM> ();
                        String key = (int)p.x+":"+(int)p.y;
                        Log.d("hikar", "Found a DEM to be rendered, key=" + key);
                        if(renderedDEMs.get(key)==null)
                            renderedDEMs.put(key, new RenderedDEM(d[0].dem));
                    }
                    
                    if(renderedWays==null) // do not clear out when we enter a new tile!
                        renderedWays = new HashMap<Long,RenderedWay> ();
                    d[0].osm.operateOnWays(DataRenderer.this); 
                    return true;
                }
            };
            setRenderDataTask.execute(data);  
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
                if(renderedWays.get(w.getId())==null)
                    renderedWays.put(w.getId(), new RenderedWay(w,2.0f));
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
        
        public float getHFOV()
        {
            return hFov;
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
        
        public void setCameraFrame(SurfaceTexture st)
        {
            cameraFeed = st;
        }
        
        public void setCameraHeight(float cameraHeight)
        {
            zDisp = cameraHeight;
        }
        
        public void operateOnRenderedWays(OpenGLView.RenderedWayVisitor visitor)
        { 
            for(HashMap.Entry<Long,RenderedWay> entry : renderedWays.entrySet())
                visitor.visit(entry.getValue());
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