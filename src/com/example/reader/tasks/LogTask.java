package com.example.reader.tasks;

import java.sql.Timestamp;
import java.util.Date;

import com.example.reader.types.LogBasicExclusionStrategy;
import com.example.reader.types.LogEntry;
import com.example.reader.utils.HttpHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.os.AsyncTask;

public class LogTask 
	extends 
	AsyncTask<String, Void, String> 
{
	private String username;
	private String TAG;
	private String applicationId;

	public LogTask(){
		this.TAG = "";
		this.applicationId = "READER";
	}

	public LogTask(String tag){
		this.TAG = tag;
		this.applicationId = "READER";
	}

	/**
	 * Method takes two parameters in specified order.
	 * @param params - username & log value
	 */
	public void run(String... params){
		this.execute(params);
	}

	@Override
	protected String doInBackground(String... params) {
		username = params[0];
		String value = params[1];
		
		Gson gson;
		try {
			gson = new GsonBuilder()
					.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
					.setExclusionStrategies(new LogBasicExclusionStrategy("problemCategory", "problemIndex", "duration"))
					.create();
		} catch (Exception e) {
			e.printStackTrace();
			gson = new GsonBuilder()
			.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
			.create();
		} 
		Date date = new Date();
		Timestamp timestamp =  new Timestamp(date.getTime());

		LogEntry entry = new LogEntry(
				username, 
				applicationId,
				timestamp,
				TAG,
				value
				);
		
		String json =  gson.toJson(entry);
		String data = json;
		
		HttpHelper.post("https://ssl.ilearnrw.eu/ilearnrw/logs", data);
		return "";
	}
};
