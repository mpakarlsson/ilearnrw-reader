package com.example.reader.tasks;

import java.io.File;
import java.util.ArrayList;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.reader.R;
import com.example.reader.interfaces.OnAsyncTask;
import com.example.reader.results.TextAnnotationResult;
import com.example.reader.utils.FileHelper;
import com.example.reader.utils.HttpHelper;
import com.google.gson.Gson;

public class AddToLibraryTask extends AsyncTask<String, Void, TextAnnotationResult>{
	private ProgressDialog dialog;
	private Context context;
	private Activity activity;
	private String filename;
	private String TAG, fault;
	private OnAsyncTask listener;
	
	
	public AddToLibraryTask(Context context, Activity activity, OnAsyncTask listener){
		this.context 	= context;
		this.activity 	= activity;
		this.TAG 		= "";
		this.listener 	= listener;
	}
	
	public AddToLibraryTask(Context context, Activity activity, OnAsyncTask listener, String tag){
		this.context 	= context;
		this.activity 	= activity;
		this.TAG 		= tag;
		this.listener 	= listener;
	}
	
	public void run(String... params){
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
				cancel(true);
			}
		});
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				Toast.makeText(context, context.getString(R.string.annotation_aborted), Toast.LENGTH_SHORT).show();
				dialog.dismiss();
				cancel(true);
			}
		});
		dialog.show();
		super.onPreExecute();
	}

	@Override
	protected TextAnnotationResult doInBackground(String... params) {
		
		filename = params[4];
		HttpResponse response = HttpHelper.post("http://api.ilearnrw.eu/ilearnrw/text/annotate?userId=" + params[1] + "&lc=" + params[2]+ "&token=" + params[3], params[0]);
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

			return null;
		} else {
			System.out.println(data.get(1));
			TextAnnotationResult result = null;
			try {
				String json = data.get(1);
				result = new Gson().fromJson(json, TextAnnotationResult.class);
			} catch (Exception e) {
				e.printStackTrace();
				result = null;
			}
			
			return result;
		}
	}
	
	
	@Override
	protected void onPostExecute(TextAnnotationResult result) {
		if(dialog.isShowing())
			dialog.dismiss();
		
		if(result != null){
			
			Toast.makeText(context, context.getString(R.string.annotation_succeeded), Toast.LENGTH_SHORT).show();
			
			Gson gson =  new Gson();
			int index = filename.lastIndexOf(".");
			String name = filename.substring(0, index);
			File dir = context.getDir(context.getString(R.string.library_location), Context.MODE_PRIVATE);
			
			File newFile = new File(dir, filename);
			FileHelper.saveFile(result.html, newFile);
			String wordSet = gson.toJson(result.wordSet);
			File jsonFile = new File(dir, name+".json");
			FileHelper.saveFile(wordSet, jsonFile);

			Intent intent=new Intent();
		    intent.putExtra("file", newFile);
		    intent.putExtra("json", jsonFile);
			intent.putExtra("name", filename);
			activity.setResult(Activity.RESULT_OK, intent);
			activity.finish();
		}  else {
			Log.e(TAG, context.getString(R.string.annotation_failed) + " : " + fault);
			Toast.makeText(context, context.getString(R.string.annotation_failed), Toast.LENGTH_SHORT).show();
		}
	}
	
};