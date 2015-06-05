package com.ilearnrw.reader;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
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
