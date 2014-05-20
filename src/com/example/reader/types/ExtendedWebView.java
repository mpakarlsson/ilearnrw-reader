package com.example.reader.types;

import android.app.AlertDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class ExtendedWebView extends WebView {

	public ExtendedWebView(Context context) {
		super(context);
	}
	
	public ExtendedWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ExtendedWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean performLongClick() {
		return super.performLongClick();
	}
	
	

//	@Override
//	public boolean performLongClick() {		
//		return true;
//	}

	
	
	
}
