package com.example.reader.types.handlers;

import java.lang.ref.WeakReference;

import com.example.reader.HttpConnection;
import com.example.reader.LoginActivity;
import com.example.reader.serveritems.LoginResult;
import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class ExtendedLoginHandler extends Handler {
	private final WeakReference<LoginActivity> activity;
	private final Context context;
	private final String username;
	
	public ExtendedLoginHandler(LoginActivity activity, Context context, String username){
		this.activity = new WeakReference<LoginActivity>(activity);
		this.context = context;
		this.username = username;
	}

	@Override
	public void handleMessage(Message msg) {
		LoginActivity a = activity.get();
		if(a!=null){
			switch (msg.what) {
        	case HttpConnection.CONNECTION_START: {
        		Log.d("Login", "Starting connection...");
        		Toast.makeText(context, "Connecting...", Toast.LENGTH_SHORT).show();
        		break;
        	}
        	case HttpConnection.CONNECTION_SUCCEED: {
        		String response = (String) msg.obj;
        		Log.d("Login", response);
        		
        		LoginResult lr = new Gson().fromJson(response, LoginResult.class);
        		
        		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        		editor.putString("authToken", lr.authToken);
        		editor.putString("refreshToken", lr.refreshToken);
        		editor.commit();
        		
        		new HttpConnection(new ExtendedUserDataHandler(a, context)).get("http://api.ilearnrw.eu/ilearnrw/user/details/"+ username +"?token=" + lr.authToken);
        		
        		break;
        	}
        	case HttpConnection.CONNECTION_ERROR: { 
        		Exception e = (Exception) msg.obj;
        		e.printStackTrace();
        		Log.e("Login", "Connection failed.");
        		Toast.makeText(context, "Login error", Toast.LENGTH_SHORT).show();
        		break;
        	}
        	case HttpConnection.CONNECTION_RESPONSE_ERROR: {
        		String s = (String) msg.obj;
        		Log.e("Login", s);
        		Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show();
        		break;
        	}
    	}
		}
	}
	
}
