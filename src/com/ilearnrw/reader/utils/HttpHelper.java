package com.ilearnrw.reader.utils;


import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;

import com.ilearnrw.reader.R;
import com.google.gson.Gson;
import com.ilearnrw.reader.results.TokenResult;
import com.ilearnrw.reader.tasks.LogTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;


public class HttpHelper {
	private static final String authString = new String(Base64.encode("api:api".getBytes(), Base64.URL_SAFE|Base64.NO_WRAP));
	
	public static HttpResponse post(String url, String data){
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response = null;
		
		//HttpConnectionParams.setSoTimeout(client.getParams(), 25000);
		
		HttpPost post = new HttpPost(url);
		post.setHeader("Accept", "application/json");
		post.setHeader("Authorization", "Basic " + authString);
		post.setHeader("Content-Type", "application/json;charset=utf-8");
		
		try {
			post.setEntity(new StringEntity(data, HTTP.UTF_8));
			response = client.execute(post);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return response;
	}
	
	public static HttpResponse get(String url){
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response = null;
		
		HttpConnectionParams.setSoTimeout(client.getParams(), 25000);
		
		HttpGet get = new HttpGet(url);
		get.setHeader("Authorization", "Basic " + authString);
		try {
			response = client.execute(get);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return response;
	}
	
	public static ArrayList<String> handleResponse(HttpResponse response){
		ArrayList<String> data = new ArrayList<String>();
		
		if(response == null){
			data.add("No response");
			return data;
		}
		
		switch (response.getStatusLine().getStatusCode()) {
		case HttpStatus.SC_OK:
			try {
				String str = FileHelper.inputStreamToString(response.getEntity().getContent());
				
				data.add(response.getStatusLine().toString());
				data.add(str);
				
			} catch (IOException e) {
				e.printStackTrace();
			} 
			break;

		case HttpStatus.SC_UNAUTHORIZED:
			try {
				String str = FileHelper.inputStreamToString(response.getEntity().getContent());
				
				if(str.contains("Token expired"))
					data.add("Token expired");
				else
					data.add(response.getStatusLine().toString());
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
			
		case HttpStatus.SC_INTERNAL_SERVER_ERROR:
			Log.e("Internal Server Error", "Internal Server Error");
			data.add(response.getStatusLine().toString());
			break;
		default:
			data.add(response.getStatusLine().toString());
			break;
		}
		
		return data;
	}
	
	public static boolean refreshTokens(Context context){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String refreshToken = preferences.getString(context.getString(R.string.sp_refreshToken), "");
		
		if(refreshToken.isEmpty())
			return false;
		
		HttpResponse refreshResponse = HttpHelper.get("https://ssl.ilearnrw.eu/ilearnrw/user/newtokens?refresh="+refreshToken);
		ArrayList<String> refreshData = HttpHelper.handleResponse(refreshResponse);
		
		if(refreshData == null)
			return false;
		
		if(refreshData.size()>1){
			try {
				TokenResult lr = new Gson().fromJson(refreshData.get(1), TokenResult.class);
				SharedPreferences.Editor editor = preferences.edit();
	    		editor.putString(context.getString(R.string.sp_authToken), lr.authToken);
	    		editor.putString(context.getString(R.string.sp_refreshToken), lr.refreshToken);
	    		editor.apply();
	    		return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			Log.e("Error", refreshData.get(0));
		}
		
		return false;
	}
	
	public static boolean log(Context ctx, String msg, String tag){
		if(msg.trim().isEmpty())
			return false;
		
		if(!isNetworkAvailable(ctx))
			return false;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		String username = prefs.getString(ctx.getString(R.string.sp_user_name), "");
		
		if(username.isEmpty())
			return false;
		
		new LogTask(tag).run(username, msg);
		
		return true;
	}
	
	public static boolean isNetworkAvailable(Context ctx){
		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return (ni != null) && (ni.isConnected());
	}
}
