package freemap.opentrail;

import freemap.datasource.FreemapDataset;
import android.location.Location;
import freemap.data.Projection;
import java.util.ArrayList;
import freemap.data.Walkroute;

public class Shared {
	public static FreemapDataset pois;
	public static Location location;
	public static Projection proj;
	public static ArrayList<Walkroute> walkroutes = new ArrayList<Walkroute>();
}
