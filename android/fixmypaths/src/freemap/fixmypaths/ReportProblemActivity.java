package freemap.fixmypaths;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;



import java.util.ArrayList;


import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.app.AlertDialog;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.DialogInterface;



public class ReportProblemActivity extends Activity{

	
	ReportProblemTask task;
	Bundle rowDetails;
	
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		rowDetails  = getIntent().getExtras();
		
		
		setContentView(R.layout.reportproblem);
		Button ok = (Button)findViewById(R.id.buttonReportProblemOK),
			cancel = (Button)findViewById(R.id.buttonReportProblemCancel);
		
		((EditText)findViewById(R.id.editTextPathID)).setText(rowDetails.getString("parish") + 
						" " + rowDetails.getString("routeno") + " (" +
						rowDetails.getString("row_type") + ")");
		
		ok.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if(rowDetails!=null)
				{
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					String email = prefs.getString("email", "");
					String name=prefs.getString("name","");
					String problemDetails = ((EditText)findViewById(R.id.editTextReportProblem)).getText().toString();
					String problemType = (String) ((Spinner)findViewById(R.id.spinnerProblemType)).getSelectedItem();
					// send to the server
					
					//countryside@hants.gov.uk
					ArrayList<NameValuePair> postData;
					postData = new ArrayList<NameValuePair>();
					postData.add(new BasicNameValuePair("action", "addProblem"));
					postData.add(new BasicNameValuePair("id",rowDetails.getString("gid")));
					postData.add(new BasicNameValuePair("reporter_email",email));
					postData.add(new BasicNameValuePair("reporter_name",name));
					postData.add(new BasicNameValuePair("category",problemType)); // cboCategory
					postData.add(new BasicNameValuePair("problem",problemDetails)); // txtProblem
					postData.add(new BasicNameValuePair("y",String.valueOf(rowDetails.getDouble("lat"))));
					postData.add(new BasicNameValuePair("x",String.valueOf(rowDetails.getDouble("lon"))));
					postData.add(new BasicNameValuePair("inProj", "4326"));
					
					/// ... asynctask
					
					
					task = new ReportProblemTask (ReportProblemActivity.this);
					task.execute(postData);
				
					
				}
			}
		}
		
		
		
		);
		
		cancel.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		
		Spinner spinnerProblemType = (Spinner)findViewById(R.id.spinnerProblemType);
		
		String[] problemTypes = { 
				"Structure - Stile",
				"Structure - Gate",
				"Structure - Squeeze Gap",
				"Structure - Steps",
				"Structure - Bridge",
				"Structure - Boardwalk",
				"Obstruction - Fence",
				"Obstruction - Locked Gate",
				"Obstruction - Seasonal Growth",
				"Obstruction - Fallen Tree",
				"Obstruction - Headland Path",
				"Obstruction - Crossfield Path",
				"Sign Posting - Fingerpost",
				"Sign Posting - Waymarking",
				"Surface",
				"Other"
		};
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
		for(int i=0; i<problemTypes.length; i++)
			adapter.add(problemTypes[i]);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerProblemType.setAdapter(adapter);
		
		ReportProblemTask lastTask = (ReportProblemTask)getLastNonConfigurationInstance();
		if(lastTask!=null)
		{
			// restart task
			lastTask.reconnect(this);
		}
	}
	
	public Object onRetainNonConfigurationInstance()
	{
		Object saved = null;
		if(task!=null && task.getStatus()==AsyncTask.Status.RUNNING) 
		{
			task.disconnect();
			saved=task;
		}
		return saved;
	}
	
	public void receiveData(Object returned)
	{
		Intent intent = new Intent();
		Bundle data = new Bundle();
		data.putBoolean("freemap.openhants.success", task.isSuccess());
		intent.putExtras(data);
		setResult(RESULT_OK, intent);
		finish();
	}

}
