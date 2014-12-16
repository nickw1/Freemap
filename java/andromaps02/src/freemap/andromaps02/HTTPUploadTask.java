package freemap.andromaps02;

import android.content.Context;
import java.util.ArrayList;
import org.apache.http.NameValuePair;
import freemap.datasource.HTTPCommunicator;


public class HTTPUploadTask extends HTTPCommunicationTask {
	
	ArrayList<NameValuePair> postData;
	String username,password;
	
	public HTTPUploadTask(Context ctx,  String url,  ArrayList<NameValuePair> postData, String alertMsg, Callback callback, int taskId)
	{
		super(ctx,new String[]{url},alertMsg,callback,taskId);	
		this.postData=postData;
	}
	
	
	public String doInBackground(Void... unused)
	{
		
		HTTPCommunicator communicator = new HTTPCommunicator();
		if(username!=null && password!=null)
		{
			String details=username+":"+password;
			communicator.setAuthentication(Base64.encodeBytes(details.getBytes()));
		}
		try
		{
			String response = communicator.postAndGetResponse(urls[0], postData);
			setSuccess(communicator.getStatusCode()==200);
			setAdditionalData(response);
			return (communicator.getStatusCode()==200) ? "Successfully uploaded" : "Upload failed with HTTP code " + 
						communicator.getStatusCode();
		}
		catch(Exception e)
		{
			return e.getMessage();
		}
	}
	
	public void setLoginDetails(String username, String password)
	{
		this.username=username;
		this.password=password;
	}
	
	public void setPostData(ArrayList<NameValuePair> postData)
	{
	    this.postData = postData;
	}
}
