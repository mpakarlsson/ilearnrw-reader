package com.example.reader;

import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		
		String setting = getIntent().getExtras().getString("setting", "");
		if(setting.equals("reader"))
			getFragmentManager().beginTransaction().replace(android.R.id.content, new PreferenceFragmentReader()).commit();
		else if(setting.equals("library"))
			getFragmentManager().beginTransaction().replace(android.R.id.content, new PreferenceFragmentLibrary()).commit();
	}
}
