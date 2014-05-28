package com.example.reader.types;

import com.example.reader.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.webkit.WebView;

public class ExtendedWebView extends WebView  {

	private ActionMode actionMode;
	private ActionMode.Callback actionModeCallback;
	private ActionMode.Callback selectActionModeCallback;
	private GestureDetector detector;
	
	public ExtendedWebView(Context context) {
		super(context);
		init();
	}
	
	public ExtendedWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public ExtendedWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init(){
		detector = new GestureDetector(getContext(), new CustomGestureListener());
	}

	@Override
	public ActionMode startActionMode(Callback callback) {
		String name = callback.getClass().toString();
        if (name.contains("SelectActionModeCallback")) {
            selectActionModeCallback = callback;
        }
        
        actionModeCallback = new CustomActionModeCallback();
        return super.startActionModeForChild(this, actionModeCallback);
	}

	
	private class CustomActionModeCallback implements ActionMode.Callback {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			actionMode = mode;
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.cab_reader, menu);
			
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			loadUrl("javascript:getSelectedTextInfo()");
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {			
			switch (item.getItemId()) {
			case R.id.cab_reader_more_info:
				loadUrl("javascript:showMoreInformation();");
				break;

			default:
				break;
			}
			mode.finish();
			return true;
		} 

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			//clearFocus();
			
			if(selectActionModeCallback != null)
				selectActionModeCallback.onDestroyActionMode(mode);
			
			actionMode = null;
		}
	}

	private class CustomGestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			if(actionMode!=null){
				actionMode.finish();
				return true;
			}
			return false;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		detector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}	
	
	
}
