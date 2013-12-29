package freemap.texturetest;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.content.Context;

import android.opengl.GLES20;
import android.graphics.SurfaceTexture;


import java.io.IOException;
import android.util.Log;



import android.opengl.GLES11Ext;

public class OpenGLView extends GLSurfaceView  {
   
   DataRenderer renderer;
  
   
    
    class DataRenderer implements GLSurfaceView.Renderer {
        
        float hFov;
        float[] modelviewMtx, perspectiveMtx;
        float[] texMtx;
        boolean updateSurface;
        
        GLRect  cameraRect;
      
        
        GPUInterface textureInterface;
        
        int textureId;
        SurfaceTexture cameraFeed;
        CameraCapturer cameraCapturer;
        
        float nearPlane = 2.0f, farPlane = 3000.0f;
        
        
        public DataRenderer()
        {
            hFov = 40.0f;
           
            
            
            cameraRect = new GLRect(new float[] { -1.0f, 1.0f, 0.0f, 
                                                  -1.0f, -1.0f, 0.0f,
                                                  1.0f, -1.0f, 0.0f,
                                                  1.0f, 1.0f, 0.0f } , null);
      
            
            
            modelviewMtx = new float[16];
            perspectiveMtx = new float[16];
            Matrix.setIdentityM(modelviewMtx, 0);
            Matrix.setIdentityM(perspectiveMtx, 0);
            
            texMtx = new float[16];
            Matrix.setIdentityM(texMtx, 0);
            
           
        }
        
        public void setUpdateSurface(boolean u)
        {
            updateSurface = u;
        }
        
        public void onSurfaceCreated(GL10 unused,EGLConfig config)
        {
            GLES20.glClearColor(0.0f,0.0f,0.3f,0.0f);
            GLES20.glClearDepthf(1.0f);
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glDepthFunc(GLES20.GL_LEQUAL);
            
            // http://stackoverflow.com/questions/6414003/using-surfacetexture-in-android
            //final int GL_TEXTURE_EXTERNAL_OES = 0x8d65; not needed for api level 15
            int[] textureId = new int[1];
            GLES20.glGenTextures(1, textureId, 0);
            
            if(textureId[0] != 0)
            {
                GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId[0]);
                
                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
               
                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                
              

            
                
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
                
                // from example on stackoverflow - gave same result
                //http://stackoverflow.com/questions/19844699/capture-frames-of-video-while-playing-over-opengls-surface-view
               /*
                final String mVertexShader =
                        "uniform mat4 uMVPMatrix;\n" +
                        "uniform mat4 uSTMatrix;\n" +
                        "attribute vec4 aVertex;\n" +
                   //     "attribute vec4 aTextureCoord;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "void main() {\n" +
                        "  gl_Position = uMVPMatrix * aVertex;\n" +
                        "  vTextureCoord = (uSTMatrix * vec4(0.5*(1.0 + aVertex.x), -0.5*(1.0+aVertex.y), 0, 0)).xy;\n" +
                        "}\n";
                final String mFragmentShader =
                        "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform samplerExternalOES uTexture;\n" +
                        "void main() {\n" +
                        "  gl_FragColor = texture2D(uTexture, vTextureCoord);\n" +
                        "}\n";
                 */
                textureInterface = new GPUInterface(texVertexShader, texFragmentShader);
                
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                
                GLES20.glBindTexture(android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId[0]);
                
                
                textureInterface.setUniform1i("uTexture", 0); // this is the on-gpu texture register not the texture id
                
               
                cameraFeed = new SurfaceTexture(textureId[0]);
                
               
                cameraCapturer = new CameraCapturer(this);
                
                
                
                textureInterface.sendMatrix(modelviewMtx, "uMVPMatrix");
               
               
                textureInterface.sendMatrix(texMtx, "uSTMatrix");
                            
               onResume();
                
            }
        }
        
        public void onDrawFrame(GL10 unused)
        {
            if(!updateSurface) return;
            
            Matrix.setIdentityM(perspectiveMtx, 0);
            float aspectRatio = (float)getWidth()/(float)getHeight();
            Matrix.perspectiveM(perspectiveMtx, 0, hFov/aspectRatio, aspectRatio, 
                                nearPlane, farPlane);
            
            if(cameraFeed!=null)
                cameraFeed.getTransformMatrix(texMtx);
            
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);     
            
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            if(cameraCapturer.isActive())
            {
                cameraFeed.updateTexImage();
                float[] tm = new float[16];
                cameraFeed.getTransformMatrix(tm);
            
                textureInterface.select();
                cameraRect.draw(textureInterface);
                errcheck("draw camera rect");
            }
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            
            
        }
        
        public void onSurfaceChanged(GL10 unused, int width, int height)
        {
            // need to get the camera parameters
            GLES20.glViewport(0, 0, width, height);
            float aspectRatio = (float)width/(float)height;
            Matrix.setIdentityM(perspectiveMtx, 0);
            Matrix.perspectiveM(perspectiveMtx, 0, hFov/aspectRatio, aspectRatio, nearPlane, farPlane);
        }
        
        public void onPause()
        {
            cameraCapturer.releaseCamera();
        }
        
        public void onResume()
        {
            if(cameraCapturer!=null && !cameraCapturer.cameraSetup())
            {
                cameraCapturer.openCamera();
                float camHfov = cameraCapturer.getHFOV();
                if(camHfov>0.0f)
                    hFov = camHfov;
                try
                {
                    cameraCapturer.setTexture(cameraFeed);
                    cameraCapturer.startPreview();
                }
                catch(IOException e)
                {
                    Log.e("hikar","Error getting camera preview: " + e);
                    cameraCapturer.releaseCamera();
                }
            }
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
        
        
        
       
        
        public float[] getModelviewMtx()
        {
            return modelviewMtx;
        }
        
        public float[] getPerspectiveMtx()
        {
            return perspectiveMtx;
        }
        
       
        
        public void setCameraFrame(SurfaceTexture st)
        {
            cameraFeed = st;
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
    
    public static void errcheck(String place)
    {
        int e = GLES20.glGetError();
        if(e!=0)
            Log.e("hikar", "GL ERROR: at place " + place + " " + e);
    }
}