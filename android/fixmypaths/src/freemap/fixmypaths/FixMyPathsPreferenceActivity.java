package freemap.fixmypaths;


import android.preference.PreferenceActivity;
import android.os.Bundle;

public class FixMyPathsPreferenceActivity extends PreferenceActivity {
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

}
