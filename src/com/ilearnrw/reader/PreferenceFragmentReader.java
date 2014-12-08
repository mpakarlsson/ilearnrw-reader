package com.ilearnrw.reader;

import java.io.IOException;
import java.util.Locale;

import com.ilearnrw.reader.R;
import com.ilearnrw.reader.types.ColorPickerPreference;
import com.ilearnrw.reader.types.SeekBarPreference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
		
		
		String name = a.getString(R.string.pref_background_color_title);
		ColorPickerPreference backgroundPicker = (ColorPickerPreference)findPreference(name);
		name = a.getString(R.string.pref_text_color_title);
		ColorPickerPreference textPicker = (ColorPickerPreference)findPreference(name);
		
		fontFace.setOnPreferenceChangeListener(this);
		backgroundPicker.setOnPreferenceChangeListener(this);
		textPicker.setOnPreferenceChangeListener(this);
		
		
		Preference comfy = findPreference("preset_comfy");
		Preference cozy = findPreference("preset_cozy");
		Preference narrow = findPreference("preset_narrow");
		
		comfy.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				onPresetClicked("150", "10", "50", "18");
				
				return false;
			}
		});
		
		cozy.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				onPresetClicked("125", "20", "20", "16");				
				return false;
			}
		});
		narrow.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				onPresetClicked("110", "5", "0", "14");
				return false;
			}
		});		
		
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
						edit.apply();						
						a.finish();
						return;
					}
				});
				
				builder.show();
				return true;
			}
		});
	}
	
	private void onPresetClicked(final String lineHeight, final String margin, final String letterSpacing, final String fontSize){
		new Runnable() {
			@Override
			public void run() {
				SeekBarPreference fs = (SeekBarPreference) findPreference("pref_font_size");
				ListPreference lh = (ListPreference) findPreference("pref_line_height");
				final ListPreference m = (ListPreference) findPreference("pref_margin");
				final ListPreference ls = (ListPreference) findPreference("pref_letter_spacing");
				
				lh.setValue(lineHeight);
				m.setValue(margin);
				ls.setValue(letterSpacing);
				fs.setValue(Integer.valueOf(fontSize));
				getActivity().finish();
			}
		}.run();
		
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {			
		if(!preference.getTitle().toString().equals(newValue))
			return true;
		
		return false;
	}
}