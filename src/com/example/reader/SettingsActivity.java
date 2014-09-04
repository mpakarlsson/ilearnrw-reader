package com.example.reader;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class SettingsActivity 
	extends 
		Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		
		String setting = getIntent().getExtras().getString("setting", "");
		if(setting.equals("reader")){
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			PreferenceFragmentReader pfr = new PreferenceFragmentReader();
			ft.replace(android.R.id.content, pfr);
			ft.commit();
		}
	}
}
