package freemap.hikar;

import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.hardware.Camera;
import android.content.Context;
import java.io.IOException;
import android.util.Log;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

	Camera camera;
	int previewWidth, previewHeight;
	boolean ready, firstTime;
	
	public CameraView(Context ctx)
	{
		super(ctx);
		previewWidth=240;
		previewHeight=160;
		camera = Camera.open();
		firstTime=true;
		try
		{
			camera.setPreviewDisplay(this.getHolder());
			this.getHolder().addCallback(this);
		    this.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // will not work without this
		}
		catch(IOException e)
		{
			Log.e("OpenTrail",e.toString());
			camera=null;
		}
		
	}
	
	public void surfaceCreated(SurfaceHolder holder)
	{
		
	}
	
	public void surfaceChanged(SurfaceHolder holder,int format, int w, int h)
	{
		if(camera!=null)
		{
			Camera.Parameters p= camera.getParameters();
			p.setPreviewSize(previewWidth,previewHeight);
			camera.setParameters(p);
			camera.startPreview();
			ready=true;
			previewWidth=w;
			previewHeight=h;
			Log.d("OpenTrail","Camera Parameters: " + p.flatten());
			System.out.println("Camera Parameters: " + p.flatten());
			try
			{
				camera.setPreviewDisplay(this.getHolder());
			}
			catch(IOException e)
			{
				Log.e("OpenTrail","Error setting preview display: " + e);
			}
		}
	}
	
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		if(camera!=null)
		{
		 	camera.stopPreview();
			camera=null;
		}
	}
	 public boolean isReadyFirstTime()
	 {
		 if(ready==true)
	    {
	    	if(firstTime==true)
	    	{
	    		firstTime=false;
	    		return true;
	    	}
	    }
	    return false;
	}
	    
	public float getAspectRatio()
	{
		return (float)previewWidth / (float)previewHeight;
	}
	    
	public float getHFOV()
	{
		return 40.0f; // in field estimation - htc hero
	}
}
