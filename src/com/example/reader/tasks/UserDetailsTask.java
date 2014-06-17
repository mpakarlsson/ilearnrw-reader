package com.example.reader.tasks;

import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.HttpResponse;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.reader.LibraryActivity;
import com.example.reader.R;
import com.example.reader.results.UserDetailResult;
import com.example.reader.utils.HttpHelper;
import com.google.gson.Gson;

public class UserDetailsTask extends AsyncTask<String, Void, UserDetailResult>{
	private ProgressDialog dialog;
	private Context context;
	private String TAG, fault;
	
	public UserDetailsTask(Context context){
		this.context = context;
		this.TAG = "";
	}
	
	public UserDetailsTask(Context context, String tag){
		this.context = context;
		this.TAG = tag;
	}
	
	public void run(String... params){
		this.execute(params);
	}
	
	@Override
	protected void onPreExecute() {
		dialog = new ProgressDialog(context);
		dialog.setTitle(context.getString(R.string.dialog_fetch_user_title));
		dialog.setMessage(context.getString(R.string.dialog_fetch_user_message));
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
	protected UserDetailResult doInBackground(String... params) {
		HttpResponse response = HttpHelper.get("http://api.ilearnrw.eu/ilearnrw/user/details/"+ params[0] +"?token=" + params[1]);
		ArrayList<String> data = HttpHelper.handleResponse(response);
		
		if(data.size()==1){
			fault = data.get(0);			
			return null;
		} else {
			UserDetailResult userDetails = new Gson().fromJson(data.get(1), UserDetailResult.class);
			return userDetails;
		}
	}
	
	@Override
	protected void onPostExecute(UserDetailResult result) {
		if(dialog.isShowing()) {
			dialog.dismiss();
		}
		
		if(result != null){
			Toast.makeText(context, context.getString(R.string.login_succeeded), Toast.LENGTH_SHORT).show();
			
			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
			editor.putInt("id", result.id);
			editor.putString("language", result.language);
			editor.commit();
			
			if(result.language.equals("EN"))
				Locale.setDefault(new Locale("en"));	
			else if(result.language.equals("GR"))
				Locale.setDefault(new Locale("el"));
			else
				Locale.setDefault(new Locale("en"));			
			
    		Intent i2 = new Intent(context, LibraryActivity.class);
    		context.startActivity(i2);
		} else {
			Log.e(TAG, context.getString(R.string.login_failed_fetching) + " : " + fault);
			Toast.makeText(context, context.getString(R.string.login_failed_fetching), Toast.LENGTH_SHORT).show();
		}
	}
};