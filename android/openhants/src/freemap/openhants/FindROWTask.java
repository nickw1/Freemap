package freemap.openhants;

import android.location.Location;
import freemap.datasource.WebXMLSource;
import java.util.HashMap;
import android.content.Context;
import android.os.AsyncTask;
import freemap.andromaps.DataCallbackTask;

public class FindROWTask extends DataCallbackTask<Location,Void> {

	public interface ROWReceiver
	{
		public void receiveROW(HashMap<String,String> row);
	}
	
	
	public FindROWTask(Context ctx, ROWReceiver receiver)
	{
		super(ctx, receiver);
		setShowDialogOnFinish(false);
		setDialogDetails("Finding ROW", "Finding nearest right of way...");
	}
	
	public String doInBackground(Location... point)
	{
		try
		{
			WebXMLSource xmlsource = new WebXMLSource
				("http://www.free-map.org.uk/hampshire/row.php?action=findNearest"+
			 "&x=" + point[0].getLongitude() + "&y=" + point[0].getLatitude()+ 
			 "&inProj=4326&format=xml&dist=100", new ROWHandler());
	
			HashMap<String,String> row = (HashMap<String,String>)xmlsource.getData();
			
			setData(row);
			
			return "Successfully downloaded";
		}
		catch(Exception e)
		{
			setShowDialogOnFinish(true);
			return e.getMessage();
		}
		
	}
	
	public void receive(Object row)
	{
		if(receiver!=null)
			((ROWReceiver)receiver).receiveROW((HashMap<String,String>) row);
	}
}
