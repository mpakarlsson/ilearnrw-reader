package com.ilearnrw.reader.types;

import java.util.ArrayList;

import com.ilearnrw.reader.R;
import com.ilearnrw.reader.interfaces.OnSettingUpdated;
import com.ilearnrw.reader.types.adapters.ColorOptionsAdapter;
import com.ilearnrw.reader.types.adapters.ColorPresetAdapter;
import com.ilearnrw.reader.utils.Helper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ColorOptionPreference extends Preference implements OnSettingUpdated{

	private int mColorText, mColorBackground, mColorHighlight;
	private String mColorValues;
	
	private Context mContext;
	
	private ArrayList<Preset> ps;
	private ArrayList<ColorOption> cops;
	
	public ColorOptionPreference(Context context) {
		super(context);
		initPreference(context, null);
	}
	
	public ColorOptionPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPreference(context, attrs);
	}
	
	public ColorOptionPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initPreference(context, attrs);
	}

	private void initPreference(Context context, AttributeSet attrs){
		mContext = context;
		setWidgetLayoutResource(R.layout.widget_preference_color_options);
		
		
		ps = new ArrayList<Preset>();
		ps.add(new Preset("Black on White", 				"#000000", "#FFFFFF", "#FFFF00"));
		ps.add(new Preset("Black on White (low contrast)", 	"#454545", "#F2F2F2", "#F4F4F4"));
		ps.add(new Preset("White on Black", 				"#FFFFFF", "#000000", "#698818"));
		ps.add(new Preset("White on Black (low contrast)", 	"#D2D2D2", "#202020", "#5D693F"));
		ps.add(new Preset("Dark on Cream", 					"#000000", "#FFFFCC", "#A9BD77"));
		ps.add(new Preset("Dark on Cream (low contrast)",	"#505050", "#FFFFCC", "#D1E79A"));
		ps.add(new Preset("Dark on gray", 					"#454545", "#CCCCCC", "#FFFFAA"));
		ps.add(new Preset("Green on Black", 				"#00CC00", "#000000", "#100C7A"));
		ps.add(new Preset("Green on Black (low contrast)",	"#70DC70", "#202020", "#476687"));
		ps.add(new Preset("Green on Cream", 				"#008000", "#FFFFCC", "#D5D543"));
		
		cops = new ArrayList<ColorOption>();		
		cops.add(new ColorOption("Text", "#000000", "#000000"));
		cops.add(new ColorOption("Background", "#FFFFCC", "#FFFFCC"));
		cops.add(new ColorOption("Highlight", "#D1E79A", "#D1E79A"));
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		
		final View textBox = view.findViewById(R.id.color_options_widget_text_box);
		final View bgBox = view.findViewById(R.id.color_options_widget_bg_box);
		final View hlBox = view.findViewById(R.id.color_options_widget_hl_box);
		
		if(textBox != null)
			textBox.setBackgroundColor(mColorText);
		
		if(bgBox != null)
			bgBox.setBackgroundColor(mColorBackground);
		
		if(hlBox != null)
			hlBox.setBackgroundColor(mColorHighlight);
	}	
	
	@SuppressLint("InflateParams")
	@Override
	protected void onClick() {
		
		LayoutInflater inflater =  LayoutInflater.from(mContext);
		View dialogView = inflater.inflate(R.layout.dialog_coloring_settings, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setView(dialogView);
		builder.setPositiveButton(mContext.getString(android.R.string.ok), null);
		final AlertDialog alertDialog = builder.create();
		
		ListView presets = (ListView) dialogView.findViewById(R.id.lv_presets);
		ColorPresetAdapter adapter = new ColorPresetAdapter(mContext, R.layout.row_default, ps);
		presets.setAdapter(adapter);
		presets.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Preset item = ps.get(position);
			
				String hex = Helper.fixHex(item.getTextColor());
				mColorText = Helper.hexToColor(hex);
				hex = Helper.fixHex(item.getBackgroundColor());
				mColorBackground = Helper.hexToColor(hex);
				hex = Helper.fixHex(item.getHighlightColor());
				mColorHighlight = Helper.hexToColor(hex);
				
				mColorValues = Integer.toString(mColorText) + " " + Integer.toString(mColorBackground) + " " + Integer.toString(mColorHighlight);
				persistString(mColorValues);
				
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
				sp.edit().putInt(mContext.getString(R.string.pref_text_color_title), mColorText).apply();
				sp.edit().putInt(mContext.getString(R.string.pref_background_color_title), mColorBackground).apply();
				sp.edit().putInt(mContext.getString(R.string.pref_highlight_color_title), mColorHighlight).apply();
				notifyChanged();
				alertDialog.dismiss();
			}
		});
		
		ListView options = (ListView) dialogView.findViewById(R.id.lv_options);
		ColorOptionsAdapter coadapter = new ColorOptionsAdapter(mContext, R.layout.row_color_options, cops, this);
		options.setAdapter(coadapter);
		alertDialog.show();
		
		super.onClick();
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInteger(index, 0);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,
			Object defaultValue) {
		if(restorePersistedValue){
			mColorValues = getPersistedString(mColorValues);
			
			String[] values = mColorValues.split(" ");
			
			mColorText = Integer.parseInt(values[0]);
			mColorBackground = Integer.parseInt(values[1]);
			mColorHighlight = Integer.parseInt(values[2]);
		}
		else {
			mColorText = Color.argb(255, 0, 0, 0);
			mColorBackground = Color.argb(255, 255, 255, 204);
			mColorHighlight = Color.argb(255, 209, 231, 154);
			
			mColorValues = Integer.toString(mColorText) + " " + Integer.toString(mColorBackground) + " " + Integer.toString(mColorHighlight);
			persistString(mColorValues);
		}
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		if(isPersistent()) return superState;
		
		final SavedState state = new SavedState(superState);
		state.text = mColorText;
		state.background = mColorBackground;
		state.hl = mColorHighlight;
		return state;
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if(!state.getClass().equals(SavedState.class)){
			super.onRestoreInstanceState(state);
			return;
		}
		
		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());
		
		mColorText = myState.text;
		mColorBackground = myState.background;
		mColorHighlight = myState.hl;
		
		notifyChanged();
	}
	
	private static class SavedState extends BaseSavedState {
		int text, background, hl;
		
		public SavedState(Parcelable superState){
			super(superState);			
		}
		
		public SavedState(Parcel source){
			super(source);
			text = source.readInt();
			background = source.readInt();
			hl = source.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(text);
			dest.writeInt(background);
			dest.writeInt(hl);
		}
		
		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel source) {
				return new SavedState(source);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};	
	}

	@Override
	public void onSettingUpdated(Integer pos, Integer color) {
		switch (pos) {
		case 0:
			mColorText = color;
			break;

		case 1:
			mColorBackground = color;
			break;
			
		case 2:
			mColorHighlight = color;
			break;
		}

		mColorValues = Integer.toString(mColorText) + " " + Integer.toString(mColorBackground) + " " + Integer.toString(mColorHighlight);
		persistString(mColorValues);
		notifyChanged();
	}
	
}
