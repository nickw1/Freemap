
package freemap.opentrail;

import org.acra.*;
import org.acra.annotation.*;
import freemap.datasource.FreemapDataset;
import java.util.ArrayList;
import freemap.data.Walkroute;

@ReportsCrashes(formKey = "dGZ6RDJDaWxCMTlfcEJqYTJDRFEtTmc6MQ")

public class App extends android.app.Application {
	
	public void onCreate()
	{
		ACRA.init(this);
		super.onCreate();
	}
}
