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
        
        static class IndexedTrackPoint extends TrackPoint
        {
            public int index;
           
            
            public IndexedTrackPoint (TrackPoint tp, int i)
            {
                this.x = tp.x;
                this.y = tp.y;
                this.z = tp.z;
                this.timestamp = tp.timestamp;
                index=i;
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
       

       public static TrackPoint[] douglasPeucker (TrackPoint[] points, double distMetres)
       {
           if(points.length > 2)
           {
               FurthestPoint p = getFurthest(points);
              
               if (p.maxDist > distMetres )
               {
                   TrackPoint[] before = Arrays.copyOfRange(points, 0, p.index+1), after = Arrays.copyOfRange(points, p.index, points.length);
               
                   TrackPoint[] simp1 = douglasPeucker(before,distMetres), 
                           simp2 = douglasPeucker(after,distMetres);
                   TrackPoint[] merged = new TrackPoint[simp1.length + simp2.length - 1];
                   System.arraycopy(simp1, 0, merged, 0, simp1.length);
                   System.arraycopy(simp2, 1, merged, simp1.length, simp2.length-1);
                   return merged;
               }
           }
           return new TrackPoint[] { points[0], points[points.length-1] };
       }   
   

     
       // Recursive version causes stack overflow on Android
       public static TrackPoint[] douglasPeuckerNonRecursive(TrackPoint[] points, double distMetres)
       {
           ArrayList<IndexedTrackPoint[]> descendant = null, current = new ArrayList<IndexedTrackPoint[]>();
           boolean ended=false;
           TrackPoint[] packedFinalPts = null;
           IndexedTrackPoint[] initial= new IndexedTrackPoint[points.length];
           for(int i=0; i<points.length; i++)
               initial[i] = new IndexedTrackPoint(points[i], i);
           current.add(initial);
          
           int[] starts = {0,0}, ends = {0,0};
          
          
           TrackPoint[] finalPts = new TrackPoint[points.length];
           int selected=0;
           while(!ended)
           {
               descendant = new ArrayList<IndexedTrackPoint[]>();
                    
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
                           if(furthest.maxDist > distMetres)
                           {       
                               descendant.add(Arrays.copyOfRange(current.get(i), starts[j], ends[j]+1));            
                           }
                           else
                           {   
                               IndexedTrackPoint start = current.get(i)[starts[j]], end = current.get(i)[ends[j]];
                               
                               if(finalPts[start.index]==null)
                               {
                                   selected++;
                                   finalPts[start.index] = start;
                               }
                               if(finalPts[end.index]==null)
                               {
                                   selected++;
                                   finalPts[end.index] = end;
                               }
                           }             
                       }
                   }
                   else
                   {
                       for(int j=0; j<2; j++)
                       {
                           if(finalPts[current.get(i)[j].index]==null)
                           {
                               selected++;
                               finalPts[current.get(i)[j].index] = current.get(i)[j];
                           }
                       }
                   }
               }
               ended = descendant.size()==0;
               current = descendant;
           }
           
           packedFinalPts = new TrackPoint[selected];
           int j=0;
           for(int i=0; i<finalPts.length; i++)
               if(finalPts[i]!=null)
                   packedFinalPts[j++] = new TrackPoint(finalPts[i]);
           
           return packedFinalPts;
       }
       
       public static FurthestPoint getFurthest(TrackPoint[] points)
       {
           FurthestPoint p = new FurthestPoint();
           
           double curDist=0.0;
           
           for(int i=1; i<points.length-1; i++)
           {
               curDist = points[i].haversineDistToLine(points[0], points[points.length-1]);
              // System.out.println("getFurthest() points[0] = " + points[0] + " last pt = "+ points[points.length-1] + " point=" + points[i] + " i= " + i + " curDist=" + curDist);
               if(curDist > p.maxDist)
               {
                   p.index = i;
                   p.maxDist = curDist;
               }
           }
         
           if(p.maxDist<=0.0)
           {
               p.index = points.length/2;
               p.maxDist = 0.0;
           }
           return p;
       }
}
