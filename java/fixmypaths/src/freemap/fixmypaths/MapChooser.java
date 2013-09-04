package freemap.fixmypaths;

import android.app.ListActivity;
import android.os.Bundle;
import android.content.Intent;
import java.io.File;
import java.io.FilenameFilter;

import android.os.Environment;
import android.view.View;
import android.widget.ListView;
import android.widget.ArrayAdapter;


public class MapChooser extends ListActivity {

    ArrayAdapter<String> adapter;
    String[] mapFiles;
    
    public void onCreate(Bundle savedInstanceState)
    {
    
        super.onCreate(savedInstanceState);
        
        File dir = new File (Environment.getExternalStorageDirectory().getAbsolutePath()+"/openhants");
        File[] files = dir.listFiles(
                    new FilenameFilter()
                    {
                        public boolean accept(File dir, String filename)
                        {
                            return filename.endsWith(".map");
                        }
                    }
                );
        
        mapFiles = new String[files.length];
        for(int i=0; i<files.length; i++)
            mapFiles[i] = files[i].getName();
        
       
        adapter = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, mapFiles);
        this.setListAdapter(adapter);
    }

    
    public void onListItemClick(ListView lv, View view, int index, long id)
    {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("freemap.fixmypaths.county", mapFiles[index].replace(".map",""));
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
