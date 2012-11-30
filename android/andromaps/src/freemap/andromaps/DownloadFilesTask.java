package freemap.andromaps;





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
import java.io.IOException;

import freemap.datasource.HTTPCommunicator;
import freemap.andromaps.ConfigChangeSafeTask;

public abstract class DownloadFilesTask extends ConfigChangeSafeTask<Void,Void> {

	public interface Callback
	{
		public void downloadFinished(int taskId);
		public void downloadCancelled(int taskId);
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
			setNegativeButton("Cancel",new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface i, int which)
				{
					callback.downloadCancelled(taskId);
				}
			}).
			setPositiveButton("OK",
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
		
		HTTPCommunicator communicator = new HTTPCommunicator();
		
		try
		{
			for(int i=0; i<urls.length; i++)
			{
				InputStream in = communicator.getInputStream(urls[i]);
				if(in!=null)
					doWriteFile(in,localFiles[i]);
			}
			return "Successfully downloaded";
		}
		catch(Exception e)
		{
			return e.getMessage();
		}
	}
	
	
	
	protected void showFinishDialog(String result)
	{
		new AlertDialog.Builder(ctx).setPositiveButton("OK",
				
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface i, int which)
					{
						callback.downloadFinished(taskId);
					}
				}
				
				).setMessage(result).setCancelable(false).show();
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
	
	public abstract void doWriteFile(InputStream in, String localFile) throws IOException;
}
