package com.example.reader;

import com.example.reader.utils.AppLocales;

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
		AppLocales.setLocales(getApplicationContext(), sp.getString("language", "en"));
		
		String setting = getIntent().getExtras().getString("setting", "");
		if(setting.equals("reader")){
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			PreferenceFragmentReader pfr = new PreferenceFragmentReader();
			ft.replace(android.R.id.content, pfr);
			ft.commit();
		}
	}
}
