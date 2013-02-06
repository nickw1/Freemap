import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.XMLReader;
import java.io.FileInputStream;
import org.xml.sax.InputSource;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintStream;
import java.io.FileOutputStream;

public class OSMReproject {
	public static void main (String[] args) throws Exception
	{
		boolean unproj=false;
		int argIndex=0;
		double multiplier=0.0001;
		
		if(args.length>0 && args[0].equals("-u"))
		{
			unproj=true;
			argIndex++;
		}
		
		if(args.length>argIndex+1 && args[argIndex].equals("-d"))
		{
			multiplier = Double.parseDouble(args[argIndex+1]);
			argIndex+=2;
		}
		
		if(args.length<argIndex+3)
		{
			System.err.println("Usage: java OSMReproject [-u] [-m multiplier] proj infile outfile");
			System.exit(0); 
		}
		
		SAXParserFactory fact = SAXParserFactory.newInstance();
		SAXParser parser = fact.newSAXParser();
		XMLReader reader = parser.getXMLReader();
		ReprojectHandler handler = new ReprojectHandler(new PrintStream(new FileOutputStream(args[argIndex+2])),
															args[argIndex], unproj, multiplier);
		reader.setContentHandler(handler);
		
		FileInputStream in = new FileInputStream(args[argIndex+1]);
		
		reader.parse(new InputSource(in));
	}
}
