package com.example.reader;

import java.io.IOException;
import java.util.Locale;

import com.example.reader.types.ColorPickerPreference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

public class PreferenceFragmentReader extends PreferenceFragment implements OnPreferenceChangeListener {	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		String[] fonts = {};
		try {
			fonts = getActivity().getAssets().list("custom-fonts");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		Activity a = getActivity();
		
		ListPreference fontFace = (ListPreference) findPreference(a.getString(R.string.pref_font_face_title));
		
		int numBasicFonts = 3;
		CharSequence[] entries = new CharSequence[fonts.length+numBasicFonts];
		CharSequence[] values = new CharSequence[fonts.length+numBasicFonts];
		
		entries[0] = (CharSequence) "Sans-Serif";
		values[0] = (CharSequence) "sans-serif";
		
		entries[1] = (CharSequence) "Serif";
		values[1] = (CharSequence) "serif";
		
		entries[2] = (CharSequence) "Monospace";
		values[2] = (CharSequence) "monospace";
		
		for(int i=0; i<fonts.length; i++){
			String name = fonts[i].substring(0, fonts[i].lastIndexOf(".")).toLowerCase(Locale.getDefault());
			name = name.replace("_", " ");
			name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
			entries[i+numBasicFonts] = (CharSequence) name;
			values[i+numBasicFonts] = (CharSequence) fonts[i];
		}
		
		fontFace.setEntries(entries);
		fontFace.setEntryValues(values);
		if(fontFace.getValue() == null)
			fontFace.setValueIndex(0);
		
		ColorPickerPreference backgroundPicker = (ColorPickerPreference)findPreference(a.getString(R.string.pref_background_color_title));
		ColorPickerPreference textPicker = (ColorPickerPreference)findPreference(a.getString(R.string.pref_text_color_title));
		
		ListPreference languages = (ListPreference)findPreference(a.getString(R.string.pref_tts_language_title));
		if(languages.getValue() == null)
			languages.setValueIndex(0);
		/*if(changed){
			Intent i = new Intent();
			i.putExtra("changed", changed);
			getActivity().setResult(Activity.RESULT_OK, i);
			getActivity().finish();
		}*/
		fontFace.setOnPreferenceChangeListener(this);
		backgroundPicker.setOnPreferenceChangeListener(this);
		textPicker.setOnPreferenceChangeListener(this);
		languages.setOnPreferenceChangeListener(this);
		
		Preference format = (Preference) findPreference("pref_reader_reset");
		format.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				final Activity a = getActivity();
				AlertDialog.Builder builder = new AlertDialog.Builder(a);
				builder.setTitle(a.getString(R.string.pref_reader_reset));
				builder.setNegativeButton(a.getString(android.R.string.no), null);
				builder.setPositiveButton(a.getString(android.R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {	
						Editor edit = PreferenceManager.getDefaultSharedPreferences(a.getBaseContext()).edit();
						edit.remove(getString(R.string.pref_background_color_title));
						edit.remove(getString(R.string.pref_background_color_posX_title));
						edit.remove(getString(R.string.pref_background_color_posY_title));
						edit.remove(getString(R.string.pref_text_color_title));
						edit.remove(getString(R.string.pref_text_color_posX_title));
						edit.remove(getString(R.string.pref_text_color_posY_title));
						edit.remove(getString(R.string.pref_highlight_color_title));
						edit.remove(getString(R.string.pref_highlight_color_posX_title));
						edit.remove(getString(R.string.pref_font_size_title));
						edit.remove(getString(R.string.pref_font_face_title));
						edit.remove(getString(R.string.pref_line_height_title));
						edit.remove(getString(R.string.pref_letter_spacing_title));
						edit.remove(getString(R.string.pref_margin_title));
						edit.remove(getString(R.string.pref_speech_rate_title));
						edit.remove(getString(R.string.pref_pitch_title));
						edit.remove(getString(R.string.pref_tts_language_title));
						edit.commit();						
						a.finish();
						return;
					}
				});
				
				builder.show();
				return true;
			}
		});
		
		Preference presentationRules = (Preference) findPreference("pref_presentation_rules");
		presentationRules.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(preference.getContext(), PresentationModule.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(i);
				return true;
			}
		}); 
		
	}	
	
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {			
		if(!preference.getTitle().toString().equals(newValue)){
			return true;
		}
		
		return false;
	}

	
}