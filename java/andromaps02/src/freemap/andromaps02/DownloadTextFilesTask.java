package freemap.andromaps02;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.IOException;

import android.content.Context;


public class DownloadTextFilesTask extends DownloadFilesTask {

	public DownloadTextFilesTask(Context ctx,  String[] urls, String[] localFiles, String alertMsg, Callback callback, 
			int taskId)
	{
		super(ctx,urls,localFiles,alertMsg,callback,taskId);
	}
	
	public void doWriteFile(InputStream in, String outputFile) throws IOException
	{
		PrintWriter writer = new PrintWriter(new FileWriter(outputFile));
		BufferedReader reader=new BufferedReader(new InputStreamReader(in));
		String line;
		while((line=reader.readLine()) != null)		
		{
			writer.println(line);
		}	    					
		writer.close();
	}
}
