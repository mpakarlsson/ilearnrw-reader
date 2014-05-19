package com.example.reader;

import java.io.IOException;
import java.util.Locale;

import com.example.reader.types.PreferenceColorPicker;
import com.example.reader.types.PreferenceSeekBar;

import android.app.Activity;
import android.os.Bundle;

import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
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
		EditTextPreference fontSize = (EditTextPreference) findPreference(a.getString(R.string.pref_font_size_title));
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
		
		EditTextPreference lineHeight = (EditTextPreference) findPreference(a.getString(R.string.pref_line_height_title));
		EditTextPreference margin = (EditTextPreference) findPreference(a.getString(R.string.pref_margin_title));
		
		PreferenceColorPicker backgroundPicker = (PreferenceColorPicker)findPreference(a.getString(R.string.pref_background_color_title));
		PreferenceColorPicker textPicker = (PreferenceColorPicker)findPreference(a.getString(R.string.pref_text_color_title));
		
		PreferenceSeekBar speechSlider = (PreferenceSeekBar)findPreference(a.getString(R.string.pref_speech_rate_title));
		PreferenceSeekBar pitchSlider = (PreferenceSeekBar)findPreference(a.getString(R.string.pref_pitch_title));
		
		ListPreference languages = (ListPreference)findPreference(a.getString(R.string.pref_tts_language_title));
		if(languages.getValue() == null)
			languages.setValueIndex(0);
		/*if(changed){
			Intent i = new Intent();
			i.putExtra("changed", changed);
			getActivity().setResult(Activity.RESULT_OK, i);
			getActivity().finish();
		}*/
		fontSize.setOnPreferenceChangeListener(this);
		fontFace.setOnPreferenceChangeListener(this);
		lineHeight.setOnPreferenceChangeListener(this);
		margin.setOnPreferenceChangeListener(this);
		backgroundPicker.setOnPreferenceChangeListener(this);
		textPicker.setOnPreferenceChangeListener(this);
		speechSlider.setOnPreferenceChangeListener(this);
		pitchSlider.setOnPreferenceChangeListener(this);
		languages.setOnPreferenceChangeListener(this);
	}	
	
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		
		if(!preference.getTitle().toString().equals(newValue)){
			return true;
		}
		
		return false;
	}

	
}