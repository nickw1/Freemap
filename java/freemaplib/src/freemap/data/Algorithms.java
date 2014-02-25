package freemap.data;

import java.util.Arrays;

public class Algorithms {
 // www.faqs.org/faqs/geography/infosystems-faq
    public static double haversineDist(double lon1, double lat1, double lon2, double lat2)
    {
        double R = 6371000;
        double dlon=(lon2-lon1)*(Math.PI / 180);
        double dlat=(lat2-lat1)*(Math.PI / 180);
        double slat=Math.sin(dlat/2);
        double slon=Math.sin(dlon/2);
        double a = slat*slat + Math.cos(lat1*(Math.PI/180))*Math.cos(lat2*(Math.PI/180))*slon*slon;
        double c = 2 *Math.asin(Math.min(1,Math.sqrt(a)));
        return R*c;
    }
    

    public static Point[] douglasPeucker (Point[] points, double distMetres)
    {
        int index = -1;
        double maxDist = 0;
        
        double curDist;
        
        for(int i=1; i<points.length-1; i++)
        {
            curDist = points[i].haversineDistToLine(points[0], points[points.length-1]);
            if(curDist > maxDist)
            {
                index = i;
                maxDist = curDist;
            }
        }
        
        if (maxDist > distMetres)
        {
            Point[] before = Arrays.copyOfRange(points, 0, index), after = Arrays.copyOfRange(points, index, points.length-1);
            Point[] simp1 = douglasPeucker(before,distMetres), 
                        simp2 = douglasPeucker(after,distMetres);
            Point[] merged = new Point[simp1.length + simp2.length - 1];
            System.arraycopy(simp1, 0, merged, 0, simp1.length);
            System.arraycopy(simp2, 1, merged, simp1.length, simp2.length-1);
            return merged;
        }
        else
            return new Point[] { points[0], points[points.length-1] };
    }   
}
