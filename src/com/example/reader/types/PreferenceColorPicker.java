package com.example.reader.types;

import com.example.reader.R;
import com.example.reader.types.ColorPickerDialog.OnColorChangedListener;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

public class PreferenceColorPicker extends Preference implements OnColorChangedListener{
	private Context context;
	private SharedPreferences preferences;
	private int defaultColor = Color.rgb(255, 255, 255), currentColor;
	
	public PreferenceColorPicker(Context context) {
		super(context);
		this.context = context;
		preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
	}
	
	public PreferenceColorPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
	}

	@Override
	protected void onClick() {
		String key = getKey();
		int clickX = 0;
		int clickY = 0;
		
		if(key.equals(context.getString(R.string.pref_background_color))){
			currentColor = preferences.getInt(context.getString(R.string.pref_background_color), Color.rgb(255, 255, 255));
			defaultColor = Color.rgb(255, 255, 255);
			clickX =  preferences.getInt(context.getString(R.string.pref_background_color_posX), 10);
			clickY =  preferences.getInt(context.getString(R.string.pref_background_color_posY), 50);
		} else if(key.equals(context.getString(R.string.pref_text_color))){
			currentColor = preferences.getInt(context.getString(R.string.pref_text_color), Color.rgb(0, 0, 0));
			defaultColor = Color.rgb(0, 0, 0);
			clickX =  preferences.getInt(context.getString(R.string.pref_text_color_posX), 10);
			clickY =  preferences.getInt(context.getString(R.string.pref_text_color_posY), 305);
		}
		
		
		new ColorPickerDialog(context, this, 
				 key, 
				 currentColor, 
				 defaultColor,
				 clickX,
				 clickY).show();
	}

	@Override
	public void colorChanged(String key, boolean confirm, int color, int xPos, int yPos) {
		
		if(!confirm)
			return;
		
		SharedPreferences.Editor editor = preferences.edit();
		if(key.equals(context.getString(R.string.pref_background_color))){
			editor.putInt(context.getString(R.string.pref_background_color), color);
			editor.putInt(context.getString(R.string.pref_background_color_posX), xPos);
			editor.putInt(context.getString(R.string.pref_background_color_posY), yPos);
		} else if(key.equals(context.getString(R.string.pref_text_color))){
			editor.putInt(context.getString(R.string.pref_text_color), color);
			editor.putInt(context.getString(R.string.pref_text_color_posX), xPos);
			editor.putInt(context.getString(R.string.pref_text_color_posY), yPos);
		}
		editor.commit();
	}
}
