package freemap.fixmypaths;

import freemap.andromaps.LocationDisplayer;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.ArrayItemizedOverlay;
import org.mapsforge.android.maps.overlay.ItemizedOverlay;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.core.GeoPoint;
import freemap.data.Annotation;
import android.util.Log;

public class DataDisplayer extends LocationDisplayer implements
	freemap.datasource.FreemapDataset.AnnotationVisitor
{

	Drawable problemIcon;
	SparseArray<Annotation> indexedProblems;
	
	public DataDisplayer(Context ctx, MapView mv, Drawable locationIcon, Drawable problemIcon)
	{
		super(ctx,mv,locationIcon);
		this.problemIcon=problemIcon;
		indexedProblems=new SparseArray<Annotation>();
	}
	
	public void visit(Annotation ann)
	{
		Log.d("OpenHants","visiting annotaiton with id="+ ann.getId());
		if(indexedProblems.get(ann.getId())==null)
		{
			Log.d("OpenHants","Adding overlay item: " + ann.getPoint());
			overlay.addItem(new OverlayItem(new GeoPoint(ann.getPoint().y,ann.getPoint().x),
					"Problem #" + ann.getId(),ann.getDescription(),
					ItemizedOverlay.boundCenter(problemIcon)));
			indexedProblems.put(ann.getId(), ann);
		}
	}
	
	public void requestRedraw()
	{
		overlay.requestRedraw();
	}
	
	public void clear()
	{
		overlay.clear();
		indexedProblems.clear();
	}
}
