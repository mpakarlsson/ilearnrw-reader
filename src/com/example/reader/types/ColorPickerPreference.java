package com.example.reader.types;

import com.example.reader.R;
import com.example.reader.interfaces.ColorPickerListener;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

public class ColorPickerPreference extends Preference {
	//private static final String androidns = "http://schemas.android.com/apk/res/android";
	//private static final String applicationns = "http://schemas.android.com/apk/res/com.example.reader";
	
	private int mValue;
	
	private boolean mSupportsAlpha = false;
	
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
		if(box!=null)
			box.setBackgroundColor(mValue);
	}

	@Override
	protected void onClick() {
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
