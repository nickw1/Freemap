package freemap.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TrackPoint extends Point {
	public long timestamp;
	
	public TrackPoint()
	{
		timestamp = -1L;
	}
	
	public TrackPoint(double x, double y)
	{
		super(x,y);
		timestamp = -1L;
	}
	
	public TrackPoint(double x, double y, long timestamp)
	{
		super(x,y);
		this.timestamp = timestamp;
	}
	
	public void setTime(long time)
	{
		this.timestamp=time;
	}
	
	public String getGPXTimestamp()
	{
		Date date = new Date(timestamp);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.format(date);
	}
}
