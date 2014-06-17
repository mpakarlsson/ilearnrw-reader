package com.example.reader.popups;

import java.util.Locale;

import com.example.reader.LibraryActivity;
import com.example.reader.R;
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

	public Button btnOk, btnCancel;
	public ExtendedEditText etName;
	private String ending, orgName;
	private String[] VALID_ENDINGS = { ".txt", ".html" };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_activity_rename);
	
		Bundle b = getIntent().getExtras();
		orgName = b.getString("name");
		btnOk = (Button) findViewById(R.id.btn_rename_ok);
		btnCancel = (Button) findViewById(R.id.btn_rename_cancel);
		etName = (ExtendedEditText) findViewById(R.id.et_rename);
		etName.setOnEditorActionListener(this);
		etName.setActivity(this);
		
		etName.setText(orgName);
		
		int selectionPos = orgName.indexOf(".") == -1 ? orgName.length() : orgName.indexOf(".");
		etName.setSelection(selectionPos);
		ending = orgName.substring(orgName.lastIndexOf("."));
		
		btnOk.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
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
		
		switch(v.getId()){
		case R.id.btn_rename_ok:
			String name = etName.getText().toString();
			
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
				end = end.toLowerCase(Locale.getDefault());
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
			i.putExtra("name", name);
			i.putExtra("orgName", orgName);
			setResult(RESULT_OK, i);
			finish();
			
			break;
			
		case R.id.btn_rename_cancel:
			setResult(RESULT_CANCELED);
			finish();
			break;
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if(actionId==EditorInfo.IME_ACTION_DONE){
			btnOk.callOnClick();
			hideKeyboard();
			return true;
		}
		return false;
	}

	private void hideKeyboard(){
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(btnOk.getWindowToken(), 0);
	}
}
