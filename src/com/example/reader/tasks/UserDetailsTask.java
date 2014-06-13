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
				
			
			// REMOVE
			String expToken = "MTQwMjU1MzUyNjMzNjpmNDAyOTRiNzBkYjM5OWY0ZTRkOWU4M2IyNzA4ZDAzN2EyYWJjNzMxOGM1ZmVhMjg1YWVjYWM3ZGQ1ZGM3OGE4ZGQ5ZGJiOGY1NGI3ODBkNTI5ZTRkYTEyYTIyNDhlZjdjYTEwM2RjZDkwMTkwODhhMWY0ZmE1MTcyNWQwYjE5ZDhlZjM5ZDhkNTU5NjcyMWMxNzcyN2ViMjNjYWUyZWZhY2MyODQyM2I5NjBhMDI1Y2QxYmUyNjU4MjFmNzIxM2NjYmQ5MjFjN2M0MDkwNWU4NTUyOTViMDE3OTc4NTk0ZWNjNDAwZWE1OWIxZTMwNWUzYzZlZTFlYWE1NTljMzA2MmQ1NmIyOTY1OWJhZjQ2OWJjYjFjZWZmMDJjZjFmZjc0NTRhZTgxNGM2Mjc2ZTc5ODMzNTY3NmQxNGM3ZDUyNzI5Nzc5YzFmZmIyMmI1ODA5MGQxZmQ3OTcxZWJjYzQwOWE3YTU1MDBlZjFjZTE3ZmUwMWIxZTc5ZGE2NTU0ZjJjZmZiOGVjNzA3ODY4NzNmY2RhZTI4MjJkZjMwOTMyMzRjZDY0OGRiOTdlYzM4OTg4NzhjMmFjYTA5OGZiMDQ3MmE4OWNmNWU2MjhiYTM5NjNiNjRmYWMxOGEzNzc0Nzg0MWI3NzI0ZWQ4MGExNWUxOTJjYWYzNGJjZDJjODVmYjp7InVzZXJOYW1lIjoiam9lX3QiLCJ0ZWFjaGVyIjpudWxsfTo0ZjI4MDJlNTVkNTY3MzM4ZTA1N2NkYTdlMGNiNzkyNmQxYTFmZTE3MmM4ODc0NjQ3M2Q3MTE5M2YxODc2MWZkZWEzMjM3ZDQ3MjRhOGQzNTQ5MGI2NjM0Zjc0NWJjZGY3MjZmMWVmYzFiODk5Y2JkNjE3NjAzOGZhOGIxMjQ3ZA==";
    		editor.putString("authToken", expToken);
    		editor.commit();
			
			
    		Intent i2 = new Intent(context, LibraryActivity.class);
    		context.startActivity(i2);
		} else {
			Log.e(TAG, context.getString(R.string.login_failed_fetching) + " : " + fault);
			Toast.makeText(context, context.getString(R.string.login_failed_fetching), Toast.LENGTH_SHORT).show();
		}
	}
};