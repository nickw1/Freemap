package freemap.routing;

import freemap.data.Point;
import java.util.ArrayList;

public class CountyTracker
{

	CountyManager mgr;
	County curCounty;
	ArrayList<CountyChangeListener> listeners;
	
	public interface CountyChangeListener
	{
		public void onCountyChange (County newCounty);
	}
	
	public CountyTracker(CountyManager mgr)
	{
		this.mgr = mgr;
		listeners = new ArrayList<CountyChangeListener>();
	}
	
	public void addCountyChangeListener (CountyChangeListener listener)
	{
		listeners.add(listener);
	}
	
	public void update (Point p)
	{
		County newCounty = mgr.findCounty(p);
		if(newCounty!=null || curCounty!=null)
		{
			if(newCounty==null || curCounty==null || !newCounty.equals(curCounty))
			{
				curCounty = newCounty;
				for(int i=0; i<listeners.size(); i++)
					listeners.get(i).onCountyChange(newCounty);
			}
		}
	}
}
