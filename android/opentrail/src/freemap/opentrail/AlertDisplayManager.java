package freemap.opentrail;

import freemap.data.Annotation;
import freemap.datasource.FreemapDataset;
import freemap.data.Point;
import freemap.data.Walkroute;

public class AlertDisplayManager {
	
	private static AlertDisplayManager instance;
	private Annotation prevAnnotation;
	double distLimit;
	AlertDisplay display;
	FreemapDataset pois;
	Walkroute walkroute;
	Walkroute.Stage prevStage;
	
	public static AlertDisplayManager getInstance(AlertDisplay display, double distLimit)
	{
		if(instance==null)
			instance = new AlertDisplayManager(display,distLimit);
		return instance;
	}
	
	private AlertDisplayManager( AlertDisplay display,double distLimit)
	{
		this.distLimit = distLimit;
		this.display = display;
	}

	public void setWalkroute(Walkroute walkroute)
	{
		this.walkroute = walkroute;
	}
	
	public void setPOIs(FreemapDataset pois)
	{
		this.pois = pois;
	}
	
	public void update(Point lonLat)
	{
		if(pois!=null)
		{
			Annotation newAnnotation = pois.findNearestAnnotation(lonLat, distLimit, null);
			if(newAnnotation != null)
			{
				if(prevAnnotation==null || newAnnotation.getId() != prevAnnotation.getId())
				{
					display.displayAnnotationInfo(prevAnnotation.getDescription());
				}
				prevAnnotation = newAnnotation;
			}
		}
		if(walkroute!=null)
		{
			Walkroute.Stage newStage = walkroute.findNearestStage(lonLat, distLimit);
			if(newStage != null)
			{		
				if (prevStage==null || newStage.id != prevStage.id)
				{
					display.displayAnnotationInfo(newStage.description);
					prevStage = newStage;
				}
				
			}
		}
	}
}
