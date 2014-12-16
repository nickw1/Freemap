package freemap.andromaps02;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Marker;


import android.graphics.drawable.Drawable;
import android.widget.Toast;
import android.content.Context;


public class MapsforgeUtil
{
	static class TappableMarker extends Marker
	{
		String msg;
		Context ctx;
		
		public TappableMarker (Context ctx, LatLong latLong, Bitmap b, int x, int y, String msg)
		{
			super (latLong,b,x,y);
			this.msg=msg;	
			this.ctx=ctx;
		}
		public boolean onTap(LatLong point, org.mapsforge.core.model.Point viewPosition,
					org.mapsforge.core.model.Point tapPoint)
		{
			if (contains(viewPosition,tapPoint))
			{
				Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
				return true;
			}
			return false;
		}
		
	}
	
	public static Marker makeMarker (Drawable drawable, LatLong latLong)
	{
		Bitmap b = AndroidGraphicFactory.convertToBitmap(drawable);
		Marker marker = new Marker (latLong, b, 0, -b.getHeight()/ 2);
		return marker;
	}
	
	public static Marker makeTappableMarker (Context ctx, Drawable drawable, LatLong latLong, String text)
	{
		Bitmap b = AndroidGraphicFactory.convertToBitmap(drawable);
		Marker marker = new TappableMarker(ctx, latLong, b, 0, -b.getHeight()/2, text);
		return marker;
	}
	
	
	public static Paint makePaint (Color colour, int width, Style style)
	{
		Paint p = AndroidGraphicFactory.INSTANCE.createPaint();
		p.setColor(colour);
		p.setStrokeWidth(width);
		p.setStyle(style);
		return p;
	}
}
