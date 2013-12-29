package freemap.texturetest;

import android.app.Fragment;
import android.app.Activity;

import android.os.Bundle;

import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;




public class ViewFragment extends Fragment 
    
{

    
    OpenGLView glView;
    //VideoSurfaceView glView;
    
    
   
    
    
    public ViewFragment()
    {
       
        setRetainInstance(true);
        
    }
    
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        
        // We don't do any reprojection on RenderedWays if the display projection and tiling projection
        // are the same
        glView = new OpenGLView(activity);
       //glView = new VideoSurfaceView(activity);
        
        
    }
    
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
 
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState)
    {
        return glView;
    }
    
    public void onResume() {
        super.onResume();
        
        glView.getRenderer().onResume();
       
    }

    public void onPause() {
        super.onPause();
        
        glView.getRenderer().onPause();
    }

    public void onDetach() {
        super.onDetach();
    }    
}
