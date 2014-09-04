package com.example.reader.types;

import java.util.ArrayList;

import com.example.reader.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SeekBarPreference extends Preference implements OnSeekBarChangeListener{

	private final String TAG = getClass().getName();
	
	private static final String androidns = "http://schemas.android.com/apk/res/android";
	private static final String applicationns = "http://schemas.android.com/apk/res/com.example.reader";
	private int mDefault = 50;
	
	private int mMax		= 100;
	private int mMin		= 0;
	private int mInterval	= 1;
	private int mCurrent;
	private String mUnitsLeft	= "";
	private String mUnitsRight	= "";
	private SeekBar mSeekBar;
	private boolean mIsStatusNumber;
	private ArrayList<String> mTextValues;
	private String textValueIdentifier;
	
	private TextView mStatusText;
	
	public SeekBarPreference(Context context, AttributeSet attrs){
		super(context, attrs);
		initPreference(context, attrs);
	}
	
	public SeekBarPreference(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		initPreference(context, attrs);
	}
	
	private void initPreference(Context context, AttributeSet attrs){
		setValuesFromXml(attrs);
		mSeekBar = new SeekBar(context, attrs);
		mSeekBar.setMax(mMax - mMin);
		mSeekBar.setOnSeekBarChangeListener(this);
		
		setWidgetLayoutResource(R.layout.widget_preference_seekbar);
	}
	
	private void setValuesFromXml(AttributeSet attrs){
		mMax 		= attrs.getAttributeIntValue(androidns, "max", 100);
		mDefault 	= attrs.getAttributeIntValue(androidns, "defaultValue", 50);
		mMin 		= attrs.getAttributeIntValue(applicationns, "min", 0);
		
		mUnitsLeft		= getAttributeStringValue(attrs, applicationns, "unitsLeft", "");
		String units	= getAttributeStringValue(attrs, applicationns, "units", "");
		mUnitsRight		= getAttributeStringValue(attrs, applicationns, "unitsRight", units);
		
		textValueIdentifier		= getAttributeStringValue(attrs, applicationns, "textValues", "");
		
		mIsStatusNumber	= Boolean.parseBoolean(getAttributeStringValue(attrs, applicationns, "statusNumber", "false"));
		
		
		
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
			mStatusText = (TextView) view.findViewById(R.id.seekBarPrefValue);
			
			updateStatusText();
			mStatusText.setMinimumWidth(30);

			mSeekBar.setProgress(mCurrent-mMin);
			
			TextView unitsRight = (TextView) view.findViewById(R.id.seekBarPrefUnitsRight);
			unitsRight.setText(mUnitsRight);
			
			TextView unitsLeft = (TextView) view.findViewById(R.id.seekBarPrefUnitsLeft);
			unitsLeft.setText(mUnitsLeft);
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

	private void updateStatusText(){
		if(mIsStatusNumber)
			mStatusText.setText(String.valueOf(mCurrent));
		else {
			double diff = 0.0;
			
			if(mCurrent == mDefault)
				mStatusText.setText("Default");
			else if(mCurrent>mDefault){
				diff = (double)(mCurrent-mDefault)/(double)mDefault;
				
				if(diff>=0.0 && diff<0.5)
					mStatusText.setText("Fast");
				else if(diff>=0.5 && diff<1.0)
					mStatusText.setText("Faster");
				else if(diff>=1.0)
					mStatusText.setText("Rabbit");
			}
			else {
				diff = (double)(mDefault-mCurrent)/(double)mCurrent;
				
				if(diff>=0.0 && diff<0.5)
					mStatusText.setText("Slow");
				else if(diff>=0.5 && diff<1.0)
					mStatusText.setText("Slower");
				else if(diff>=1.0)
					mStatusText.setText("Turtle");
				
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
