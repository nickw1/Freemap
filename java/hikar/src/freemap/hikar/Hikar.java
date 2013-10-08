package freemap.hikar;

import freemap.andromaps.DialogUtils;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;
import android.view.MenuItem;
import android.content.Intent;
import android.content.SharedPreferences;


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
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
        boolean retcode=false;
        
        switch(item.getItemId())
        {    
            case R.id.menu_calibrate:
                viewFragment.toggleCalibrate();
                item.setTitle(item.getTitle().equals("Calibrate") ? "Stop calibrating": "Calibrate");
                retcode=true;
                break;
                
            case R.id.menu_settings:
                Intent i = new Intent(this,Preferences.class);
                startActivity(i);
                break;
            
            case R.id.menu_start:
                boolean start = item.getTitle().equals("Start");
                viewFragment.setActivate(start);
                item.setTitle(start ? "Stop" : "Start");
                break;
                
        }
        return retcode;
    }
    
    public void onPause()
    {
        super.onPause();
    }
    
    public void onStart()
    {
        super.onStart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        float cameraHeight = Float.parseFloat(prefs.getString("prefCameraHeight","1.4"));
        viewFragment.setCameraHeight(cameraHeight);
        String prefSrtmUrl=prefs.getString("prefSrtmUrl","http://www.free-map.org.uk/ws/"), 
                prefLfpUrl=prefs.getString("prefLfpUrl", "http://www.free-map.org.uk/downloads/lfp/"), 
                prefOsmUrl=prefs.getString("prefOsmUrl", "http://www.free-map.org.uk/0.6/ws/");
        boolean urlchange = viewFragment.setDataUrls(prefLfpUrl, prefSrtmUrl, prefOsmUrl);
        int prefDEM = Integer.valueOf(prefs.getString("prefDEM","0"));
        viewFragment.setDEM(prefDEM);
        String prefDisplayProjectionID = "epsg:" + prefs.getString("prefDisplayProjection", "27700");
        if(!viewFragment.setDisplayProjectionID(prefDisplayProjectionID))
            DialogUtils.showDialog(this, "Invalid projection " + prefDisplayProjectionID);
    }
    
    public HUD getHUD()
    {
        return hud;
    }
}
