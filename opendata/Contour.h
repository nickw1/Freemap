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

	string toWKT();
	string toOSM(long, long);

	int nPoints() { return points.size(); }
};

#endif


