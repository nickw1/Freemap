package freemap.data;



public class GoogleProjection extends SimpleProjection implements Projection {
	
	public class Tile
	{
		public int x, y, z;
		
		Tile(int x,int y,int z)
		{
			this.x=x; this.y=y; this.z=z;
		}
	}
	
	public Point project (Point lonLat)
	{
		return new Point(lonToGoogle(lonLat.x), latToGoogle(lonLat.y));
	}
	
	public Point unproject (Point projected)
	{
		return new Point(googleToLon(projected.x),googleToLat(projected.y));
	}
	
	private double lonToGoogle(double lon)
	{
		return (lon/180) * 20037508.34;
	}
	
	private double latToGoogle(double lat)
	{
		double y = Math.log(Math.tan((90+lat)*Math.PI/360)) / (Math.PI/180);
		return y*20037508.34/180;
	}
	
	private double googleToLon(double x)
	{
			return (x/20037508.34) * 180.0;
	}
	
	private double googleToLat(double y)
	{
		double lat = (y/20037508.34) * 180.0;
		lat = 180/Math.PI * (2*Math.atan(Math.exp(lat*Math.PI/180)) - Math.PI/2);
		return lat;
	}
	
	public Tile getTile (Point p, int z)
	{
		double metresInTile = 40075016.68 / Math.pow(2,z);
		Tile tile = new Tile((int)((20037508.34+p.x) / metresInTile), 
							(int)((20037508.34-p.y) / metresInTile), z);
		return tile;
	}
	
	public String getID()
	{
		return "epsg:3785";
	}
	
	
}
