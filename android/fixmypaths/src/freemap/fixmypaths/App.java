
package freemap.fixmypaths;

import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(formKey = "dGZ6RDJDaWxCMTlfcEJqYTJDRFEtTmc6MQ")

public class App extends android.app.Application {
	
	public void onCreate()
	{
		ACRA.init(this);
		super.onCreate();
	}
}
