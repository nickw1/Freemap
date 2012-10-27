package freemap.andromaps;


import android.content.Context;
import android.os.AsyncTask;


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
		receive(data);
	}
	
	public abstract void receive(Object data);
}
