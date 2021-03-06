package com.ilearnrw.reader.popups;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
import com.ilearnrw.reader.R;
import com.ilearnrw.reader.ReaderActivity.ReaderMode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;

public class ModeActivity extends Activity {
	
	public static float posX, posY;
	public static int imageHeight;
	public static RadioButton listen, guidance;
	
	static final int PICK_READER_MODE = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_activity_mode);
	
		Bundle bundle = getIntent().getExtras();	
		posX = bundle.getFloat("posX", -1);
		posY = bundle.getFloat("posY", -1);
		imageHeight = bundle.getInt("imageHeight", -1);
	
		listen = (RadioButton) findViewById(R.id.rdbtn_mode_listen);
		guidance = (RadioButton) findViewById(R.id.rdbtn_mode_highlight);
		
		ReaderMode mode = (ReaderMode) bundle.get("readerMode");
		
		switch (mode.getValue()) {
		case 0:
			listen.setChecked(true);
			break;
		case 1:
			guidance.setChecked(true);
			break;
		default:
			break;
		}
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		View v = getWindow().getDecorView();
		WindowManager.LayoutParams wmlp = (WindowManager.LayoutParams) v.getLayoutParams();
		wmlp.gravity = Gravity.LEFT | Gravity.TOP;
		wmlp.x = (int)posX;
		wmlp.y = (int)posY;
		getWindowManager().updateViewLayout(v, wmlp);
		
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}

	public void onRadioButtonClicked(View view) {
		Intent intent=new Intent();
		intent.putExtra("chosenMode", -1);
		
	    switch(view.getId()) {
	        case R.id.rdbtn_mode_listen:
	            intent.putExtra("chosenMode", 0);
	            break;
	        case R.id.rdbtn_mode_highlight:
	        	intent.putExtra("chosenMode", 1);
	        	break;
	    }
	    
	    setResult(RESULT_OK, intent);
	    finish();
	}
}
