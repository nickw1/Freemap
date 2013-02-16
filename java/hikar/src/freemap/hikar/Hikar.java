package freemap.hikar;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class Hikar extends Activity {
    OpenGLView glView;
    OsmDemIntegrator integrator;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glView = new OpenGLView(this);
        integrator = new OsmDemIntegrator("epsg:27700");
        setContentView(glView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

}
