#ifndef PROJECTIONS_H
#define PROJECTIONS_H

#include <proj_api.h>

struct Projections
{
	static projPJ lonlat, goog, osgb;
};

projPJ Projections::lonlat = pj_init_plus("+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs");

projPJ Projections::goog = pj_init_plus("+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +no_defs +over");

projPJ Projections::osgb = pj_init_plus("+proj=tmerc +lat_0=49 +lon_0=-2 +k=0.999601 +x_0=400000 +y_0=-100000 +ellps=airy +units=m +towgs84=446.448,-125.157,542.060,0.1502,0.2470,0.8421,-20.4894 +units=m +nodefs");

#endif
