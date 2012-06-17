package freemap.opentrail;

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

public class HTTPDownloader {
	

	public static InputStream getStream(String url) throws IOException
	{
		BasicHttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params,20000);
		HttpClient client = new DefaultHttpClient(params);	
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);
		InputStream in = response.getEntity().getContent();
		return in;
	}
	
	public static String download(String url) throws IOException
	{
		InputStream in = getStream(url);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String responseText= "", line;
		while((line=reader.readLine()) != null)
			responseText += line;
		return responseText;
	}
}
