package com.example.reader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

public class PreferenceFragmentLibrary extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.library_preferences);
		
		final CheckBoxPreference showAll = (CheckBoxPreference) findPreference("pref_library_show_all");
		showAll.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent();
				i.putExtra("showAll", showAll.isChecked());
				getActivity().setResult(Activity.RESULT_OK, i);
				getActivity().finish();
				return true;
			}
		});
		
		Preference format = (Preference) findPreference("pref_library_clean");
		format.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				final Activity a = getActivity();
				AlertDialog.Builder builder = new AlertDialog.Builder(a);
				builder.setTitle(a.getString(R.string.format_library));
				builder.setPositiveButton(a.getString(android.R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {	
						Intent i = new Intent();
						i.putExtra("format", true);
						a.setResult(Activity.RESULT_OK);
						a.finish();
						return;
					}
				});
				builder.setNegativeButton(a.getString(android.R.string.no), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				});
				
				builder.show();
				
				return true;
			}
		});
		
	}

	
}
