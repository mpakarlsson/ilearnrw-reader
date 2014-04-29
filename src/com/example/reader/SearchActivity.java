package com.example.reader;

import com.example.reader.types.ExtendedEditText;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class SearchActivity extends Activity implements OnClickListener, OnEditorActionListener{

	public static float posX, posY;
	public static int imageHeight;
	public static ExtendedEditText search;
	public static ImageButton ibtn_search;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		posX = getIntent().getFloatExtra("posX", -1);
		posY = getIntent().getFloatExtra("posY", -1);
		imageHeight = getIntent().getIntExtra("imageHeight", -1);
		
		search = (ExtendedEditText) findViewById(R.id.et_search);
		search.setOnEditorActionListener(this);
		search.setActivity(this);
		// TODO: Close search activity with one back button press even if keyboard is active
	
		ibtn_search = (ImageButton) findViewById(R.id.ibtn_search_activity);
		ibtn_search.setOnClickListener(this);	
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		View v = getWindow().getDecorView();
		WindowManager.LayoutParams wmlp = (WindowManager.LayoutParams) v.getLayoutParams();
		wmlp.gravity = Gravity.LEFT | Gravity.TOP;
		wmlp.x = (int)posX;
		wmlp.y = (int)posY + imageHeight;
		getWindowManager().updateViewLayout(v, wmlp);
		
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	
	}

	@Override
	public void onBackPressed() {
		Intent i = new Intent(this, ReaderActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(i);
		finish();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibtn_search_activity:
			Intent intent=new Intent();
		    intent.putExtra("searchString", search.getText().toString());
		    setResult(RESULT_OK, intent);
		    finish();
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if(actionId==EditorInfo.IME_ACTION_DONE){
			ibtn_search.callOnClick();
			hideKeyboard();
			return true;
		}
		
		return false;
	}

	

	private void hideKeyboard(){
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(ibtn_search.getWindowToken(), 0);
	}
	

	
	
	
}
