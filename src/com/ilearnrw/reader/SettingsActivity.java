package com.ilearnrw.reader;

import com.ilearnrw.reader.R;
import com.ilearnrw.reader.utils.AppLocales;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class SettingsActivity 
	extends 
		Activity {
	private SharedPreferences sp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		
        sp = PreferenceManager.getDefaultSharedPreferences(this);
		AppLocales.setLocales(getApplicationContext(), sp.getString(getString(R.string.sp_user_language), "en"));
		
		String setting = getIntent().getExtras().getString("setting", "");
		if(setting.equals("reader")){
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			PreferenceFragmentReader pfr = new PreferenceFragmentReader();
			ft.replace(android.R.id.content, pfr);
			ft.commit();
		}
	}
}