package freemap.openhants;

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
					String problemDetails = ((EditText)findViewById(R.id.editTextReportProblem)).getText().toString();
					String problemType = (String) ((Spinner)findViewById(R.id.spinnerProblemType)).getSelectedItem();
					// send to the server
					
					ArrayList<NameValuePair> postData;
					postData = new ArrayList<NameValuePair>();
					postData.add(new BasicNameValuePair("action", "addProblem"));
					postData.add(new BasicNameValuePair("routeno",rowDetails.getString("routeno")));
					postData.add(new BasicNameValuePair("rowtype",rowDetails.getString("row_type")));
					postData.add(new BasicNameValuePair("parish",rowDetails.getString("parish")));
					postData.add(new BasicNameValuePair("email","countryside@hants.gov.uk"));
					postData.add(new BasicNameValuePair("category",problemType)); // cboCategory
					postData.add(new BasicNameValuePair("problem",problemDetails)); // txtProblem
					postData.add(new BasicNameValuePair("lat",String.valueOf(rowDetails.getDouble("lat"))));
					postData.add(new BasicNameValuePair("lon",String.valueOf(rowDetails.getDouble("lon"))));
					
					/// ... asynctask
					
					/*
					task = new ReportProblemTask (ReportProblemActivity.this);
					task.execute(postData);
					*/
					new AlertDialog.Builder(ReportProblemActivity.this).setMessage
						("Unimplemented - Awaiting permission from HCC").
							setPositiveButton("OK",null).setCancelable(false).show();
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
		data.putString("freemap.openhants.code", "");
		intent.putExtras(data);
		setResult(RESULT_OK, intent);
		finish();
	}

}
