// Orientation handling based on
// http://www.eigo.co.uk/Threads-and-Progress-Dialogs-in-Android-Screen-Orientation-Rotations.aspx

package freemap.opentrail;

import org.apache.http.HttpResponse;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.message.BasicNameValuePair;
import java.util.ArrayList;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.net.Uri;
import android.view.KeyEvent;

import android.view.inputmethod.InputMethodManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.location.Location;
import android.content.Intent;
import android.content.DialogInterface;


public class InputAnnotationActivity extends Activity 
{
	Handler handler;
	AnnotationSender as;
	Location loc;
	
	public class OKListener implements OnClickListener
	{
		public void onClick(View view)
		{
			sendAnnotation();
		}
	}
	
	public ProgressDialog dlg;
	
	public class AnnotationSender implements Runnable
	{
		ProgressDialog dlg;
		Handler handler;
		
		public AnnotationSender(Handler h)
		{
			this.handler=h;
			createDialog();
		}
		
		public void disconnect()
		{
			this.handler=null;
			dlg.dismiss();
			dlg=null;
		}
		
		public void reconnect(Handler h)
		{
			this.handler=h;
			createDialog();
		}
		
		private void createDialog()
		{
			dlg = ProgressDialog.show(InputAnnotationActivity.this,
					"Please wait","Sending to server, please wait...",true,false);
		}
		
		public void run()
		{
			Bundle bundle=new Bundle();
			boolean success=true;
			String dlgMsg="";
			EditText text=(EditText)findViewById(R.id.etAnnotation);
			String annText = Uri.encode(text.getText().toString());
			try
			{
				HttpParams params = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(params,20000);
				HttpConnectionParams.setSoTimeout(params,20000);
				HttpClient client = new DefaultHttpClient(params);
				HttpPost request = new HttpPost("http://www.free-map.org.uk/0.6/ws/annotation.php");
				ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
				postData.add(new BasicNameValuePair("action","create"));
				postData.add(new BasicNameValuePair("lon",String.valueOf(loc.getLongitude())));
				postData.add(new BasicNameValuePair("lat",String.valueOf(loc.getLatitude())));
				postData.add(new BasicNameValuePair("text",annText));
				request.setEntity(new UrlEncodedFormEntity(postData));
				
					
				HttpResponse response = client.execute(request);
				InputStream in = response.getEntity().getContent();
				BufferedReader reader=new BufferedReader(new InputStreamReader(in));
				String str = "", line;
				while((line=reader.readLine()) != null)
				{
					str+=line;
				}
				dlgMsg="Annotation created with ID " + str;
				bundle.putString("ID", str);
				bundle.putString("description", annText);
			}
			catch(Exception e)
			{
				dlgMsg = "Error communicating with server. Check you have net access";
				success=false;
			}
			finally
			{
				this.dlg.dismiss();
				Message msg=new Message();
				bundle.putString("msg",dlgMsg);
				bundle.putBoolean("success",success);
				msg.setData(bundle);
				if(this.handler!=null) this.handler.sendMessage(msg);
			}
		}
	} 
	
	
	public void sendAnnotation()
	{
		
		if(Shared.location!=null && as==null) 
		{					
			as = new AnnotationSender(handler);
			new Thread(as).start();
		}
		else if (Shared.location==null)
		{
			new AlertDialog.Builder(InputAnnotationActivity.this).setCancelable(false).
					setPositiveButton("OK",null).
					setMessage("Location unavailable").show();
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
		
		handler = new Handler() 
		{
			public void handleMessage(Message msg)
			{
				Bundle bundle = msg.getData();
				new AlertDialog.Builder(InputAnnotationActivity.this).setCancelable(false).
					setPositiveButton("OK",new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface i,int which)
							{
								InputAnnotationActivity.this.finish();
							}
						}).
					setMessage(bundle.getString("msg")).show();
				as=null;
				boolean success=bundle.getBoolean("success");
				Intent resultIntent = new Intent();
			
				if(success==true)
				{
					Bundle extras = new Bundle();
					extras.putDouble("lat",loc.getLatitude());
					extras.putDouble("lon",loc.getLongitude());
					extras.putString("description",bundle.getString("description"));
					extras.putString("ID",bundle.getString("ID"));
					resultIntent.putExtras(extras);
				}
				
				InputAnnotationActivity.this.setResult((success?RESULT_OK:RESULT_CANCELED),resultIntent);
			}
		};
		
		if(getLastNonConfigurationInstance()!=null)
		{
			as = (AnnotationSender)getLastNonConfigurationInstance();
			as.reconnect(handler);
		}
	}
	
	public Object onRetainNonConfigurationInstance()
	{
		if(as!=null)
		{
			as.disconnect();
			return as;
		}
		return super.onRetainNonConfigurationInstance();
	}
}