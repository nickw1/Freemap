package freemap.fixmypaths;

import freemap.andromaps.DataCallbackTask;

import android.content.Context;
import freemap.datasource.FreemapDataset;
import freemap.data.Point;
import freemap.datasource.WebXMLSource;
import freemap.datasource.FreemapDataHandler;
import android.util.Log;

public class DownloadProblemsTask extends DataCallbackTask<Point,Void> {
	
	public interface ProblemsReceiver
	{
		public void receiveProblems(FreemapDataset dataset);
	}
	
	public DownloadProblemsTask(Context ctx, ProblemsReceiver receiver)
	{
		super(ctx,receiver);
		this.setDialogDetails("Downloading...", "Downloading problems...");
	}

	public String doInBackground(Point... points)
	{
		try
		{
			String url = "http://www.free-map.org.uk/hampshire/row.php?action=getAllProblems&inProj=4326&outProj=4326"+
					"&format=xml&bbox=" + points[0].x + "," + points[0].y+"," + points[1].x+","+points[1].y;
			
			WebXMLSource source = new WebXMLSource(url,new FreemapDataHandler());
			
			setData(source.getData());
		}
		catch(Exception e)
		{
			return e.getMessage();
		}
		
		
		return "OK";
	}
	
	public void receive(Object data)
	{
		((ProblemsReceiver)receiver).receiveProblems((FreemapDataset)data);
	}
}
