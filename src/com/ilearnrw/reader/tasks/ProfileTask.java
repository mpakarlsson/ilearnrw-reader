package com.ilearnrw.reader.tasks;

import java.util.ArrayList;

import org.apache.http.HttpResponse;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.ilearnrw.reader.R;
import com.ilearnrw.reader.interfaces.OnHttpListener;
import com.ilearnrw.reader.interfaces.OnProfileFetched;
import com.ilearnrw.reader.utils.HttpHelper;

public class ProfileTask extends AsyncTask<String, Void, String>{
	private ProgressDialog dialog;
	private Context context;
	private OnProfileFetched profileListener;
	private OnHttpListener asyncListener;
	
	public ProfileTask(Context context, OnHttpListener asyncListener, OnProfileFetched profileListener){
		this.context 			= context;
		this.profileListener 	= profileListener;
		this.asyncListener		= asyncListener;		
	}
	
	public void run(String... params){
		this.execute(params);
	}
	
	@Override
	protected void onPreExecute() {
		dialog = new ProgressDialog(context);
		dialog.setTitle(context.getString(R.string.dialog_fetch_user_profile_title));
		dialog.setMessage(context.getString(R.string.dialog_fetch_user_profile_summary));
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
		HttpResponse response = HttpHelper.get("https://ssl.ilearnrw.eu/ilearnrw/profile?userId=" + params[0] + "&token=" + params[1]);
		ArrayList<String> data = HttpHelper.handleResponse(response);
		
		if(data.size()==1){
			if(data.get(0).equals("Token expired"))
				asyncListener.onTokenExpired(params[0], params[1]);
			return null;
		} else {
			return data.get(1);
		}
	}
	
	@Override
	protected void onPostExecute(String result) {
		if(dialog.isShowing()) {
			dialog.dismiss();
		}
		if(result != null){
			profileListener.onProfileFetched(result);
		}
	}
}