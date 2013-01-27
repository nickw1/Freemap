package freemap.andromaps;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public abstract class ConfigChangeSafeTask<Params,Progress> extends AsyncTask<Params,Progress,String> {

	protected Context ctx;
	protected ProgressDialog dlg;
	protected String resultMsg;
	protected boolean showDialogOnFinish, showProgressDialog;
	protected String dialogTitle, dialogMsg;
	
	public ConfigChangeSafeTask (Context ctx)
	{
		this.ctx=ctx;
		showDialogOnFinish=true;
		showProgressDialog=true;
	}
	
	public void setDialogDetails(String t, String m)
	{
		dialogTitle=t;
		dialogMsg=m;
	}
	
	public void disconnect()
	{
		dlg.dismiss();
		dlg=null;
		ctx=null;
	}
	
	public void setShowDialogOnFinish(boolean dof)
	{
		showDialogOnFinish=dof;
	}
	
	protected void setShowProgressDialog(boolean pd)
	{
		showProgressDialog=pd;
	}
	
	public void reconnect(Context ctx)
	{
		if(getStatus()==AsyncTask.Status.FINISHED)
		{
			if(showDialogOnFinish)
				DialogUtils.showDialog(ctx,resultMsg);
		}
		else
		{
			this.ctx=ctx;
			showDialog();
		}
	}

	
	protected void onPreExecute()
	{
		if(showProgressDialog)
			showDialog();
	}
	
	protected void showDialog()
	{
		dlg = ProgressDialog.show(ctx, dialogTitle, dialogMsg);
	}
	
	@Override
	protected void onPostExecute(String result)
	{
		
		resultMsg=result;
		if(ctx!=null)
		{
			if(dlg!=null )
				dlg.dismiss();
			if(showDialogOnFinish)
				showFinishDialog(result);
		}
	}
	
	public String getResultMsg()
	{
		return resultMsg;
	}
	
	protected void showFinishDialog(String result)
	{
		DialogUtils.showDialog(ctx, result);
	}
}
