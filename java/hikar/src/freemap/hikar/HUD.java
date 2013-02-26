package freemap.hikar;

import android.view.View;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.content.Context;

public class HUD extends View{

    float[] orientation;
    double height;
    Paint paint;
    
    public HUD(Context ctx)
    {
        super(ctx);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(24);
        orientation = new float[3];
    }
    
    public void setOrientation(float[] orientation)
    {
        if(orientation.length>=3)
        {
            for(int i=0; i<3; i++)
                this.orientation[i] = (float)(orientation[i]*180.0/Math.PI);
        }
    }
    
    public void setHeight(double height)
    {
        this.height = height;    
    }
    
    public void onDraw (Canvas canvas)
    {
        super.onDraw(canvas);
       
       
        if(orientation!=null)
        {
           
            String data = String.format ("Azimuth %8.3f pitch %8.3f roll %8.3f ht %8.3f", orientation[0], orientation[1],
                                            orientation[2], height);
            canvas.drawText(data, 0, getHeight()/2, paint);
            
        }
    }
}
