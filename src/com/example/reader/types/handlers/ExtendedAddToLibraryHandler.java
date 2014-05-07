package com.example.reader.types.handlers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;

import com.example.reader.AddToLibraryExplorerActivity;
import com.example.reader.HttpConnection;
import com.example.reader.R;
import com.example.reader.serveritems.TextAnnotationResult;
import com.example.reader.utils.FileHelper;
import com.google.gson.Gson;

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
	private final File file;
	private String filename;
	
	public ExtendedAddToLibraryHandler(AddToLibraryExplorerActivity activity, Context context, File file, String filename){
		this.activity 	= new WeakReference<AddToLibraryExplorerActivity>(activity);
		this.context 	= context;
		this.file 		= file;
		this.filename 	= filename;
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
				
				try {
					Toast.makeText(context, "Annotation successful", Toast.LENGTH_SHORT).show();
					
					InputStream is = new ByteArrayInputStream(text.html.getBytes("UTF-8"));
					File dir = a.getDir(context.getString(R.string.library_location), Context.MODE_PRIVATE);
					FileHelper.WriteFileToDirectory(is, filename, dir);
					is.close();
					
					Intent intent=new Intent();
				    intent.putExtra("file", file);
					intent.putExtra("name", filename);
					a.setResult(Activity.RESULT_OK, intent);
					a.finish();
					return;
					
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				break;
				
			case HttpConnection.CONNECTION_RESPONSE_ERROR:
				Log.e("TextAnnotation", "Failed due to response error");
				Toast.makeText(context, "Server annotation failed", Toast.LENGTH_SHORT).show();
				break;
				
			case HttpConnection.CONNECTION_ERROR:
				Log.e("TextAnnotation", "Failed due to error");
				Toast.makeText(context, "Server annotation error", Toast.LENGTH_SHORT).show();
				break;
				
			}
		}
	
	}
	
	
}
