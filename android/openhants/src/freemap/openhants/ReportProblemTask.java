package freemap.openhants;

import org.apache.http.NameValuePair;
import java.util.ArrayList;
import android.content.Context;
import freemap.datasource.HTTPDownloader;
import java.io.IOException;
import freemap.andromaps.ConfigChangeSafeTask;

public class ReportProblemTask extends ConfigChangeSafeTask<ArrayList<NameValuePair>, Void>{

	public ReportProblemTask(Context ctx)
	{
		super(ctx);
	}
	
	public String doInBackground(ArrayList<NameValuePair>... postData)
	{
		try
		{
			HTTPDownloader.post("http://www.free-map.org.uk/hampshire/row.php", postData[0]);
			return "ok";
		}
		catch(IOException e)
		{
			return e.getMessage();
		}
	}
}
