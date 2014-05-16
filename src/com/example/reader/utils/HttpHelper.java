package com.example.reader.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import android.util.Base64;


public class HttpHelper {
	private static final String authString = new String(Base64.encode("api:api".getBytes(), Base64.URL_SAFE|Base64.NO_WRAP));
	
	public static HttpResponse post(String url, String data){
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response = null;
		
		HttpConnectionParams.setSoTimeout(client.getParams(), 25000);
		
		HttpPost post = new HttpPost(url);
		post.setHeader("Accept", "application/json");
		post.setHeader("Authorization", "Basic " + authString);
		post.setHeader("Content-Type", "application/json;charset=utf-8");
		
		try {
			post.setEntity(new StringEntity(data));
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
		
		if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
			HttpEntity entity = response.getEntity();
			
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
				StringBuilder sb = new StringBuilder();
				String line = null;
			
				while ((line = br.readLine()) != null) {
				    sb.append(line + "\r\n");
				}
				
				data.add(response.getStatusLine().toString());
				data.add(sb.toString());
				
			} catch (IOException e) {
				e.printStackTrace();
			} 
			
		} else
			data.add(response.getStatusLine().toString());
		
		return data;
		
	}
	
}
