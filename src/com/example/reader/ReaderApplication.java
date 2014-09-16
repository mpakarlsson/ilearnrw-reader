package com.example.reader;

import android.app.Application;
import android.preference.PreferenceManager;

public class ReaderApplication extends Application{
	public ReaderApplication(){}

	@Override
	public void onCreate() {
		super.onCreate();
	
		PreferenceManager.getDefaultSharedPreferences(this).edit().remove("isLoggedIn").commit();
	}
	
	
}
