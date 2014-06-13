package com.example.reader.tasks;

import java.util.ArrayList;

import org.apache.http.HttpResponse;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.example.reader.R;
import com.example.reader.interfaces.OnAsyncTask;
import com.example.reader.interfaces.OnProfileFetched;
import com.example.reader.results.ProfileResult;
import com.example.reader.utils.HttpHelper;
import com.google.gson.Gson;

public class ProfileTask extends AsyncTask<String, Void, ProfileResult>{
	private ProgressDialog dialog;
	private Context context;
	private OnProfileFetched profileListener;
	private OnAsyncTask asyncListener;
	
	public ProfileTask(Context context, OnAsyncTask asyncListener, OnProfileFetched profileListener){
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
		dialog.setTitle(context.getString(R.string.dialog_fetch_user_problems_title));
		dialog.setMessage(context.getString(R.string.dialog_fetch_user_problems_summary));
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
	protected ProfileResult doInBackground(String... params) {
		HttpResponse response = HttpHelper.get("http://api.ilearnrw.eu/ilearnrw/profile?userId=" + params[0] + "&token=" + params[1]);
		ArrayList<String> data = HttpHelper.handleResponse(response);
		
		if(data.size()==1){
			if(data.get(0).equals("Token expired"))
				asyncListener.onTokenExpired(params[0], params[1]);
			return null;
		} else {
			ProfileResult result = null;
			try {
				result = new Gson().fromJson(data.get(1), ProfileResult.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}
	}
	
	@Override
	protected void onPostExecute(ProfileResult result) {
		if(dialog.isShowing()) {
			dialog.dismiss();
		}
		
		if(result != null){
			profileListener.onProfileFetched(result);
		}
	}
}