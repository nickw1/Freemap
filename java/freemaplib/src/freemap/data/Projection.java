package freemap.data;


public interface Projection {
	public  Point project(Point lonLat);
	public   Point unproject(Point projected);
	public  String getID();
	public boolean equals (Projection other);

}
