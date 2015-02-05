package freemap.andromaps;


import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.content.DialogInterface;


public abstract class DataCallbackTask<Params,Progress> extends ConfigChangeSafeTask<Params,Progress> {


	protected Object receiver, data;
	
	
	
	public DataCallbackTask(Context ctx)
	{
		super(ctx);
	}
	
	public DataCallbackTask (Context ctx, Object receiver)
	{
		super(ctx);
		this.receiver=receiver;
	}
	
	public Object getReceiver()
	{
		return receiver;
	}
	
	public void setData(Object data)
	{
		this.data=data;
	}
	
	public void disconnect()
	{
		receiver=null;
		super.disconnect();
	}
	
	public void reconnect(Context ctx, Object receiver)
	{
		
		if(getStatus()==AsyncTask.Status.FINISHED && receiver!=null)
			receive(data);
		else
		{
			this.receiver=receiver;
			super.reconnect(ctx);
		}
	}
	
	protected void onPostExecute(String result)
	{
		super.onPostExecute(result);
		if(!showDialogOnFinish)
			receive(data);
	}
	
	protected void showFinishDialog(String result)
	{
		new AlertDialog.Builder(ctx).setPositiveButton("OK",
				
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface i, int which)
					{
						receive(data);
					}
				}
				
				).setMessage(result).setCancelable(false).show();
	}
	
	public abstract void receive(Object data);
}
