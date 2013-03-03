package freemap.hikar;

import android.hardware.Camera;
import android.graphics.SurfaceTexture;
import java.io.IOException;

public class CameraCapturer {

    Camera camera;
   
    public CameraCapturer()
    {
        
    }
    
    public void openCamera()
    {
        try
        {
            camera = Camera.open();
        }
        catch (Exception e) { } 
    }
    
    public void startPreview(SurfaceTexture surfaceTexture) throws IOException
    {
        camera.setPreviewTexture(surfaceTexture);
        camera.startPreview();
    }
    
    public void stopPreview()
    {
        camera.stopPreview();
    }
    
    public void releaseCamera()
    {
        if(camera!=null)
        {
            camera.release();
            camera=null;
        }
    }  
}
