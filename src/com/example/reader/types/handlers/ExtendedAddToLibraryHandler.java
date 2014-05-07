package com.example.reader.types.handlers;

import java.lang.ref.WeakReference;

import com.example.reader.AddToLibraryExplorerActivity;
import com.example.reader.HttpConnection;
import com.example.reader.serveritems.TextAnnotationResult;
import com.google.gson.Gson;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ExtendedAddToLibraryHandler extends Handler {

	private final WeakReference<AddToLibraryExplorerActivity> activity;
	
	public ExtendedAddToLibraryHandler(AddToLibraryExplorerActivity activity){
		this.activity = new WeakReference<AddToLibraryExplorerActivity>(activity);
	}

	@Override
	public void handleMessage(Message msg) {
		AddToLibraryExplorerActivity a = activity.get();
		
		if(a!=null){
			switch(msg.what){
			case HttpConnection.CONNECTION_START:
				Log.d("TextAnnotation", "Start");
				break;
			
			case HttpConnection.CONNECTION_SUCCEED:
				Log.d("TextAnnotation", "Succeeded");
				String response = (String) msg.obj;
				TextAnnotationResult text = new Gson().fromJson(response, TextAnnotationResult.class);
				// Save file
				
				break;
				
			case HttpConnection.CONNECTION_RESPONSE_ERROR:
				Log.e("TextAnnotation", "Failed due to response error");
				break;
				
			case HttpConnection.CONNECTION_ERROR:
				Log.e("TextAnnotation", "Failed due to error");
				break;
				
			}
		}
	
	}
	
	
}
