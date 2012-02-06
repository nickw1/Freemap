package freemap.opentrail;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import freemap.data.Point;

public class DownloadThread extends Thread
{
	ProgressDialog dlg;
	Handler handler;
	Context ctx;
	String progressTitle,progressMsg;
	
	public DownloadThread(Context ctx,Handler h)
	{
		this.handler=h;
		this.ctx=ctx;
		progressTitle="Downloading";
		progressMsg="Downloading...";
	}
	
	public void setDialogDetails(String pt, String pm)
	{
		progressTitle=pt;
		progressMsg=pm;
	}
	
	public void createDialog()
	{
		dlg=ProgressDialog.show(ctx,progressTitle,progressMsg,true,false);
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
}