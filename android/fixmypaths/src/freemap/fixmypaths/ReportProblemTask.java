package freemap.fixmypaths;

import org.apache.http.NameValuePair;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import freemap.datasource.HTTPCommunicator;
import java.io.IOException;
import freemap.andromaps.ConfigChangeSafeTask;

public class ReportProblemTask extends ConfigChangeSafeTask<ArrayList<NameValuePair>, Void>{

	boolean success;
	
	public ReportProblemTask(Context ctx)
	{
		super(ctx);
	}
	
	public boolean isSuccess()
	{
		return success;
	}
	
	public String doInBackground(ArrayList<NameValuePair>... postData)
	{
		try
		{
			String resp=new HTTPCommunicator().postAndGetResponse
					("http://www.free-map.org.uk/hampshire/row.php", postData[0]);
			success=true;
			return resp;
		}
		catch(IOException e)
		{
			return e.getMessage();
		}
	}
	
	protected void onPostExecute(String result)
	{
		
		resultMsg=result;
		if(ctx!=null)
		{
			if(dlg!=null )
				dlg.dismiss();
			if(showDialogOnFinish)
				new AlertDialog.Builder(ctx).setPositiveButton("OK",null).setMessage(result).setCancelable(false).show();
		}
	}
}
