package com.example.reader;

import android.os.Bundle;

import android.preference.PreferenceFragment;

public class PreferenceFragmentReader extends PreferenceFragment  {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}