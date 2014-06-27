package com.example.reader.tasks;

import java.util.ArrayList;

import org.apache.http.HttpResponse;

import com.example.reader.R;
import com.example.reader.interfaces.OnHttpListener;
import com.example.reader.interfaces.OnTrickyWordListener;
import com.example.reader.utils.HttpHelper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

public class TrickyWordsTask 
	extends 
		AsyncTask<String, Void, Boolean>{
	
	private ProgressDialog dialog;
	private Context context;
	private Boolean isAdding;
	
	private OnHttpListener httpListener;
	private OnTrickyWordListener trickyListener;
	
	public TrickyWordsTask(Context context, Boolean isAdd, OnHttpListener httpListener, OnTrickyWordListener trickyListener){
		this.context 		= context;
		this.httpListener 	= httpListener;
		this.trickyListener	= trickyListener;
		this.isAdding		= isAdd;
	}
	
	public void run(String... params){
		this.execute(params);
	}
	
	@Override
	protected void onPreExecute() {
		dialog = new ProgressDialog(context);
		if(isAdding){
			dialog.setTitle(context.getString(R.string.dialog_add_tricky_word_title));
			dialog.setMessage(context.getString(R.string.dialog_add_tricky_word_summary));
		} else {
			dialog.setTitle(context.getString(R.string.dialog_remove_tricky_word_title));
			dialog.setMessage(context.getString(R.string.dialog_remove_tricky_word_summary));
		}
		
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				cancel(true);
				dialog.dismiss();
			}
		});
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				cancel(true);
				dialog.dismiss();
			}
		});
		dialog.show();
		
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(String... params) {
		HttpResponse response = null;
		if(isAdding)
			response = HttpHelper.get("http://api.ilearnrw.eu/ilearnrw/profile/trickywords/add?userId=" + params[0] + "&word=" + params[1] + "&token=" + params[2]);
		else 
			response = HttpHelper.get("http://api.ilearnrw.eu/ilearnrw/profile/trickywords/delete?userId=" + params[0] + "&word=" + params[1] + "&token=" + params[2]);

		ArrayList<String> data = HttpHelper.handleResponse(response);
		
		if(data.size()==1){
			if(data.get(0).equals("Token expired"))
				httpListener.onTokenExpired(params);
			return null;
		} else {
			Boolean result = false;
			
			if(data.get(1).equals("ok"))
				result = true;

			return result;
		}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if(dialog.isShowing()){
			dialog.dismiss();
		}
		
		if(result!=null){
			trickyListener.onTrickyWord(result);			
		}
	}

	
}
