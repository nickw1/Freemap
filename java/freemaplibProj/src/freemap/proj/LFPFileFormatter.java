package freemap.proj;

import uk.me.jstott.jcoord.OSRef;
import freemap.data.Point;
import freemap.datasource.FileFormatter;

public class LFPFileFormatter extends FileFormatter
{
	public String format (Point bottomLeft) throws IllegalArgumentException
	{
		if(((int)bottomLeft.x) % 5000 !=0 || ((int)bottomLeft.y) % 5000 !=0)
			throw new IllegalArgumentException("must be 5km squares");
		OSRef gr = new OSRef(bottomLeft.x,bottomLeft.y);
		String s = gr.toSixFigureString();
		System.out.println("six figure string " + s);
		String gridletter=s.substring(0,2).toLowerCase();
		String fn = gridletter+"/"+gridletter + s.charAt(2) + s.charAt(5) + 
			((s.charAt(6)=='5') ? "n":"s") +
			((s.charAt(3)=='5') ? "e":"w");
		return fn + ".hgtl";
	}
}
