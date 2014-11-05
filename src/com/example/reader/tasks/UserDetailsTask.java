package com.example.reader.tasks;

import java.util.ArrayList;

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
import com.example.reader.interfaces.OnHttpListener;
import com.example.reader.interfaces.OnProfileFetched;
import com.example.reader.results.UserDetailResult;
import com.example.reader.types.SystemTags;
import com.example.reader.utils.AppLocales;
import com.example.reader.utils.HttpHelper;
import com.google.gson.Gson;

public class 
		UserDetailsTask 
	extends 
		AsyncTask<String, Void, UserDetailResult>
	implements
		OnHttpListener,
		OnProfileFetched{
	private ProgressDialog dialog;
	private Context context;
	private String TAG, fault;
	private SharedPreferences sp;
	private String username;
	
	public UserDetailsTask(Context context, String tag){
		this.context 		= context;
		this.TAG 			= tag;
		sp 					= PreferenceManager.getDefaultSharedPreferences(context);
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
		username = params[0];
		HttpResponse response = HttpHelper.get("https://ssl.ilearnrw.eu/ilearnrw/user/details/"+ username +"?token=" + params[1]);
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
			
			SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
			edit.putInt(context.getString(R.string.sp_user_id), result.id);
			edit.putString(context.getString(R.string.sp_user_language), result.language);
			edit.commit();

			AppLocales.setLocales(context, result.language);
			new ProfileTask(context, this, this).run(Integer.toString(result.id), sp.getString(context.getString(R.string.sp_authToken), ""));
		} else {
			Log.e(TAG, context.getString(R.string.login_failed_fetching) + " : " + fault);
			Toast.makeText(context, context.getString(R.string.login_failed_fetching), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onProfileFetched(String profile) {
		new LogTask(SystemTags.APP_SESSION_START).run(username, "Logged in: " + username + ".");
		sp.edit().putString(context.getString(R.string.sp_user_profile_json), profile).commit();
		sp.edit().putBoolean(context.getString(R.string.sp_user_is_logged_in), true).commit();
		Intent i2 = new Intent(context, LibraryActivity.class);
		context.startActivity(i2);
	}

	@Override
	public void onTokenExpired(String... params) {
		// Token should not be expired here, if it is then something is wrong with its lifetime
		if(HttpHelper.refreshTokens(context)){
			final String newToken = sp.getString(context.getString(R.string.sp_authToken), "");
			new ProfileTask(context, this, this).run(params[0], newToken);
			
			Log.d(TAG, context.getString(R.string.token_error_retry));
			Toast.makeText(context, context.getString(R.string.token_error_retry), Toast.LENGTH_SHORT).show();
		}
	}
};