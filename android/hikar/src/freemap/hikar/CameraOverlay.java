package freemap.hikar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import freemap.data.Point;

public class CameraOverlay extends View
{
	float azimuth,pitch,roll, hfov, height;
	Point gridref;

	String dataStatus;
	
	public CameraOverlay(Context ctx)
	{
		super(ctx);
		dataStatus="No data yet";
		hfov=40.0f;
		height=-1.0f;
	}
	
	// this is needed if you want to create from an XML layout file
	public CameraOverlay(Context ctx,android.util.AttributeSet s)
	{
		super(ctx,s);
	}
	
	public void onDraw(Canvas canvas)
	{
		//super.onDraw(canvas);
		//canvas.drawColor(Color.BLUE);
		Paint paint = new Paint();
		paint.setColor(Color.RED);
		//paint.setARGB(0,255,0,0);
		//canvas.drawCircle(100,100,20,paint);
		canvas.drawText("Azimuth " + azimuth + " pitch " + pitch + " roll " + roll,
				0, canvas.getHeight()/2, paint);
	
		
		canvas.drawText(
						((gridref==null) ? "Location unknown,":
							"Easting: " + (int)gridref.x + " northing: " + (int)gridref.y) +
							dataStatus,0, canvas.getHeight()/2 + 20, paint);
		
		canvas.drawText(" HFOV " + hfov + " Height " + height,
				0,canvas.getHeight()/2 + 40, paint);
		
		
	}
	
	public void setDirection(float az,float p,float r)
	{
		azimuth=az;
		pitch=p;
		roll=r;
		
	}
	
	public void setLocation(double easting,double northing)
	{
		if(gridref==null)
			gridref=new Point();
		gridref.x=easting;
		gridref.y=northing;	
	}
	
	public void setDataStatus(String ds)
	{
		dataStatus=ds;
	}
	
	public void changeHFOV(float amount)
	{
		hfov+=amount;
	}
	
	public void setHeight(float ht)
	{
		height=ht;
	}
}


