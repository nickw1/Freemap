

package freemap.opentrail;



import org.apache.http.message.BasicNameValuePair;
import java.util.ArrayList;
import org.apache.http.NameValuePair;



import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;

import android.net.Uri;
import android.view.KeyEvent;

import android.view.inputmethod.InputMethodManager;
import android.app.AlertDialog;

import android.content.Context;
import android.os.AsyncTask;

import android.preference.PreferenceManager;
import android.location.Location;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.SharedPreferences;


public class InputAnnotationActivity extends Activity implements InputAnnotationTask.Receiver
{
	
	Location loc;
	InputAnnotationTask iaTask;
	ArrayList<NameValuePair> postData;
	Intent resultIntent;
	
	public class OKListener implements OnClickListener
	{
		public void onClick(View view)
		{
			sendAnnotation();
		}
	}
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.inputannotation);
		Button ok1 = (Button)findViewById(R.id.btnOkInputAnnotation);
		ok1.setOnClickListener(new OKListener());
		Button cancel1=(Button)findViewById(R.id.btnCancelInputAnnotation);
		loc=Shared.location;
		if(loc!=null)
		{
			cancel1.setOnClickListener(new OnClickListener() {
				public void onClick(View v)
				{
					Intent resultIntent = new Intent();
					InputAnnotationActivity.this.setResult(RESULT_CANCELED,resultIntent);
					finish();
				}
			});
			EditText et = (EditText)findViewById(R.id.etAnnotation);
			
			et.setOnKeyListener (new OnKeyListener() {
			
				public boolean onKey(View v, int keyCode, KeyEvent event)
				{
					if(event.getAction()==KeyEvent.ACTION_DOWN)
					{
						switch(keyCode)
						{
					
							case KeyEvent.KEYCODE_ENTER:
								// hide the soft keyboard
								InputMethodManager imm=(InputMethodManager)getSystemService
									(Context.INPUT_METHOD_SERVICE);
								imm.hideSoftInputFromWindow(((EditText)findViewById(R.id.etAnnotation)).
										getWindowToken(),0);
								sendAnnotation();
								return true;
						}
					}
					return false;
				}
			});
			
		}
		else
		{
			new AlertDialog.Builder(this).setMessage("Location not known yet").setCancelable(false).
				setPositiveButton("OK",null).show();
		}
	}
	
	public void sendAnnotation()
	{
		EditText text=(EditText)findViewById(R.id.etAnnotation);
		String annText = Uri.encode(text.getText().toString());
		
			
		postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("action","create"));
		postData.add(new BasicNameValuePair("lon",String.valueOf(loc.getLongitude())));
		postData.add(new BasicNameValuePair("lat",String.valueOf(loc.getLatitude())));
		postData.add(new BasicNameValuePair("text",annText));
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		iaTask=new InputAnnotationTask(this, this);
		iaTask.setDialogDetails("Sending...", "Sending annotation");
		iaTask.setShowDialogOnFinish(false);
		String username=prefs.getString("prefUsername",""), password=prefs.getString("prefPassword","");
		if(username.equals("") || password.equals(""))
		{
			new AlertDialog.Builder(this).setMessage("You have not supplied a username and password in the " +
													"preferences. Your annotation will be sent but will need to " +
													"be authorised.").setPositiveButton 
														("OK", new DialogInterface.OnClickListener()
															{
																public void onClick(DialogInterface i, int which)
																{
																	iaTask.execute(postData);		
																}
															} ).setNegativeButton("Cancel",null).show();
		}
		else
		{
			iaTask.setLoginDetails(username,password);
			iaTask.execute(postData);
		}
			
		/*
		 AlertDialog dlg=new AlertDialog(this);
		 this.setContentView(R.id.logindialog);
		 Button dlgOk = (Button)findViewById(R.id.loginOk),
		 	dlgCancel = (Button)findViewById(R.id.loginCancel);
		 dlgOk.setOnClickListener (new OnClickListener()
		 							{
		 								public void onClick(View view)
		 								{
		 									iaTask.setLoginDetails
		 										((EditText)findViewById(R.id.txtUsername).getText().toString(),
		 										(EditText)findViewById(R.id.txtPassword).getText().toString());
		 									iaTask.execute(postData);
		 								}
		 							} );
		 dlg.show();
		*/
	}
	
	public void receiveResponse(String response)
	{
		resultIntent = new Intent();
		Bundle extras = new Bundle();
		boolean success = iaTask.isSuccess() && response!=null;
		extras.putBoolean("success", success);
		if(success)
		{
			extras.putString("ID", response);
			extras.putString("description", ((EditText)findViewById(R.id.etAnnotation)).getText().toString());
			extras.putDouble("lon", loc.getLongitude());
			extras.putDouble("lat", loc.getLatitude());
		}
		resultIntent.putExtras(extras);
		
		new AlertDialog.Builder(this).setPositiveButton("OK",
				
						new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface i, int which)
							{
								setResult(RESULT_OK, resultIntent);
								finish();
							}
						}
				
				).setMessage(iaTask.getResultMsg()).setCancelable(false).show();
		//finish();
	}
	
	public Object onRetainNonConfigurationInstance()
	{
		Object saved = null;
		if(iaTask!=null && iaTask.getStatus()==AsyncTask.Status.RUNNING) 
		{
			iaTask.disconnect();
			saved=iaTask;
		}
		return saved;
	}
}