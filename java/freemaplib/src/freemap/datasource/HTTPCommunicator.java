package freemap.datasource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.client.methods.HttpPost;
import java.util.ArrayList;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;


public class HTTPCommunicator {

	int statusCode;
	String auth;
	
	public HTTPCommunicator()
	{
		statusCode = 0;
	}
	
	private HttpClient getClient(int timeout)
	{
		BasicHttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, timeout);
		HttpClient client = new DefaultHttpClient(params);
		return client;
	}
	
	private HttpResponse doCommunication(String url) throws IOException
	{
		HttpClient client = getClient(20000);
		HttpGet request = new HttpGet(url);
		return client.execute(request);	
	}
	
	// quickfix
	public InputStream getInputStream(String url) throws IOException
	{	
		HttpResponse response=doCommunication(url);
		statusCode = response.getStatusLine().getStatusCode();
		if(statusCode==200)
		{
			InputStream in = response.getEntity().getContent();
			return in;
		}
		return null;
	}
	
	
	
	public InputStream post(String url,ArrayList<NameValuePair> fields) throws IOException
	{
		HttpClient client = getClient(20000);
	
		HttpPost post = new HttpPost(url);
		if(auth!=null)
			post.setHeader("Authorization","Basic "+auth);
		post.setEntity(new UrlEncodedFormEntity(fields));
		HttpResponse response = client.execute(post);
		statusCode = response.getStatusLine().getStatusCode();
		InputStream in = response.getEntity().getContent();
		return in;
	}
	
	public void setAuthentication(String auth)
	{
		this.auth=auth;
	}
	
	public String postAndGetResponse(String url,ArrayList<NameValuePair> fields) throws IOException
	{
		return readStream(post(url,fields));
	}
	
	public String download(String url) throws IOException
	{
		InputStream in = getInputStream(url);
		return (in==null)? "":readStream(in);
	}
	
	public String readStream(InputStream in) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String responseText= "", line;
		while((line=reader.readLine()) != null)
			responseText += line;
		return responseText;
	}
	
	public int getStatusCode()
	{
		return statusCode;
	}
}
