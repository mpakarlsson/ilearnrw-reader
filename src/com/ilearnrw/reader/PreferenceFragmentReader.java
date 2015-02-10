package com.ilearnrw.reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import com.ilearnrw.reader.R;
import com.ilearnrw.reader.types.ColorOptionPreference;
import com.ilearnrw.reader.types.Pair;
import com.ilearnrw.reader.types.SeekBarPreference;
import com.ilearnrw.reader.types.SystemTags;
import com.ilearnrw.reader.utils.Helper;
import com.ilearnrw.reader.utils.HttpHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

public class PreferenceFragmentReader extends PreferenceFragment implements OnPreferenceChangeListener {
	
	
	ArrayList<String> data = new ArrayList<String>();
	ArrayList<Pair<String>> updated = new ArrayList<Pair<String>>();
	boolean isPreset = false;
	String presetType = "";
	
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
		
		
		if(fontFace.getValue().startsWith("Open_Dyslexic")){
			ListPreference lh = (ListPreference) findPreference("pref_line_height");
			lh.setValue("150");
			lh.setEntries(R.array.pref_line_height_open_dyslexic);
			lh.setEntryValues(R.array.pref_line_height_values_open_dyslexic);
		}
		
		fontFace.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				
				if(preference.getKey().equals(getActivity().getString(R.string.pref_font_face_title))){
					String val = (String) newValue;
					ListPreference lh = (ListPreference) findPreference("pref_line_height");
					if(val.startsWith("Open_Dyslexic")){
						lh.setValue("150");
						lh.setEntries(R.array.pref_line_height_open_dyslexic);
						lh.setEntryValues(R.array.pref_line_height_values_open_dyslexic);
					} else {
						lh.setValue("135");
						lh.setEntries(R.array.pref_line_height);
						lh.setEntryValues(R.array.pref_line_height_values);
					}
				}
				return true;
			}
		});
		
		Preference comfy = findPreference("preset_comfy");
		Preference cozy = findPreference("preset_cozy");
		Preference narrow = findPreference("preset_narrow");
		
		comfy.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				onPresetClicked("165", "8", "160", "34", "comfy");
				return false;
			}
		});
		
		cozy.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				onPresetClicked("135", "4", "120", "28", "cozy");				
				return false;
			}
		});
		narrow.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				onPresetClicked("100", "4", "100", "20", "snug");
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
						edit.remove(getString(R.string.pref_highlight_color_posY_title));
						edit.remove(getString(R.string.pref_font_size_title));
						edit.remove(getString(R.string.pref_font_face_title));
						edit.remove(getString(R.string.pref_line_height_title));
						edit.remove(getString(R.string.pref_letter_spacing_title));
						edit.remove(getString(R.string.pref_margin_title));
						edit.remove(getString(R.string.pref_speech_rate_title));
						edit.remove(getString(R.string.pref_pitch_title));
						edit.remove(getString(R.string.pref_tts_language_title));
						edit.remove("pref_color_options");
						edit.apply();
						a.finish();
						return;
					}
				});
				
				builder.show();
				return true;
			}
		});
		
		SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(a.getBaseContext());
		
		int font_size = prefs.getInt(getString(R.string.pref_font_size_title), -1);
		int speech_rate = prefs.getInt(getString(R.string.pref_speech_rate_title), -1);
		int pitch = prefs.getInt(getString(R.string.pref_pitch_title), -1);

		ListPreference ff = (ListPreference) findPreference(getString(R.string.pref_font_face_title));
		ListPreference lh = (ListPreference) findPreference(getString(R.string.pref_line_height_title));
		ListPreference m = (ListPreference) findPreference(getString(R.string.pref_margin_title));
		ColorOptionPreference cop = (ColorOptionPreference) findPreference("pref_color_options");
		String[] colors = cop.getValue().split(" ");
		
		data.add(Integer.toString(font_size));
		data.add(ff.getValue());
		data.add(lh.getValue());
		data.add(m.getValue());
		data.add(colors[0]);
		data.add(colors[1]);
		data.add(colors[2]);
		data.add(Integer.toString(speech_rate));
		data.add(Integer.toString(pitch));	
		
		
	}
	
	
	
	@Override
	public void onDestroy() {
		SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
		
		int font_size = prefs.getInt(getString(R.string.pref_font_size_title), -1);
		int speech_rate = prefs.getInt(getString(R.string.pref_speech_rate_title), -1);
		int pitch = prefs.getInt(getString(R.string.pref_pitch_title), -1);

		ListPreference ff = (ListPreference) findPreference(getString(R.string.pref_font_face_title));
		ListPreference lh = (ListPreference) findPreference(getString(R.string.pref_line_height_title));
		ListPreference m = (ListPreference) findPreference(getString(R.string.pref_margin_title));
		ColorOptionPreference cop = (ColorOptionPreference) findPreference("pref_color_options");
		String[] colors = cop.getValue().split(" ");
		
		updated.add(new Pair<String>("Font size", Integer.toString(font_size)));
		updated.add(new Pair<String>("Font face", ff.getValue()));
		updated.add(new Pair<String>("Line height", lh.getValue()));
		updated.add(new Pair<String>("Margin", m.getValue()));
		updated.add(new Pair<String>("Text color", colors[0]));
		updated.add(new Pair<String>("Background color", colors[1]));
		updated.add(new Pair<String>("Highlight color", colors[2]));
		updated.add(new Pair<String>("Speech rate", Integer.toString(speech_rate)));
		updated.add(new Pair<String>("Pitch", Integer.toString(pitch)));
		
		String logMsg = "";
		
		if(isPreset)
			logMsg = "Preset " + presetType + ",";
		
		for(int i=0; i<data.size(); i++){
			Pair<String> temp = updated.get(i);
			if(!data.get(i).equals(temp.second())){
				String value = temp.second();
				if(temp.first().contains("color"))
					value = Helper.colorToHex(Integer.valueOf(value));
				
				logMsg += temp.first() + " " + value + ",";
			}
		}
		
		if(!logMsg.isEmpty())
			logMsg = logMsg.substring(0, logMsg.length()-1);
		
		
		HttpHelper.log(getActivity().getBaseContext(), logMsg, SystemTags.SETTINGS_UPDATED);
		
		super.onDestroy();
	}



	private void onPresetClicked(final String lineHeight, final String margin, final String letterSpacing, final String fontSize, final String type){
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
				
				isPreset = true;
				presetType = type;
				
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