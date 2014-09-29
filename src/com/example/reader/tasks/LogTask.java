package com.example.reader.tasks;

import java.util.ArrayList;

import org.apache.http.HttpResponse;

import com.example.reader.R;
import com.example.reader.utils.HttpHelper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class LogTask 
	extends 
	AsyncTask<String, Void, String> 
{
	private ProgressDialog dialog;
	private String username, fault;
	private Context context;
	private String TAG;

	public LogTask(Context context){
		this.context = context;
		this.TAG = "";
	}

	public LogTask(Context context, String tag){
		this.context = context;
		this.TAG = tag;
	}

	public void run(String... params){
		this.execute(params);
	}

	@Override
	protected void onPreExecute() {
	dialog = new ProgressDialog(context);
	dialog.setTitle(context.getString(R.string.dialog_log_title));
	dialog.setMessage(context.getString(R.string.dialog_log_summary));
	dialog.setCancelable(true);
	dialog.setCanceledOnTouchOutside(false);
	dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			cancel(true);
			dialog.dismiss();
		}
	});
	dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
		@Override
		public void onCancel(DialogInterface dialog) {
			cancel(true);
			dialog.dismiss();
		}
	});
	dialog.show();
		super.onPreExecute();
	}
	
	@Override
	protected String doInBackground(String... params) {
		username = params[0];
		HttpResponse response = HttpHelper.get("https://ssl.ilearnrw.eu/ilearnrw/user/auth?username="+username+"&pass="+params[1]);
		
		ArrayList<String> data = HttpHelper.handleResponse(response);
		
		if(data.size()==1){
			fault = data.get(0);
			return null;
		} else {
			//TokenResult lr = new Gson().fromJson(data.get(1), TokenResult.class);
			//return lr;
			return "";
		}
	}
	
	@Override
	protected void onPostExecute(String result) {
		if(dialog.isShowing()) {
			dialog.dismiss();
		}
		
		if(result != null){
			
			
		} else{
			Log.e(TAG, context.getString(R.string.dialog_log_failed) + " : " + fault);
			Toast.makeText(context, context.getString(R.string.dialog_log_failed) + " : " + fault, Toast.LENGTH_SHORT).show();
		}
	}
};
