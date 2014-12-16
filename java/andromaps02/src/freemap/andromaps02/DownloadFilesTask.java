package freemap.andromaps02;





import android.content.Context;




import java.io.InputStream;
import java.io.IOException;

import freemap.datasource.HTTPCommunicator;


public abstract class DownloadFilesTask extends HTTPCommunicationTask {

	String[] localFiles;
	
	public DownloadFilesTask(Context ctx,  String[] urls, String[] localFiles, String alertMsg, Callback callback, int taskId)
	{
		super(ctx,urls,alertMsg,callback,taskId);
		this.localFiles=localFiles;
	}
		
	public String doInBackground(Void... unused)
	{
		
		HTTPCommunicator communicator = new HTTPCommunicator();
		
		try
		{
			String msg="Failed to download files:";
			int errors=0;
			for(int i=0; i<urls.length; i++)
			{
				InputStream in = communicator.getInputStream(urls[i]);
				if(in!=null)
					doWriteFile(in,localFiles[i]);
				else
				{
					errors++;
					msg += " " + (i+1);
				}
			} 
			setSuccess(errors!=urls.length);
			return errors==0 ? "Successfully downloaded" :
				(errors == urls.length ? "Failed to download files": msg);
		}
		catch(Exception e)
		{
			return e.getMessage();
		}
	}
	
	public abstract void doWriteFile(InputStream in, String localFile) throws IOException;
}
