package com.ilearnrw.reader.types;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
import com.ilearnrw.reader.R;

import android.app.Service;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SeekBarPreference extends Preference implements OnSeekBarChangeListener{

	private final String TAG = getClass().getName();
	
	private static final String androidns = "http://schemas.android.com/apk/res/android";
	//private static final String applicationns = "http://schemas.android.com/apk/res/com.example.reader";
	private static final String applicationns = "http://schemas.android.com/apk/res-auto";
	
	public static final int FLAG_SHOW_STATUS_AS_TEXT = 1;
	public static final int FLAG_SHOW_UNITS = 2;
	public static final int FLAG_SHOW_VALUE_AS_EDIT_TEXT = 4;
	
	private int mDefault = 50;
	
	private int mMax		= 100;
	private int mMin		= 0;
	private int mInterval	= 1;
	private int mCurrent;
	private String mUnitsLeft	= "";
	private String mUnitsRight	= "";
	private SeekBar mSeekBar;
	//private boolean mIsStatusNumber;
	private int mFlags;
	private boolean mShowStatusAsText;
	private boolean mShowValueAsEditText;
	private boolean mShowUnits;
	
	private CharSequence[] mTextValues;
	private TextView mStatusText;
	private EditText mEditText;
	
	
	public SeekBarPreference(Context context, AttributeSet attrs){
		super(context, attrs);
		initPreference(context, attrs, 0);
	}
	
	public SeekBarPreference(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		initPreference(context, attrs, defStyle);
	}
	
	private void initPreference(Context context, AttributeSet attrs, int defStyle){
		setValuesFromXml(context, attrs);
		mSeekBar = new SeekBar(context, attrs);
		mSeekBar.setMax(mMax - mMin);
		mSeekBar.setOnSeekBarChangeListener(this);
		
		setWidgetLayoutResource(R.layout.widget_preference_seekbar);
	}
	
	private void setValuesFromXml(Context context, AttributeSet attrs){
		mMax 		= attrs.getAttributeIntValue(androidns, "max", 100);
		mDefault 	= attrs.getAttributeIntValue(androidns, "defaultValue", 50);
		mMin 		= attrs.getAttributeIntValue(applicationns, "min", 0);
		
		mUnitsLeft		= getAttributeStringValue(attrs, applicationns, "unitsLeft", "");
		String units	= getAttributeStringValue(attrs, applicationns, "units", "");
		mUnitsRight		= getAttributeStringValue(attrs, applicationns, "unitsRight", units);
		
		//mIsStatusNumber	= Boolean.parseBoolean(getAttributeStringValue(attrs, applicationns, "statusNumber", "false"));
		
		mFlags = attrs.getAttributeIntValue(applicationns, "seekbarOptions", 0);
		
		mShowStatusAsText = (mFlags & FLAG_SHOW_STATUS_AS_TEXT) == FLAG_SHOW_STATUS_AS_TEXT ? true : false;
		mShowUnits = (mFlags & FLAG_SHOW_UNITS) == FLAG_SHOW_UNITS ? true : false;
		mShowValueAsEditText = (mFlags & FLAG_SHOW_VALUE_AS_EDIT_TEXT) == FLAG_SHOW_VALUE_AS_EDIT_TEXT ? true : false;
		
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PreferenceSeekBar, 0, 0);		
		try {
			mTextValues = a.getTextArray(com.ilearnrw.reader.R.styleable.PreferenceSeekBar_textValues);			
		} catch (Exception e) {
			e.printStackTrace();
			mTextValues = new CharSequence[] {"Default", "Slowest", "Slower", "Slow", "Fast", "Faster", "Fastest"};
		} finally {
			a.recycle();
		}
		
		try {
			String newInterval = attrs.getAttributeValue(applicationns, "interval");
			if(newInterval != null)
				mInterval = Integer.parseInt(newInterval);
		} catch (Exception e) {
			Log.e(TAG, "Invalid interval value", e);
		}
		
	}

	private String getAttributeStringValue(AttributeSet attrs, String namespace, String name, String defaultValue){
		
		final String STR = "@string/";
		String value = attrs.getAttributeValue(namespace, name);
		if(value==null)
			value = defaultValue;
		
		if(value.length()>1 && value.charAt(0) == '@' && value.contains(STR)){
			Context context = getContext();
			Resources res = context.getResources();
			final int id = res.getIdentifier(context.getPackageName() + ":" + value.substring(1), null, null);
			value = context.getString(id);
		}
		return value;
	}	
	
	@Override
	protected View onCreateView(ViewGroup parent) {
		View view = super.onCreateView(parent);
	
		LinearLayout layout = (LinearLayout) view;
		layout.setOrientation(LinearLayout.VERTICAL);
	
		return view;
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
	
		if(view!=null){
			mSeekBar = (SeekBar) view.findViewById(R.id.seekBarPrefSeekBar);
			mSeekBar.setMax(mMax-mMin);
			mSeekBar.setOnSeekBarChangeListener(this);
		}
		updateView(view);
	}
	
	protected void updateView(View view){
		try {
			
			mEditText = (EditText) view.findViewById(R.id.seekBarPrefValueEdit);
			mStatusText = (TextView) view.findViewById(R.id.seekBarPrefValue);			
			updateStatusText();
			mStatusText.setMinimumWidth(30);
			
			mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {          
			    public void onFocusChange(View v, boolean hasFocus) {
			        if(hasFocus) {
			            // show keyboard
			            InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Service.INPUT_METHOD_SERVICE);
			            imm.showSoftInput(mEditText, 0);

			        }
			    }
			});
			
			LinearLayout seekbarLayout = (LinearLayout) view.findViewById(R.id.seekBarPrefBarContainer);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) seekbarLayout.getLayoutParams();
			
			mSeekBar.setProgress(mCurrent-mMin);
			
			TextView unitsRight = (TextView) view.findViewById(R.id.seekBarPrefUnitsRight);
			unitsRight.setText(mUnitsRight);
			
			TextView unitsLeft = (TextView) view.findViewById(R.id.seekBarPrefUnitsLeft);
			unitsLeft.setText(mUnitsLeft);
			
			if(!mShowUnits){
				unitsLeft.setText("");
				unitsRight.setText("");
			}
			
			if(mShowValueAsEditText){
				mStatusText.setVisibility(View.GONE);
				unitsLeft.setVisibility(View.GONE);
				unitsRight.setText("");
				
				params.addRule(RelativeLayout.BELOW, R.id.seekBarPrefValueEdit);
				
				//RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mEditText.getLayoutParams();
				//params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
				//params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
				
			} else {
				mEditText.setVisibility(View.GONE);
			}
			
		} catch (Exception e) {
			Log.e(TAG, "Error updating seekbar preference", e);
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		int newValue = progress + mMin;
		
		if(newValue > mMax)
			newValue = mMax;
		else if(newValue < mMin)
			newValue = mMin;
		else if(mInterval != 1 && newValue % mInterval != 0)
			newValue = Math.round(((float)newValue)/mInterval)*mInterval;
		
		if(!callChangeListener(newValue)){
			seekBar.setProgress(mCurrent-mMin);
			return;
		}
		
		mCurrent = newValue;
		updateStatusText();
	}
	
	public void setValue(int value){
		if(value > mMax)
			value = mMax;
		else if(value < mMin)
			value = mMin;
		else if(mInterval != 1 && value % mInterval != 0)
			value = Math.round(((float)value)/mInterval)*mInterval;

		persistInt(value);
	}
	
	private void updateStatusText(){		
		if(!mShowStatusAsText){
			String value = String.valueOf(mCurrent);
			mStatusText.setText(value);
			mEditText.setText(value);
		} else {
			double diff = 0.0;
			
			if(mCurrent == mDefault)
				mStatusText.setText(mTextValues[0]);
			else if(mCurrent>mDefault){
				diff = (double)(mCurrent-mDefault)/(double)mDefault;
				
				if(diff>=0.0 && diff<0.5)
					mStatusText.setText(mTextValues[4]);
				else if(diff>=0.5 && diff<1.0)
					mStatusText.setText(mTextValues[5]);
				else if(diff>=1.0)
					mStatusText.setText(mTextValues[6]);
			}
			else {
				diff = (double)(mDefault-mCurrent)/(double)mCurrent;
				
				if(diff>=0.0 && diff<0.5)
					mStatusText.setText(mTextValues[3]);
				else if(diff>=0.5 && diff<1.0)
					mStatusText.setText(mTextValues[2]);
				else if(diff>=1.0)
					mStatusText.setText(mTextValues[1]);
			}			
		}
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		persistInt(mCurrent);
		notifyChanged();
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		int defaultValue = a.getInt(index, mDefault);
		return defaultValue;
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,
			Object defaultValue) {
		
		if(restorePersistedValue)
			mCurrent = getPersistedInt(mCurrent);
		else {
			int temp = 0;
			try {
				temp = (Integer)defaultValue;
			} catch (Exception e) {
				Log.e(TAG, "Invalid default value" + e.toString());
			}
			
			persistInt(temp);
			mCurrent = temp;
		}
		super.onSetInitialValue(restorePersistedValue, defaultValue);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		mSeekBar.setEnabled(enabled);
	}

	@Override
	public void onDependencyChanged(Preference dependency,
			boolean disableDependent) {
		super.onDependencyChanged(dependency, disableDependent);
	
		if(mSeekBar != null){
			mSeekBar.setEnabled(!disableDependent);
		}
	}
}
