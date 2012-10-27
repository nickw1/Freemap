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


public class HTTPDownloader {

	int statusCode;
	
	private static HttpClient getClient(int timeout)
	{
		BasicHttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, timeout);
		HttpClient client = new DefaultHttpClient(params);
		return client;
	}
	
	public static InputStream getStream(String url) throws IOException
	{	
		HttpClient client = getClient(20000);
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);
		InputStream in = response.getEntity().getContent();
		return in;
	}
	
	// quickfix
	public InputStream getInputStream(String url) throws IOException
	{	
		HttpClient client = getClient(20000);
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);
		statusCode = response.getStatusLine().getStatusCode();
		if(statusCode==200)
		{
			InputStream in = response.getEntity().getContent();
			return in;
		}
		return null;
	}
	
	public static InputStream post(String url,ArrayList<NameValuePair> fields) throws IOException
	{
		HttpClient client = getClient(20000);
		HttpPost post = new HttpPost();
		post.setEntity(new UrlEncodedFormEntity(fields));
		HttpResponse response = client.execute(post);
		InputStream in = response.getEntity().getContent();
		return in;
	}
	
	public static String download(String url) throws IOException
	{
		InputStream in = getStream(url);
		return readStream(in);
	}
	
	public static String readStream(InputStream in) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String responseText= "", line;
		while((line=reader.readLine()) != null)
			responseText += line;
		return responseText;
	}
}
