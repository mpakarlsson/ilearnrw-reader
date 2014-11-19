package com.ilearnrw.reader.types;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class ExtendedEditText extends EditText {

	private static Activity activity;
	
	public ExtendedEditText(Context context) {
		super(context);
	}
	
	public ExtendedEditText(Context context, AttributeSet attrs){
        super(context, attrs);
    }
	
	public ExtendedEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
	}
	
	public void setActivity(Activity a){
		activity = a;
	}

	@Override
	public boolean dispatchKeyEventPreIme(KeyEvent event) {
	    if(KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
	    	InputMethodManager mgr = (InputMethodManager) activity.getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	        mgr.hideSoftInputFromWindow(this.getWindowToken(), 0);
	    }
	    return super.dispatchKeyEventPreIme(event);
	}
}
