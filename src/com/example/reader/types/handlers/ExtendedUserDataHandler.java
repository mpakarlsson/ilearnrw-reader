package com.example.reader.types.handlers;

import java.lang.ref.WeakReference;
import java.util.Locale;

import com.example.reader.HttpConnection;
import com.example.reader.LibraryActivity;
import com.example.reader.LoginActivity;
import com.example.reader.serveritems.UserDetailResult;
import com.google.gson.Gson;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class ExtendedUserDataHandler extends Handler {

	private final WeakReference<LoginActivity> activity;
	private final Context context;
	
	public ExtendedUserDataHandler(LoginActivity activity, Context context){
		this.activity = new WeakReference<LoginActivity>(activity);
		this.context = context;
	}

	@Override
	public void handleMessage(Message msg) {
		LoginActivity a = activity.get();
		
		if(a!=null){
			switch(msg.what){
			case HttpConnection.CONNECTION_START:
				Log.d("UserDetails", "Fetching user info");
				break;
				
			case HttpConnection.CONNECTION_SUCCEED:
				Log.d("UserDetails", "Fetching user info succeeded");
				Toast.makeText(context, "Login succeeded", Toast.LENGTH_SHORT).show();
				
				String response = (String) msg.obj;
				UserDetailResult userDetails = new Gson().fromJson(response, UserDetailResult.class);
				
				SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).edit();
				editor.putInt("id", userDetails.id);
				editor.putString("language", userDetails.language);
				editor.commit();
				
				if(userDetails.language.equals("EN"))
					Locale.setDefault(new Locale("en"));	
				else if(userDetails.language.equals("GR"))
					Locale.setDefault(new Locale("el"));
				else
					Locale.setDefault(new Locale("en"));
					
        		Intent i2 = new Intent(context, LibraryActivity.class);
        		a.startActivity(i2);
        		
				break;
				
			case HttpConnection.CONNECTION_ERROR:
				Log.e("UserDetails", "Getting user info failed");
				Toast.makeText(context, "Login, fetching user data, error", Toast.LENGTH_SHORT).show();
				break;
				
			case HttpConnection.CONNECTION_RESPONSE_ERROR:
				Log.e("UserDetails", "Getting user info status fail");
				Toast.makeText(context, "Login, fetching user data, failed", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}
}
