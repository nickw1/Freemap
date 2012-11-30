package freemap.andromaps;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public abstract class ConfigChangeSafeTask<Params,Progress> extends AsyncTask<Params,Progress,String> {

	protected Context ctx;
	protected ProgressDialog dlg;
	protected String resultMsg;
	protected boolean showDialogOnFinish;
	protected String dialogTitle, dialogMsg;
	
	public ConfigChangeSafeTask (Context ctx)
	{
		this.ctx=ctx;
		showDialogOnFinish=true;
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
	
	public void reconnect(Context ctx)
	{
		if(getStatus()==AsyncTask.Status.FINISHED)
		{
			if(showDialogOnFinish)
				new AlertDialog.Builder(ctx).setPositiveButton("OK",null).setMessage(resultMsg).setCancelable(false).show();
		}
		else
		{
			this.ctx=ctx;
			showDialog();
		}
	}

	
	protected void onPreExecute()
	{
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
		new AlertDialog.Builder(ctx).setPositiveButton("OK",null).setMessage(result).setCancelable(false).show();
	}

}
