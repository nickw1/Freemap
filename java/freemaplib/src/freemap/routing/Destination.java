package freemap.routing;


/**
 * Created by nick on 09/06/15.
 */

import freemap.data.POI;

// Represents a destination on a signpost.
public class Destination {

    POI poi;
    double distance;

    public Destination (POI poi, double distance)
    {
        this.poi = poi;
        this.distance = distance;
    }

    public String toString()
    {
        return poi.getValue("name") + " " + distance + " km";
    }

    public String getName()
    {
        return poi.getValue("name");
    }

    public String getType()
    {
        return poi.getValue("class");
    }
}

