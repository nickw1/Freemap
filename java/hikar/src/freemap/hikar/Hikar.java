package freemap.hikar;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;


public class Hikar extends Activity 
{
    LocationProcessor locationProcessor;
    ViewFragment viewFragment;
   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewFragment = (ViewFragment)getFragmentManager().findFragmentById(R.id.view_fragment);        
    }

   
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
