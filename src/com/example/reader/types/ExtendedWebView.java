package com.example.reader.types;

import com.example.reader.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

public class ExtendedWebView extends WebView implements ActionMode.Callback {

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

	@Override
	public ActionMode startActionMode(Callback callback) {
		return super.startActionMode(callback);
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.cab_copy:
			Toast.makeText(getContext(), "COPY!", Toast.LENGTH_SHORT).show();
			
			mode.finish();
			return true;

		default:
			return false;
		}
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.cab, menu);
		
		MenuItem item = menu.getItem(0);
		
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}
	
	

//	@Override
//	public boolean performLongClick() {		
//		return true;
//	}

	
	
	
}
