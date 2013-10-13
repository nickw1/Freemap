package freemap.proj;


import freemap.data.ProjectionFactory;
import freemap.data.Projection;
import freemap.data.IdentityProjection;
import freemap.data.GoogleProjection;



public class Proj4ProjectionFactory implements ProjectionFactory {

	public Projection generate(String id)
	{
		id=id.toLowerCase();
		id=id.equals("epsg:3857") ? "epsg:3785" :id; // Library doesn't recognise 3857
		if (id.equals("epsg:4326"))
			return IdentityProjection.getInstance();
		// OSGB projection not complete with Java Proj.4 (WGS84 to OSGB36 not implemented), so
		// we have to use the JCoord version
		else if (id.equals("epsg:27700") || id.equals("osgb") || id.equals("27700"))
			return new OSGBProjection();
		else
		{
		    Proj4Projection proj4proj = new Proj4Projection(id);
		    return proj4proj.isValid() ? proj4proj : null;
		}
	}
}
