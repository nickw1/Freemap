#ifndef CONTOUR_H
#define CONTOUR_H

#include <vector>
#include <string>
#include <sstream>

using std::string;


struct Point
{
	double x, y;
};

class Contour
{
private:
	double height;
	std::vector<Point> points;

public:
	Contour(double h)
	{
		height=h;
	}

	void addPoint(double x, double y)
	{
		Point p = { x,y };
		points.push_back(p);
	}
		
	double getHeight() { return height; }

	string toWKT()
	{
		std::stringstream ss;
		ss<<"LINESTRING (";
		for(int i=0; i<points.size(); i++)
		{
			if(i>0)
				ss<<",";
			ss<<(int)points[i].x<<" " <<(int)points[i].y;
		}
		ss<< ")";
		return ss.str();
	}
};

#endif


