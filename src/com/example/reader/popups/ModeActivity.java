package com.example.reader.popups;

import com.example.reader.R;
import com.example.reader.ReaderActivity.ReaderMode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.Toast;

public class ModeActivity extends Activity {
	
	public static float posX, posY;
	public static int imageHeight, mode;
	public static RadioButton listen, guidance, chunking;
	
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
		chunking = (RadioButton) findViewById(R.id.rdbtn_mode_chunking);
		
		ReaderMode mode = (ReaderMode) bundle.get("readerMode");
		
		switch (mode.getValue()) {
		case 0:
			listen.setChecked(true);
			break;
		case 1:
			guidance.setChecked(true);
			break;
		case 2:
			chunking.setChecked(true);
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
	    switch(view.getId()) {
	        case R.id.rdbtn_mode_listen:
	            if(mode!=0){
	            	mode=0;
	            	PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("readerMode", mode).commit();
	            } else
	            	Toast.makeText(this, "Listen mode, already active.", Toast.LENGTH_SHORT).show();
	            break;
	        case R.id.rdbtn_mode_highlight:
	        	if(mode!=1){
	            	mode=1;
	            	PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("readerMode", mode).commit();
	            } else
	            	Toast.makeText(this, "Guidance mode, already active.", Toast.LENGTH_SHORT).show();
	        	break;
	        case R.id.rdbtn_mode_chunking:
	        	//if(mode!=2){
	            //	mode=2;
	            //	getPreferences(MODE_PRIVATE).edit().putInt("readerMode", mode);
	            //} 
            	Toast.makeText(this, "Chunking mode not available, Listen mode chosen instead", Toast.LENGTH_SHORT).show();
            	mode = 0;
            	PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("readerMode", mode).commit();
	        	break;
	    }
	    
	    Intent intent=new Intent();
	    intent.putExtra("chosenMode", mode);
	    setResult(RESULT_OK, intent);
	    finish();
	}
	
	
}
