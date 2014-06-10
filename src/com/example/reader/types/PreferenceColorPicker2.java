package com.example.reader.types;

import com.example.reader.R;
import com.example.reader.types.ColorPickerDialog2.OnColorChangedListener;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

public class PreferenceColorPicker2 extends Preference implements OnColorChangedListener{
	private Context context;
	private SharedPreferences preferences;
	private int defaultColor = Color.rgb(255, 255, 255), currentColor;
	
	private String prefBackgroundColor, prefBackgroundColorPosX, prefBackgroundColorPosY;
	private String prefTextColor, prefTextColorPosX, prefTextColorPosY;
	private String prefHighlightColor, prefHighlightColorPosX, prefHighlightColorPosY;
	
	public PreferenceColorPicker2(Context context) {
		super(context);
		this.context = context;
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		setupPreferenceStrings();
	}
	
	public PreferenceColorPicker2(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		setupPreferenceStrings();
	}

	@Override
	protected void onClick() {
		String key = getKey();
		int clickX = 0;
		int clickY = 0;
		
		if(key.equals(prefBackgroundColor)){
			defaultColor = Color.rgb(255, 255, 255);
			currentColor = preferences.getInt(prefBackgroundColor, defaultColor);
			clickX =  preferences.getInt(prefBackgroundColorPosX, 10);
			clickY =  preferences.getInt(prefBackgroundColorPosY, 50);
		} else if(key.equals(prefTextColor)){
			defaultColor = Color.rgb(0, 0, 0);
			currentColor = preferences.getInt(prefTextColor, defaultColor);
			clickX =  preferences.getInt(prefTextColorPosX, 10);
			clickY =  preferences.getInt(prefTextColorPosY, 305);
		} else if(key.equals(prefHighlightColor)){
			defaultColor = Color.rgb(255, 255, 0);
			currentColor = preferences.getInt(prefTextColor, defaultColor);
			clickX =  preferences.getInt(prefTextColorPosX, 265);
			clickY =  preferences.getInt(prefTextColorPosY, 50);
			
		}
		
		
		new ColorPickerDialog2(context, this, 
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
		if(key.equals(prefBackgroundColor)){
			editor.putInt(prefBackgroundColor, color);
			editor.putInt(prefBackgroundColorPosX, xPos);
			editor.putInt(prefBackgroundColorPosY, yPos);
		} else if(key.equals(prefTextColor)){
			editor.putInt(prefTextColor, color);
			editor.putInt(prefTextColorPosX, xPos);
			editor.putInt(prefTextColorPosY, yPos);
		} else if(key.equals(prefHighlightColor)){
			editor.putInt(prefHighlightColor, color);
			editor.putInt(prefHighlightColorPosX, xPos);
			editor.putInt(prefHighlightColorPosY, yPos);
		}
		editor.commit();
	}
	
	private void setupPreferenceStrings(){
		prefBackgroundColor = context.getString(R.string.pref_background_color_title);
		prefBackgroundColorPosX = context.getString(R.string.pref_background_color_posX_title);
		prefBackgroundColorPosY = context.getString(R.string.pref_background_color_posY_title);
		prefTextColor = context.getString(R.string.pref_text_color_title);
		prefTextColorPosX = context.getString(R.string.pref_text_color_posX_title);
		prefTextColorPosY = context.getString(R.string.pref_text_color_posY_title);
		prefHighlightColor = context.getString(R.string.pref_highlight_color_title);
		prefHighlightColorPosX = context.getString(R.string.pref_highlight_color_posX_title);
		prefHighlightColorPosY = context.getString(R.string.pref_highlight_color_posY_title);
	}
}
