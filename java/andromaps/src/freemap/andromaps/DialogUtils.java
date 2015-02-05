package freemap.andromaps;

import android.app.AlertDialog;
import android.content.Context;

public class DialogUtils {

	public static void showDialog(Context ctx, String msg)
	{
		new AlertDialog.Builder(ctx).setMessage(msg).
			setPositiveButton("OK",null).show();
	}
	
	public static void showCancelDialog(Context ctx, String msg)
	{
		new AlertDialog.Builder(ctx).setMessage(msg).
			setPositiveButton("OK",null).
			setNegativeButton("Cancel",null).show();
	}
}
