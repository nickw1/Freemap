package freemap.hikar;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;


public class Hikar extends Activity 
{
    LocationProcessor locationProcessor;
    ViewFragment viewFragment;
    HUD hud;
   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hud=new HUD(this);
        setContentView(R.layout.activity_main);
        addContentView(hud, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        viewFragment = (ViewFragment)getFragmentManager().findFragmentById(R.id.view_fragment);        
    }

   
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public HUD getHUD()
    {
        return hud;
    }
}
