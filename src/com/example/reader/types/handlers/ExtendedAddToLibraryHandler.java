package com.example.reader.types.handlers;

import java.io.File;
import java.lang.ref.WeakReference;

import com.example.reader.AddToLibraryExplorerActivity;
import com.example.reader.HttpConnection;
import com.example.reader.R;
import com.example.reader.serveritems.TextAnnotationResult;
import com.example.reader.utils.FileHelper;
import com.google.gson.Gson;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class ExtendedAddToLibraryHandler extends Handler {

	private final WeakReference<AddToLibraryExplorerActivity> activity;
	private final Context context;
	private String filename;
	
	public ExtendedAddToLibraryHandler(AddToLibraryExplorerActivity activity, Context context, String filename){
		this.activity 	= new WeakReference<AddToLibraryExplorerActivity>(activity);
		this.context 	= context;
		this.filename 	= filename;
	}

	@SuppressLint("NewApi")
	@Override
	public void handleMessage(Message msg) {
		AddToLibraryExplorerActivity a = activity.get();
		
		if(a!=null){
			switch(msg.what){
			case HttpConnection.CONNECTION_START:
				Log.d("TextAnnotation", "Start");
				break;
			
			case HttpConnection.CONNECTION_SUCCEED:
				Toast.makeText(context, "Annotation successful", Toast.LENGTH_SHORT).show();
				Log.d("TextAnnotation", "Succeeded");
				String response = (String) msg.obj;
				
				Gson gson = new Gson();
				TextAnnotationResult text = gson.fromJson(response, TextAnnotationResult.class);
				
				int index = filename.lastIndexOf(".");
				String name = filename.substring(0, index);
				File dir = a.getDir(a.getString(R.string.library_location), Context.MODE_PRIVATE);
				
				File newFile = new File(dir, filename);
				FileHelper.saveFile(text.html, newFile);
				String wordSet = gson.toJson(text.wordSet);
				File jsonFile = new File(dir, name+".json");
				FileHelper.saveFile(wordSet, jsonFile);

				Intent intent=new Intent();
			    intent.putExtra("file", newFile);
			    intent.putExtra("json", jsonFile);
				intent.putExtra("name", filename);
				a.setResult(Activity.RESULT_OK, intent);
				a.finish();
				break;
				
			case HttpConnection.CONNECTION_RESPONSE_ERROR:
				Toast.makeText(context, "Server annotation failed, " + msg.toString(), Toast.LENGTH_SHORT).show();
				Log.e("TextAnnotation", "Failed due to response error");
				break;
				
			case HttpConnection.CONNECTION_ERROR:
				Toast.makeText(context, "Server annotation error, " + msg.toString(), Toast.LENGTH_SHORT).show();
				Log.e("TextAnnotation", "Failed due to error");
				break;
			}
		}
	}
	
}
