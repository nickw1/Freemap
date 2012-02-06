package freemap.data;

// for generating projections from IDs e.g. EPSG:whatever
// Allows the freemap xml parser to have a "plug in" projection-from-ID generator
// This could, in an application, be from Proj4 - but it allows freemaplib to 
// exist independently of proj4.

public interface ProjectionFactory {
	public Projection generate(String id);

}
