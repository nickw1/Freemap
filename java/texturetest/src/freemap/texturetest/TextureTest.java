package freemap.texturetest;


import android.os.Bundle;

import android.app.Activity;



public class TextureTest extends Activity 
{

    ViewFragment viewFragment;
   
   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      
        setContentView(R.layout.activity_main);
       
        viewFragment = (ViewFragment)getFragmentManager().findFragmentById(R.id.view_fragment);    
    }
    
    public void onPause()
    {
        super.onPause();
    }
    
    public void onStart()
    {
        super.onStart();
        
    }   
}
