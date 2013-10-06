package freemap.jdem;

import freemap.datasource.FileFormatter;
import freemap.data.Point;

public class SRTMMicrodegFileFormatter extends FileFormatter {

    String script;
    int width, height;
    
    public SRTMMicrodegFileFormatter()
    {
        this("srtm2.php", 100000, 50000);
    }
    
    public SRTMMicrodegFileFormatter(String s, int width, int height)
    {
        script = s;
        this.width=width;
        this.height=height;
    }
    
    public String format (Point p)
    {
        return script + "?bbox=" + ((int)p.x) + "," + ((int)p.y) + "," +
                        ((int)p.x + width) + "," + ((int)p.y + height);        
    }
}
