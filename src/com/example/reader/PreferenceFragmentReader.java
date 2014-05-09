package com.example.reader;

import java.io.IOException;

import com.example.reader.types.PreferenceColorPicker;
import com.example.reader.types.PreferenceSeekBar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;

public class PreferenceFragmentReader extends PreferenceFragment implements OnPreferenceChangeListener {
	private static boolean changed = false;
	public final static String SHARED_PREFERENCES_FILE_NAME = "PREF_READER";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_FILE_NAME);
		addPreferencesFromResource(R.xml.preferences);
		
		String[] fonts;
		try {
			fonts = getActivity().getBaseContext().getAssets().list("fonts");
			fonts = fonts;
			int a=0;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Activity a = getActivity();
		
		
		EditTextPreference fontSize = (EditTextPreference) findPreference(a.getString(R.string.pref_font_size_title));
		ListPreference fontFace = (ListPreference) findPreference(a.getString(R.string.pref_font_face_title));
		EditTextPreference lineHeight = (EditTextPreference) findPreference(a.getString(R.string.pref_line_height_title));
		EditTextPreference margin = (EditTextPreference) findPreference(a.getString(R.string.pref_margin_title));
		
		PreferenceColorPicker backgroundPicker = (PreferenceColorPicker)findPreference(a.getString(R.string.pref_background_color_title));
		PreferenceColorPicker textPicker = (PreferenceColorPicker)findPreference(a.getString(R.string.pref_text_color_title));
		
		PreferenceSeekBar speechSlider = (PreferenceSeekBar)findPreference(a.getString(R.string.pref_speech_rate_title));
		PreferenceSeekBar pitchSlider = (PreferenceSeekBar)findPreference(a.getString(R.string.pref_pitch_rate_title));
		
		ListPreference languages = (ListPreference)findPreference(a.getString(R.string.pref_tts_language_title));
		
		
		/*fontSize.setOnPreferenceChangeListener(this);
		fontFace.setOnPreferenceChangeListener(this);
		lineHeight.setOnPreferenceChangeListener(this);
		margin.setOnPreferenceChangeListener(this);
		backgroundPicker.setOnPreferenceChangeListener(this);
		textPicker.setOnPreferenceChangeListener(this);
		speechSlider.setOnPreferenceChangeListener(this);
		pitchSlider.setOnPreferenceChangeListener(this);
		languages.setOnPreferenceChangeListener(this);
		*/
	}	
	
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		changed = true;
		return false;
	}

	
	
	@Override
	public void onStop() {
		if(changed){
			Intent i = new Intent();
			i.putExtra("changed", changed);
			getActivity().setResult(Activity.RESULT_OK, i);
			getActivity().finish();
		}
		super.onStop();
	}


	@Override
	public void onDestroyView() {
		
		super.onDestroyView();
	}
}