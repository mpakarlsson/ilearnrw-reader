package com.example.reader;

import android.app.Activity;
import android.os.Bundle;

public class ReaderSettingsActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new PreferenceFragmentReader()).commit();
	}

	@Override
	protected void onPause() {
	
		int a = 0;
		
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	
	
}
