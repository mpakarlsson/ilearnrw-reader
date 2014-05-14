package com.example.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;

public class HttpConnection implements Runnable {

	public static final int CONNECTION_START = 0;
	public static final int CONNECTION_ERROR = 1;
	public static final int CONNECTION_RESPONSE_ERROR = 2;
	public static final int CONNECTION_SUCCEED = 3;
	
	private static final int GET 	= 0;
	private static final int POST 	= 1;
	private static final int PUT	= 2;
	private static final int DELETE = 3;
	private static final int BITMAP	= 4;
	
	private String url;
	private int method;
	private Handler handler;
	private String data;
	private String authString;
	InputStream is;
	
	private DefaultHttpClient httpClient;
	
	public HttpConnection(){
		this(new Handler());
	}
	
	public HttpConnection(Handler handler){
		this.handler = handler;
	}
	

	public void create(int method, String url, String data){
		this.method 	= method;
		this.url 		= url;
		this.data 		= data;
		this.authString = new String(Base64.encode("api:api".getBytes(), Base64.URL_SAFE|Base64.NO_WRAP));
		ConnectionManager.getInstance().push(this);
	}
	
	public void get(String url){
		create(GET, url, null);
	}
	
	public void post(String url, String data){
		create(POST, url, data);
	}
	
	public void put(String url, String data){
		create(PUT, url, data);
	}
	
	public void delete(String url){
		create(DELETE, url, null);
	}
	
	public void bitmap(String url){
		create(BITMAP, url, null);
	}
	
	@Override
	public void run() {
		handler.sendMessage(Message.obtain(handler, HttpConnection.CONNECTION_START));
		httpClient = new DefaultHttpClient();
		
		// TODO: HTTPS, this is not available yet on the server, must wait until it is
		// http://developer.android.com/training/articles/security-ssl.html
		
		
		HttpConnectionParams.setSoTimeout(httpClient.getParams(), 25000);
		
		try {
			HttpResponse response = null;
			
			switch (method) {
			case GET:
				HttpGet httpGet = new HttpGet(url);
				httpGet.setHeader("Authorization", "Basic " + authString);
				response = httpClient.execute(httpGet);
				break;
				
			case POST:
				HttpPost httpPost = new HttpPost(url);
				httpPost.setHeader("Accept", "application/json");
				httpPost.setHeader("Authorization", "Basic " + authString);
				httpPost.setHeader("Content-Type", "application/json;charset=utf-8");
				httpPost.setEntity(new StringEntity(data));
				response = httpClient.execute(httpPost);
				break;
				
			case PUT:
				HttpPut httpPut = new HttpPut(url);
				httpPut.setEntity(new StringEntity(data));
				httpPut.setHeader("Authorization", "Basic " + authString);
				response = httpClient.execute(httpPut);
				break;
				
			case DELETE:
				HttpGet httpGetDelete = new HttpGet(url);
				httpGetDelete.setHeader("Authorization", "Basic " + authString);
				response = httpClient.execute(httpGetDelete);
				break;
				
			case BITMAP:
				HttpGet httpGetBitmap = new HttpGet(url);
				httpGetBitmap.setHeader("Authorization", "Basic " + authString);
				response = httpClient.execute(httpGetBitmap);
				
				int statusCode = response.getStatusLine().getStatusCode();
				if(statusCode == HttpStatus.SC_OK){
					processBitmapEntity(response.getEntity());
				} else {
					String statusMessage = "Wrong status code.\n Wanted: 200 \n Got: " + Integer.toString(statusCode);
					handler.sendMessage(Message.obtain(handler, HttpConnection.CONNECTION_RESPONSE_ERROR, statusMessage));
				}
				
				break;
			}
			
			if(method<BITMAP){
				// TODO: handle more than just 200
				int statusCode = response.getStatusLine().getStatusCode();
				if(statusCode == HttpStatus.SC_OK){
					processEntity(response.getEntity());
				} else {
					String statusMessage = "Wrong status code.\n Wanted: 200 \n Got: " + Integer.toString(statusCode);
					handler.sendMessage(Message.obtain(handler, HttpConnection.CONNECTION_RESPONSE_ERROR, statusMessage));
				}
				
			}
			
		} catch (Exception e) {
			handler.sendMessage(Message.obtain(handler, HttpConnection.CONNECTION_ERROR, e));
		}
		ConnectionManager.getInstance().didComplete(this);
	}
	
	private void processEntity(HttpEntity entity) throws IllegalStateException, IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
		
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = br.readLine()) != null) {
		    sb.append(line + "\r\n");
		}
		
		Message message = Message.obtain(handler, CONNECTION_SUCCEED, sb.toString());
		handler.sendMessage(message);
	}
	
	private void processBitmapEntity(HttpEntity entity) throws IOException {
		BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
		Bitmap bm = BitmapFactory.decodeStream(bufHttpEntity.getContent());
		handler.sendMessage(Message.obtain(handler, CONNECTION_SUCCEED, bm));
	}	
}
