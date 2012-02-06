package freemap.hikar;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.content.Intent;
import android.widget.TextView;

public class DownloadDataActivity extends Activity implements Button.OnClickListener{
	
	Button b,cb;
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.downloaddataui);
		 b = (Button)findViewById(R.id.downloadDataOK);
		 cb=(Button)findViewById(R.id.downloadDataCancel);
		b.setOnClickListener(this);
		cb.setOnClickListener(this);
		
	}
	
	public void onClick(View view)
	{
		Intent intent = new Intent();
		if(view==b)
		{
			Bundle extras=new Bundle();
			TextView
				tvEast = (TextView)findViewById(R.id.downloadDataEast),
				tvNorth = (TextView)findViewById(R.id.downloadDataNorth),
				tvWest = (TextView)findViewById(R.id.downloadDataWest),
				tvSouth = (TextView)findViewById(R.id.downloadDataSouth);
		
			extras.putDouble("freemap.opentrail.east",Double.parseDouble(tvEast.getText().toString()));
			extras.putDouble("freemap.opentrail.north",Double.parseDouble(tvNorth.getText().toString()));
			extras.putDouble("freemap.opentrail.west",Double.parseDouble(tvWest.getText().toString()));
			extras.putDouble("freemap.opentrail.south",Double.parseDouble(tvSouth.getText().toString()));
		
			intent.putExtras(extras);
			
			setResult(RESULT_OK,intent);
		}
		else
			setResult(RESULT_CANCELED,intent);
		finish();
	}
}
