package freemap.data;

import java.util.Arrays;
import java.util.ArrayList;

public class Algorithms 
{
    
        static class FurthestPoint
        {
            public int index;
            public double maxDist;
            
            public FurthestPoint()
            {
                index = -1;
                maxDist = 0.0;
            }
        }
        
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
       

       public static TrackPoint[] douglasPeucker (TrackPoint[] points, double distMetres, int recDepth, int callno)
       {
           FurthestPoint p = getFurthest(points);
              
           if (p.maxDist > distMetres && recDepth < 100)
           {
               System.out.println("Recursion depth: " + recDepth + " callno=" + callno);
               
               TrackPoint[] before = Arrays.copyOfRange(points, 0, p.index+1), after = Arrays.copyOfRange(points, p.index, points.length);
               
               TrackPoint[] simp1 = douglasPeucker(before,distMetres,recDepth+1, 1), 
                           simp2 = douglasPeucker(after,distMetres,recDepth+1, 2);
               TrackPoint[] merged = new TrackPoint[simp1.length + simp2.length - 1];
               System.arraycopy(simp1, 0, merged, 0, simp1.length);
               System.arraycopy(simp2, 1, merged, simp1.length, simp2.length-1);
               return merged;
           }
           else
               return new TrackPoint[] { points[0], points[points.length-1] };
       }   
   

       public static TrackPoint[] douglasPeuckerNonRecursive(TrackPoint[] points, double distMetres)
       {
           ArrayList<TrackPoint[]> descendant = null, current = new ArrayList<TrackPoint[]>();
           boolean ended=false;
           TrackPoint[] simplified = null;
           TrackPoint[] initial= points.clone();
          
           current.add(initial);
           int nNeedsSimplifying;
           int[] starts = new int[2], ends = new int[2];
           starts[0] = 0;
           while(!ended)
           {
               descendant = new ArrayList<TrackPoint[]>();
               nNeedsSimplifying=0;
               for(int i=0; i<current.size(); i++)
               {
                   if(current.get(i).length>2)
                   {
                       FurthestPoint furthest=getFurthest(current.get(i));
                       starts[1] = furthest.index;
                       ends[0] = furthest.index;
                       ends[1] = current.get(i).length-1;
                       for (int j=0; j<2; j++)
                       {
                          
                           TrackPoint[] tmp;
                          
                           if(furthest.maxDist <= distMetres)
                           {
                               tmp = new TrackPoint[] { current.get(i)[starts[j]], current.get(i)[ends[j]] };
                               
                           }
                           else
                           {
                               tmp = Arrays.copyOfRange(current.get(i), starts[j], ends[j]+1);
                               nNeedsSimplifying++;
                           }
                           descendant.add(tmp);
                       }
                   }
                   else
                   {
                       descendant.add(current.get(i).clone());
                   }    
               }
               
               if(nNeedsSimplifying==0)
               {
                   simplified = new TrackPoint[descendant.size()+1];
                   simplified[0] = new TrackPoint(descendant.get(0)[0]);
                   for(int i=0; i<descendant.size(); i++)
                       simplified[i+1] = new TrackPoint(descendant.get(i)[1]);
                   ended = true;
               }
               
               current = descendant;
           }
           return simplified;
       }
       
       public static FurthestPoint getFurthest(TrackPoint[] points)
       {
           FurthestPoint p = new FurthestPoint();
           
           double curDist;
           
           for(int i=1; i<points.length-1; i++)
           {
               curDist = points[i].haversineDistToLine(points[0], points[points.length-1]);
               if(curDist > p.maxDist)
               {
                   p.index = i;
                   p.maxDist = curDist;
               }
           }
         
           return p;
       }
}
