package com.example.reader.tasks;

import ilearnrw.annotation.AnnotatedPack;

import java.io.File;
import java.util.ArrayList;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;

import com.example.reader.R;
import com.example.reader.interfaces.OnHttpListener;
import com.example.reader.types.Pair;
import com.example.reader.utils.FileHelper;
import com.example.reader.utils.HttpHelper;
import com.google.gson.Gson;

public class AddToLibraryTask extends AsyncTask<String, Void, Pair<String>>{
	private ProgressDialog dialog;
	private Context context;
	private Activity activity;
	private String filename;
	private String TAG, fault;
	private OnHttpListener listener;
	private WifiManager wifiManager;
	private PowerManager powerManager;
	private WifiLock wifiLock;
	private WakeLock wakeLock;
	
	public AddToLibraryTask(Context context, Activity activity, OnHttpListener listener){
		this.context 	= context;
		this.activity 	= activity;
		this.TAG 		= "";
		this.listener 	= listener;
	}
	
	public AddToLibraryTask(Context context, Activity activity, OnHttpListener listener, String tag){
		this.context 	= context;
		this.activity 	= activity;
		this.TAG 		= tag;
		this.listener 	= listener;
	}
	
	public void run(String... params){
		
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "wifiLock");
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wakeLock");
		
		wifiLock.acquire();
		wakeLock.acquire();
		this.execute(params);
	}

	@Override
	protected void onPreExecute() {
		dialog = new ProgressDialog(context);
		dialog.setTitle(context.getString(R.string.dialog_annotation_title));
		dialog.setMessage(context.getString(R.string.dialog_annotation_message));
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(context, context.getString(R.string.annotation_aborted), Toast.LENGTH_SHORT).show();
				dialog.dismiss();
				if(wakeLock.isHeld()) wakeLock.release();
				if(wifiLock.isHeld()) wifiLock.release();
				cancel(true);
			}
		});
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				Toast.makeText(context, context.getString(R.string.annotation_aborted), Toast.LENGTH_SHORT).show();
				dialog.dismiss();
				if(wakeLock.isHeld()) wakeLock.release();
				if(wifiLock.isHeld()) wifiLock.release();
				cancel(true);
			}
		});
		dialog.show();
		super.onPreExecute();
	}

	@Override
	protected Pair<String> doInBackground(String... params) {
		filename = params[4];
		HttpResponse response = HttpHelper.post("https://ssl.ilearnrw.eu/ilearnrw/text/annotate?userId=" + params[1] + "&lc=" + params[2]+ "&token=" + params[3], params[0]);
		ArrayList<String> data = HttpHelper.handleResponse(response);
		
		if(data.size()==1){
			fault = data.get(0);
			
			if(fault.equals("Token expired")){
				Log.d(TAG, context.getString(R.string.token_expired_refresh));
				Toast.makeText(context, context.getString(R.string.token_expired_refresh), Toast.LENGTH_SHORT).show();
				dialog.dismiss();
				cancel(true);
				listener.onTokenExpired(params[0], params[1], params[2], params[3], params[4]);
			}

			return new Pair<String>(fault, null);
		} else
			return new Pair<String>(data.get(0), data.get(1));
	}
	
	@Override
	protected void onPostExecute(Pair<String> results) {
		if(dialog.isShowing())
			dialog.dismiss();
		
		wifiLock.release();
		wakeLock.release();
		
		if(results.second() != null){
			Toast.makeText(context, context.getString(R.string.annotation_succeeded), Toast.LENGTH_SHORT).show();
			
			Gson gson =  new Gson();
			String json = results.second();
			
			AnnotatedPack result = gson.fromJson(json, AnnotatedPack.class);
			
			int index = filename.lastIndexOf(".");
			String name = filename.substring(0, index);
			File dir = context.getDir(context.getString(R.string.library_location), Context.MODE_PRIVATE);
			
			File newFile = new File(dir, filename);
			FileHelper.saveFile(result.getHtml(), newFile);
			String wordSet = gson.toJson(result.getWordSet());
			File jsonFile = new File(dir, name+".json");
			FileHelper.saveFile(wordSet, jsonFile);

			Intent intent=new Intent();
		    intent.putExtra("file", newFile);
		    intent.putExtra("json", jsonFile);
			intent.putExtra("name", filename);
			activity.setResult(Activity.RESULT_OK, intent);
			activity.finish();
		}  else {
			Log.e(TAG, results.first() + " " + context.getString(R.string.annotation_failed) + " : " + fault);
			Toast.makeText(context, results.first() + " " + context.getString(R.string.annotation_failed), Toast.LENGTH_SHORT).show();
		}
	}
};