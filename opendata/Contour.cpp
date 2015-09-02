#include "Contour.h"

string Contour::toWKT()
{
    std::stringstream ss;
    ss<<"LINESTRING (";
    for(uint i=0; i<points.size(); i++)
    {
        if(i>0)
            ss<<",";
        ss<<(uint)points[i].x<<" " <<(uint)points[i].y;
    }
    ss<< ")";
    return ss.str();
}


string Contour::toOSM(long id, long ndid)
{
    std::stringstream ss;

    for(uint i=0; i<points.size(); i++)
        ss << "<node timestamp='1970-01-01T00:00:01Z' version='1'  lat='" << points[i].y << "' lon='"
        << points[i].x << "' id='"<<ndid+i<<"'></node>\n";

    ss << "<way timestamp='1970-01-01T00:00:01Z' version='1' "
		<< "id='"<<id<<"'><tag k='height' v='" << (int)height << "' />\n";
    ss << "<tag k='natural' v='contour' />\n";

    for(uint i=0; i<points.size(); i++)
        ss << "<nd ref='" << ndid+i << "' />\n";

    ss << "</way>\n";

    return ss.str();
}
