package com.example.reader.popups;

import java.io.File;

import com.example.reader.LibraryActivity;
import com.example.reader.R;
import com.example.reader.ReaderActivity;
import com.example.reader.types.ExtendedEditText;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class RenameActivity extends Activity implements OnClickListener, OnEditorActionListener {

	public Button btn_ok, btn_cancel;
	public ExtendedEditText et_name;
	private String ending, orgName;
	private int posInList;
	private File file;
	private String[] VALID_ENDINGS = { ".txt", ".html" };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rename);
	
		Bundle b = getIntent().getExtras();
		orgName = b.getString("name");
		file = (File) b.get("file");
		posInList = b.getInt("pos");
		btn_ok = (Button) findViewById(R.id.btn_rename_ok);
		btn_cancel = (Button) findViewById(R.id.btn_rename_cancel);
		et_name = (ExtendedEditText) findViewById(R.id.et_rename);
		et_name.setOnEditorActionListener(this);
		et_name.setActivity(this);
		
		et_name.setText(orgName);
		et_name.setSelection(orgName.length());
		ending = orgName.substring(orgName.lastIndexOf("."));
		
		btn_ok.setOnClickListener(this);
		btn_cancel.setOnClickListener(this);
	}
	
	@Override
	public void onBackPressed() {
		Intent i = new Intent(this, LibraryActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(i);
		finish();
	}

	@Override
	public void onClick(View v) {
		String name = et_name.getText().toString();
		
		if(name.isEmpty()){
			Toast.makeText(this, "You must supply a name", Toast.LENGTH_SHORT).show();
			return;
		}
		
		boolean isValid = false;
		
		int pos = name.lastIndexOf(".");
		if(pos == -1){
			name = name + ending;
		} else {
			String end = name.substring(pos);
			end = end.toLowerCase();
			for(String s : VALID_ENDINGS){
				if(end.equals(s))
					isValid = true;
			}
		
			if(!isValid)
				name = name + ending;
		}
		
		if(orgName.equals(name)){
			setResult(RESULT_CANCELED);
			finish();
			return;
		}
		
		
		Intent i = new Intent();
		i.putExtra("file", file);
		i.putExtra("name", name);
		i.putExtra("pos", posInList);
		setResult(RESULT_OK, i);
		finish();
		
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if(actionId==EditorInfo.IME_ACTION_DONE){
			btn_ok.callOnClick();
			hideKeyboard();
			return true;
		}
		return false;
	}

	private void hideKeyboard(){
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(btn_ok.getWindowToken(), 0);
	}
}
