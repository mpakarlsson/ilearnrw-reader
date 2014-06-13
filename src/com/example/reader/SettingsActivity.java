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
			ft.replace(android.R.id.content, new PreferenceFragmentReader());
			ft.commit();
		}
		else if(setting.equals("library"))
			getFragmentManager().beginTransaction().replace(android.R.id.content, new PreferenceFragmentLibrary()).commit();
	}
}
