#include <mapnik/datasource_cache.hpp>
#include <mapnik/map.hpp>
#include <mapnik/agg_renderer.hpp>
#include <mapnik/image_util.hpp>
#include <mapnik/load_map.hpp>
#include <mapnik/projection.hpp>

#include <cstdlib>
#include <string>

#include <gd.h>

using namespace mapnik;
using std::cout;
using std::cerr;
using std::endl;

struct GridRef
{
	int e,n;
};

GridRef getBottomLeft(char* hundredKMSquare);

int main (int argc, char *argv[])
{
    if(argc<5)
    {
        cerr<<"Usage: " << argv[0] << 
        " <mapfile> <100 km grid square> " 
        << "<VMD directory> <output directory>" << endl;
        exit(1);
    }

    datasource_cache::instance()->register_datasources
        ("/usr/lib/mapnik/0.7/input");
    Map m (4000,4000);
    load_map(m,argv[1]);

	GridRef bottomLeft = getBottomLeft(argv[2]);

	char infile[1024], outfile[1024];

	int i=0;

    for(int easting=bottomLeft.e; easting<bottomLeft.e+100000;easting+=10000)
    {
        for(int northing=bottomLeft.n; northing<bottomLeft.n+100000;
            northing+=10000)
        {
			cerr<<easting<<" "<<northing<<endl;
            Envelope<double> bbox(easting,northing,
					easting+10000,northing+10000);
            m.zoomToBox(bbox);


            Image32 buf(m.getWidth(),m.getHeight());
            agg_renderer<Image32> r(m,buf);
            r.apply();

			char tmpfile[1024];
			sprintf(tmpfile,"%s/tmp%s%02d.png",argv[3],argv[2],i);
            save_to_file<ImageData32>(buf.data(),tmpfile,"png");
			/*
            FILE *mapnikFP=fopen(tmpfile,"rb");
            if(mapnikFP)
            {
				sprintf(infile,"%s/%s%02d.png",argv[3],argv[2],i);
                FILE *vmdFP=fopen(infile,"rb");
                if(vmdFP)
                {    
					fprintf(stderr,"Opened %s OK\n",infile);
					fprintf(stderr,"i is %d\n",i);
                    gdImagePtr mapnikImg,vmdImg;
                    mapnikImg=gdImageCreateFromPng(mapnikFP);
                    vmdImg=gdImageCreateFromPng(vmdFP);
					gdImageAlphaBlending(vmdImg,0);
					gdImageAlphaBlending(mapnikImg,0);
                    gdImageCopy(vmdImg,mapnikImg,0,0,0,0,4000,4000);
					sprintf(outfile,"%s/%s%02d.png",argv[4],argv[2],i);
                    FILE *out=fopen(outfile,"wb");
                    if(out)
                    {
						fprintf(stderr,"Opened %s OK\n",outfile);
                        gdImagePng(vmdImg,out);
                        fclose(out);    
                    }
                    gdImageDestroy(vmdImg);
                    gdImageDestroy(mapnikImg);
                    fclose(vmdFP);
                }
                fclose(mapnikFP);    
            }
			*/	
			i++;
        }
    }
        
    return 0;
}

GridRef getBottomLeft(char* hundredKMSquare)
{
	int idxmjr, idxmnr;
	idxmjr = (hundredKMSquare[0] >= 'J') ? hundredKMSquare[0] - 'A'-1: 
			hundredKMSquare[0] - 'A';
	idxmnr = (hundredKMSquare[1] >= 'J') ? hundredKMSquare[1] - 'A'-1:
			hundredKMSquare[1] - 'A';
	GridRef out;
	int emajor = ((idxmjr%5)-2) * 500000;
	int nmajor = (4-((idxmjr+3)/5)) * 500000;
	int eminor = (idxmnr%5)*100000;
	int nminor = (4-(idxmnr/5))*100000;
	out.e = emajor + eminor;
	out.n = nmajor + nminor;
	return out;
}
