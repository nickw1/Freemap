package freemap.andromaps02;





import android.content.Context;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import freemap.andromaps02.ConfigChangeSafeTask;

public abstract class HTTPCommunicationTask extends ConfigChangeSafeTask<Void,Void> {

	public interface Callback
	{
		public void downloadFinished(int taskId, Object addData);
		public void downloadCancelled(int taskId);
		public void downloadError(int taskId);
	}
	
	protected String alertMsg;
	protected Callback callback;
	protected int taskId;
	protected String[] urls;
	protected Object addData;
	private boolean success;
	
	public HTTPCommunicationTask(Context ctx,  String[] urls, String alertMsg, Callback callback, int taskId)
	{
		super(ctx);
		this.alertMsg = alertMsg;
		this.callback = callback;
		this.taskId = taskId;
		this.urls=urls;
	}
	
	public void setAdditionalData(Object addData)
	{
		this.addData=addData;
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
						HTTPCommunicationTask.this.execute();
					}
				}
		 ).show();
	}
	
	protected void showFinishDialog(String result)
	{
		new AlertDialog.Builder(ctx).setPositiveButton("OK",
				
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface i, int which)
					{
						if(success==true)
							callback.downloadFinished(taskId, addData);
						else
							callback.downloadError(taskId);
					}
				}
				
				).setMessage(result).setCancelable(false).show();
	}
	
	protected void setSuccess(boolean success)
	{
		this.success=success;
	}
	
	public void disconnect()
	{
		callback=null;
		super.disconnect();
	}

	public void reconnect(Context ctx, Callback receiver)
	{
		
		if(getStatus()==AsyncTask.Status.FINISHED && callback!=null)
			callback.downloadFinished(taskId, addData);
		else
		{
			this.callback=receiver;
			super.reconnect(ctx);
		}
	}
}
