package freemap.proj;


import freemap.data.ProjectionFactory;
import freemap.data.Projection;



public class Proj4ProjectionFactory implements ProjectionFactory {

	public Projection generate(String id)
	{
		id=id.toLowerCase();
		if (id.equals("epsg:4326"))
			return null;
		// OSGB projection not complete with Java Proj.4 (WGS84 to OSGB36 not implemented), so
		// we have to use the JCoord version
		else if (id.equals("epsg:27700") || id.equals("osgb") || id.equals("27700"))
			return new OSGBProjection();
		else
			return new Proj4Projection(id);
	}
}
