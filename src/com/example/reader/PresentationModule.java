package com.example.reader;

import java.io.File;
import java.util.ArrayList;

import org.apache.http.HttpResponse;

import com.example.reader.interfaces.ColorPickerListener;
import com.example.reader.results.ProbDefsResult;
import com.example.reader.types.ColorPickerDialog;
import com.example.reader.utils.HttpHelper;
import com.google.gson.Gson;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;

public class PresentationModule extends Activity implements OnClickListener, OnCheckedChangeListener{

	LinearLayout colorLayout;
	Button btnOk, btnCancel;
	RadioGroup rulesGroup;
	RadioButton rbtnRule1, rbtnRule2, rbtnRule3, rbtnRule4;
	Spinner spCategories, spProblems;
	ImageView colorBox;
	File file = null;
	File json = null;
	String name = "";
	Boolean showGUI = false;
	
	private final int DEFAULT_COLOR = 0xffff0000;
	private int currentColor;
	private int currentRule;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		Bundle bundle = getIntent().getExtras();
		file = (File)bundle.get("file");
		json = (File)bundle.get("json");
		name = bundle.getString("title");
		showGUI = bundle.getBoolean("showGUI");

	
		if(!showGUI){
			// Todo: Do rules as-is
			finished();
		}
		
		setContentView(R.layout.activity_presentation_module);
	
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		
		int id = sp.getInt("id",-1);
		String token = sp.getString("authToken", "");
		if(id==-1 || token.isEmpty())
			finished(); // If you don't have an id something is terribly wrong
		
		
		// Todo: move
		// Getting 'Access is denied' from the profile information getter, move this code to onPostExecute() when it's not denied
		init();
		
		//new ProblemDefinitionTask().execute(Integer.toString(id), token);
		
	}
	
	private void init(){
		spCategories 	= (Spinner) findViewById(R.id.categories);
		spProblems 		= (Spinner) findViewById(R.id.problems);
		
		btnOk 		= (Button) findViewById(R.id.pm_btn_ok);
		btnCancel 	= (Button) findViewById(R.id.pm_btn_cancel);
		
		rulesGroup 	= (RadioGroup) findViewById(R.id.pm_rules);
		rbtnRule1 	= (RadioButton) findViewById(R.id.pm_rule1);
		rbtnRule2 	= (RadioButton) findViewById(R.id.pm_rule2);
		rbtnRule3 	= (RadioButton) findViewById(R.id.pm_rule3);
		rbtnRule4 	= (RadioButton) findViewById(R.id.pm_rule4);
		
		colorLayout = (LinearLayout) findViewById(R.id.pm_color_layout);
		colorBox	= (ImageView) findViewById(R.id.pm_color);
		
		btnCancel.setOnClickListener(this);
		btnOk.setOnClickListener(this);
		colorLayout.setOnClickListener(this);
		
		rulesGroup.setOnCheckedChangeListener(this);
		
		updateColor(0, 0);
		colorBox.setBackgroundColor(currentColor);

		
		
		// Todo:
		// Figure out if it's best to use SharedPreferences or to implement a database that contains all this information, every problem is going to need 2 fields
		// 'color' and 'rule'

		// Could be something like this in SharedPreferences (key value)
		// naming: category-problem-color e.g. 1-2-color
		// rules: category-problem-rule e.g 0-3-rule

	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		
		case R.id.pm_btn_ok:
			finished();
			break;
			
		case R.id.pm_btn_cancel:
			onBackPressed();
			break;
			
		case R.id.pm_color_layout:
			int color = PreferenceManager.getDefaultSharedPreferences(this).getInt("problemColor#1", currentColor);
			ColorPickerDialog dialog = new ColorPickerDialog(this, color, new ColorPickerListener() {
				@Override
				public void onOk(ColorPickerDialog dialog, int color) {
					// Save the picked color either into the SP or DB
					currentColor = color;
					colorBox.setBackgroundColor(color);
				}
				
				@Override
				public void onCancel(ColorPickerDialog dialog) {}
			});
			
			dialog.show();
			break;
		
		}
	};
	
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (group.getId()) {
		case R.id.pm_rules:
			
			switch(group.getCheckedRadioButtonId()){
			case R.id.pm_rule1:
				currentRule = 1;
				break;
				
			case R.id.pm_rule2:
				currentRule = 2;
				break;

			case R.id.pm_rule3:
				currentRule = 3;
				break;
			
			case R.id.pm_rule4:
				currentRule = 4;
				break;
			}

		default:
			break;
		}
	}	
	
	@Override
	public void onBackPressed() {
		Intent i = new Intent(this, LibraryActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(i);
		finish();
	}
	
	private void finished(){
		Intent intent = new Intent(PresentationModule.this, ReaderActivity.class);
		intent.putExtra("file", file);
		intent.putExtra("json", json);
		intent.putExtra("title", name);
		startActivity(intent);
	}

	private void updateColor(int category, int problem){
		// Todo: Get the color that has been set for the problem
		// if no color has been set, set it to DEFAULT_COLOR
		
		currentColor =  DEFAULT_COLOR;
	}
	
	private class ProblemDefinitionTask extends AsyncTask<String, Void, ProbDefsResult>{
		private ProgressDialog dialog;
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(PresentationModule.this);
			dialog.setTitle(getString(R.string.dialog_fetch_user_title));
			dialog.setMessage(getString(R.string.dialog_fetch_user_message));
			dialog.setCancelable(true);
			dialog.setCanceledOnTouchOutside(false);
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					cancel(true);
					dialog.dismiss();
				}
			});
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(true);
					dialog.dismiss();
				}
			});
			dialog.show();
			super.onPreExecute();
		}
		
		@Override
		protected ProbDefsResult doInBackground(String... params) {
			HttpResponse response = HttpHelper.get("http://api.ilearnrw.eu/ilearnrw/profile?userId=" + params[0] + "&token=" + params[1]);
		
			ArrayList<String> data = HttpHelper.handleResponse(response);
			
			if(data.size()==1){
				return null;
			} else {
				ProbDefsResult res = null;
				try {
					res = new Gson().fromJson(data.get(1), ProbDefsResult.class);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return res;
			}
		}
		
		@Override
		protected void onPostExecute(ProbDefsResult result) {
			if(dialog.isShowing()) {
				dialog.dismiss();
			}
			
			if(result != null){
				
			}
		}
	}
	
}
