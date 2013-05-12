import uk.me.jstott.jcoord.OSRef;

public class MakeGridRef {
	public static void main (String[] args)
	{
		if(args.length>1)
		{
			OSRef ref = new OSRef(Double.parseDouble(args[0]), Double.parseDouble(args[1]));
			System.out.println(ref.toSixFigureString().toLowerCase());
		}
		else if (args.length==1)
		{
		    OSRef ref= new OSRef(args[0]+"000000");
		    System.out.println((int)ref.getEasting()/1000+" "+(int)ref.getNorthing()/1000);
		}
		else
		{
			System.err.println("Usage: makegridref easting northing");
		}
	}
}
