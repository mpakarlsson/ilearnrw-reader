package com.ilearnrw.reader;

import com.ilearnrw.reader.R;

import android.app.Application;
import android.preference.PreferenceManager;

public class ReaderApplication extends Application{
	public ReaderApplication(){}

	@Override
	public void onCreate() {
		super.onCreate();
	
		PreferenceManager.getDefaultSharedPreferences(this).edit().remove(getString(R.string.sp_user_is_logged_in)).apply();
	}
	
	
}
