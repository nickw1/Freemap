package freemap.openhants;





import android.content.Context;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.InputStream;

import freemap.datasource.HTTPDownloader;
import freemap.andromaps.ConfigChangeSafeTask;

public class DownloadFilesTask extends ConfigChangeSafeTask<Void,Void> {

	public interface Callback
	{
		public void downloadFinished(int taskId);
	}
	
	String alertMsg;
	Callback callback;
	int taskId;
	String[] urls, localFiles;
	
	public DownloadFilesTask(Context ctx,  String[] urls, String[] localFiles, String alertMsg, Callback callback, int taskId)
	{
		super(ctx);
		this.alertMsg = alertMsg;
		this.callback = callback;
		this.taskId = taskId;
		this.urls=urls;
		this.localFiles=localFiles;
	}
	
	public void confirmAndExecute()
	{
		new AlertDialog.Builder(ctx).setMessage(alertMsg).
			setNegativeButton("Cancel",null).setPositiveButton("OK",
					new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface i, int which)
					{
						DownloadFilesTask.this.execute();
					}
				}
		 ).show();
	}

	public String doInBackground(Void... unused)
	{
		
		
		try
		{
			for(int i=0; i<urls.length; i++)
			{
				InputStream in = HTTPDownloader.getStream(urls[i]);
				PrintWriter writer = new PrintWriter(new FileWriter(localFiles[i]));
				BufferedReader reader=new BufferedReader(new InputStreamReader(in));
				String line;
				while((line=reader.readLine()) != null)		
				{
					writer.println(line);
				}	    					
				writer.close();
			}
			return "Successfully downloaded";
		}
		catch(Exception e)
		{
			return e.getMessage();
		}
	}
	
	protected void onPostExecute(String result)
	{
		super.onPostExecute(result);
		callback.downloadFinished(taskId);
	}
	
	public void disconnect()
	{
		callback=null;
		super.disconnect();
	}

	public void reconnect(Context ctx, Callback receiver)
	{
		
		if(getStatus()==AsyncTask.Status.FINISHED && callback!=null)
			callback.downloadFinished(taskId);
		else
		{
			this.callback=receiver;
			super.reconnect(ctx);
		}
	}
}
