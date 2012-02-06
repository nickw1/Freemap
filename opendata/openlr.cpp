#include <mapnik/datasource_cache.hpp>
#include <mapnik/map.hpp>
#include <mapnik/agg_renderer.hpp>
#include <mapnik/image_util.hpp>
#include <mapnik/load_map.hpp>
#include <mapnik/projection.hpp>
#include <boost/tokenizer.hpp>
#include <gd.h>
#include <sstream>


// -80454 6629930

using namespace mapnik;

void parse_qs(char* cqs,int *e, int *n);

int main (int argc, char *argv[])
{
	FILE *fp;
	char *qs = getenv("QUERY_STRING");
	std::ostringstream sstrm;
	std::string tmpfile;
	if(qs)
	{
		int e,n;
		parse_qs(qs,&e,&n);

		if(e>=0 && n>=0)
		{
			printf("Content-type: image/png\n\n");
			
			datasource_cache::instance()->register_datasources
				("/usr/lib/mapnik/0.7/input");
			Map m(200,200);
			load_map(m,"/home/nick/public_html/map.xml");
			Envelope<double> bbox(e*1000,n*1000,(e+1)*1000,(n+1)*1000);
	
			m.zoomToBox(bbox);
			Image32 buf(m.getWidth(),m.getHeight());
			agg_renderer<Image32> r(m,buf);
			r.apply();
			sstrm<<"/home/www-data/"<<e<<n<<".png";
			tmpfile=sstrm.str();
			save_to_file<ImageData32>(buf.data(),tmpfile.c_str(),"png256");

			sstrm.str("");
			sstrm<<"/var/www/expts/vmdlfp200/tiles/vmdlfp200/0/"
				<< e << "/" << n << ".png";
			FILE *osfp=fopen(sstrm.str().c_str(),"rb");
			if(osfp)
			{
				FILE *osmfp=fopen(tmpfile.c_str(),"rb");
				if(osmfp)
				{
					gdImagePtr osImg = gdImageCreateFromPng(osfp),
						osmImg = gdImageCreateFromPng(osmfp);
					gdImageCopy(osImg,osmImg,0,0,0,0,200,200);
					gdImagePng(osImg,stdout);
					gdImageDestroy(osmImg);
					gdImageDestroy(osImg);
					fclose(osmfp);
					remove(tmpfile.c_str());
				}
				fclose(osfp);
			}
		}
		else
		{
			printf("Content-type: text/html\n\n");
			printf("Unable to extract e,n from query string\n");
		}
	}
	else
	{
		printf("Content-type: text/html\n\n");
		printf("You need to provide a query string!\n");
	}
	return 0;
}

void parse_qs(char* cqs, int *e, int *n)
{
	std::string qs=cqs, key, val;
	*e=-1;
	*n=-1;
	int lastamp=0;
	for(int i=0; i<=qs.length(); i++)
	{
		if(i==qs.length()||qs[i]=='&')
		{
			std::string keyval=qs.substr(lastamp,i-lastamp);
			for(int j=0; j<keyval.length(); j++)
			{
				if(keyval[j]=='=')
				{
					key=keyval.substr(0,j);
					val=keyval.substr(j+1);
					if(key=="e")
						*e=atoi(val.c_str());
					else if(key=="n")
						*n=atoi(val.c_str());
				}
			}
			lastamp=i+1;
		}
		if(*e>=0 && *n>=0)
			break;
	}
}
