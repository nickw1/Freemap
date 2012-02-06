// LandForm-Panorama DXF to a PostGIS DB

#include "Contour.h"
#include "Projections.h"
#include <iostream>
#include <cstdio>
#include <cstring>
#include <cstdlib>
using std::cout;
using std::cerr;
using std::endl;
using std::vector;

#include <sstream>

#include <postgresql/libpq-fe.h>

vector<Contour> read_dxf(char *dxffile,bool);
Contour read_contour(FILE *fp,bool);

int main (int argc, char *argv[])
{
	bool togoog=false,sqlstdout=false;

	if(argc>=2 && !strcmp(argv[1],"-g"))
	{
		togoog=true;
		argc--;
		argv++;
	}

	if(argc>=2 && !strcmp(argv[1],"-s"))
	{
		sqlstdout=true;
		argc--;
		argv++;
	}

	if(argc<2)
	{
		cerr<<"Usage: lfp2pg [-g] [-s] LFPfile" << endl <<
			  "    -g: Reproject to 'Google' Spherical Mercator" << endl<<
			  "    -s: SQL to stdout only (not to DB)" << endl;
		exit(1);
	}


	vector<Contour> contours = read_dxf(argv[1],togoog);

	PGconn *conn;

	if(!sqlstdout)
		conn = PQconnectdb("user=gisuser dbname=gis");
	if(sqlstdout || PQstatus(conn)!=CONNECTION_BAD)
	{
		for(int i=0; i<contours.size(); i++)
		{
			string wkt=contours[i].toWKT();
			std::stringstream ss;
			ss << "INSERT INTO contours (height,way) VALUES ("
				<< contours[i].getHeight() << ","
				<<"GeomFromText('" << wkt << "', "<<
				(togoog? 900913:27700)<<"));";
			//cout << ss.str()<<endl;
			if(sqlstdout)
				cout<<ss.str().c_str()<<endl;
			else
				PQexec(conn,ss.str().c_str());
		}
	}
	else
	{
		fprintf(stderr,"Error: %s\n", PQerrorMessage(conn));
	}
	if(!sqlstdout)
		PQfinish(conn);
	return 0;	
}

vector<Contour> read_dxf(char *dxffile, bool togoog)
{
	char buf[1024];
	vector<Contour> result;
	FILE *fp=fopen(dxffile,"r");
	if(fp!=NULL)
	{
		while(fgets(buf,1024,fp))
		{
			if(!strncmp(buf,"POLYLINE",8))
			{
				while(strncmp(buf,"G8040201",8))
				{
					fgets(buf,1024,fp);
					if(!strncmp(buf,"SEQEND",6))
						break;
				}

				if(!strncmp(buf,"G8040201",8))
					result.push_back(read_contour(fp,togoog));
			}
		}	
	}
	return result;
}

Contour read_contour(FILE *fp,bool togoog)
{
	char buf[1024];
	double x,y,z;
	while(strncmp(buf," 30",3))
		fgets(buf,1024,fp);
	fgets(buf,1024,fp);
	z=atof(buf);
	Contour contour(z);


	for(;;)
	{

		while(strncmp(buf,"VERTEX",6))
		{
			fgets(buf,1024,fp);
			if(!strncmp(buf,"SEQEND",6))
			{
				return contour;
			}
		}
	
		while(strncmp(buf," 10",3))
			fgets(buf,1024,fp);

		fgets(buf,1024,fp);
		x = atof(buf);

		while(strncmp(buf," 20",3))
			fgets(buf,1024,fp);

		fgets(buf,1024,fp);
		y = atof(buf);
	
		if(togoog)
			pj_transform(Projections::osgb,Projections::goog,1,1,&x,&y,NULL);	
		contour.addPoint(x,y);
	}
}
