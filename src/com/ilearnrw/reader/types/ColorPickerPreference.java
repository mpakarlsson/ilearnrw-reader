package com.ilearnrw.reader.types;

import java.util.ArrayList;

import com.ilearnrw.reader.R;
import com.ilearnrw.reader.interfaces.ColorPickerListener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListPopupWindow;

public class ColorPickerPreference extends Preference {
	//private static final String androidns = "http://schemas.android.com/apk/res/android";
	//private static final String applicationns = "http://schemas.android.com/apk/res/com.example.reader";
	
	private int mValue;
	
	private boolean mSupportsAlpha = false;
	
	private ColorListPopupWindow colorPopup;
	private ArrayList<String> colours;
	
	public ColorPickerPreference(Context context) {
		super(context);
		initPreference(context, null);
	}
	
	public ColorPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPreference(context, attrs);
	}
	
	public ColorPickerPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initPreference(context, attrs);
	}

	private void initPreference(Context context, AttributeSet attrs){
		if(attrs != null)
			setValuesFromXml(context, attrs);
		
		setWidgetLayoutResource(R.layout.widget_preference_color_picker);
		
		colorPopup = new ColorListPopupWindow(context);
		colours = new ArrayList<String>();
		
		String key = getKey();
		if(key.equals("pref_text_color")){		
			colours.add("3CB371");colours.add("ADD8E6");
			colours.add("C0C0C0");colours.add("CCFB5D");
			colours.add("FFF380");colours.add("F9966B");
			colours.add("E9CFEC");colours.add("FFD700");
		}
		else if(key.equals("pref_background_color")){
			colours.add("000000"); colours.add("FFFFCC");
			colours.add("B2F173"); colours.add("E0FFFF");
			colours.add("F4F4F4"); colours.add("FFFFFF"); 
		} else if(key.equals("pref_highlight_color")){
			colours.add("3CB371");colours.add("ADD8E6");
			colours.add("C0C0C0");colours.add("CCFB5D");
			colours.add("FFFFAA");colours.add("F9966B");
			colours.add("E9CFEC");colours.add("FFD700");
		}
		
		colorPopup.setAdapter(new ColorListAdapter(getContext(), R.layout.color_presents_row, colours));
		colorPopup.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				colorPopup.dismiss();
				mValue = Integer.parseInt(colours.get(position), 16) + 0xFF000000;
				persistInt(mValue);
				notifyChanged();
			}
		});
	}
	
	private void setValuesFromXml(Context context, AttributeSet attrs){		
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerPreference);
		mSupportsAlpha  = ta.getBoolean(R.styleable.ColorPickerPreference_supportsAlpha, false);
		ta.recycle();
		//mSupportsAlpha  = attrs.getAttributeBooleanValue(applicationns, "supportsAlpha", false);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
	
		final View box = view.findViewById(R.id.color_picker_preference_widget_box);
		if(box!=null){
			box.setBackgroundColor(mValue);
		
			colorPopup.setParentListPosition(-1);
			colorPopup.setAnchorView(box);
			colorPopup.setModal(true);
			colorPopup.setWidth(70);
			colorPopup.setPromptPosition(ListPopupWindow.POSITION_PROMPT_BELOW);
		}
	}

	@Override
	protected void onClick() {
		//Create dialog , simple, advanced
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setTitle(getContext().getString(R.string.pref_color_title))
			.setNegativeButton(android.R.string.cancel, null)
			.setItems(R.array.pref_color_choices, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						colorPopup.show();
						break;

					case 1:
						new ColorPickerDialog(getContext(), mValue, mSupportsAlpha, new ColorPickerListener() {
							@Override
							public void onOk(ColorPickerDialog dialog, int color) {
								if(!callChangeListener(color)) return;
								mValue = color;
								persistInt(mValue);
								notifyChanged();
							}
							
							@Override
							public void onCancel(ColorPickerDialog dialog) {}
							
						}).show();
						break;
					}
					
				}
			});
		builder.show();
		super.onClick();
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInteger(index, 0);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,
			Object defaultValue) {
		if(restorePersistedValue)
			mValue = getPersistedInt(mValue);
		else {
			int value = (Integer) defaultValue;
			mValue = value;
			persistInt(value);
		}
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		if(isPersistent()) return superState;
		
		final SavedState state = new SavedState(superState);
		state.value = mValue;
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
		this.mValue = myState.value;
		notifyChanged();
	}
	
	private static class SavedState extends BaseSavedState {
		int value;
		
		public SavedState(Parcelable superState){
			super(superState);			
		}
		
		public SavedState(Parcel source){
			super(source);
			value = source.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(value);
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
}
