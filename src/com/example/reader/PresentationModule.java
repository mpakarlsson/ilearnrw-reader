package com.example.reader;

import ilearnrw.textclassification.Word;
import ilearnrw.user.problems.ProblemDefinition;
import ilearnrw.user.problems.ProblemDefinitionIndex;
import ilearnrw.user.problems.ProblemDescription;

import java.io.File;
import java.util.ArrayList;

import com.example.reader.interfaces.ColorPickerListener;
import com.example.reader.interfaces.OnHttpListener;
import com.example.reader.interfaces.OnProfileFetched;
import com.example.reader.results.ProfileResult;
import com.example.reader.tasks.ProfileTask;
import com.example.reader.types.ColorPickerDialog;
import com.example.reader.types.BasicListAdapter;
import com.example.reader.utils.FileHelper;
import com.example.reader.utils.HttpHelper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;

public class PresentationModule 
	extends 
		Activity 
	implements 
		OnClickListener, 
		OnCheckedChangeListener,
		OnHttpListener,
		OnProfileFetched,
		OnItemSelectedListener {

	private LinearLayout colorLayout;
	private Button btnOk, btnCancel;
	private RadioGroup rulesGroup;
	private RadioButton rbtnRule1, rbtnRule2, rbtnRule3, rbtnRule4;
	private Spinner spCategories, spProblems;
	private ImageView colorBox;
	private File fileHtml = null;
	private File fileJSON = null;
	private String html, json;
	private String name = "";
	private Boolean showGUI = false;
	
	private ArrayList<Word> trickyWords;
	
	private SharedPreferences sp;
	
	private String TAG;
	
	private ProblemDefinition[] definitions;
	private ProblemDescription[][] descriptions;
	private ProblemDescription[] problemDescriptions;
	
	private ArrayList<String> categories;
	private ArrayList<String> problems;
	
	private final int DEFAULT_COLOR = 0xffff0000;
	private final int DEFAULT_RULE	= 3;
	private int currentColor;
	private int currentRule;
	
	private int currentCategoryPos;
	private int currentProblemPos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		TAG = getClass().getName();
		
		Bundle bundle 	= getIntent().getExtras();
		
		boolean loadFiles = true;
		if(bundle.containsKey("loadFiles"))
			loadFiles = bundle.getBoolean("loadFiles");
		
		name 			= bundle.getString("title", "");
		showGUI 		= bundle.getBoolean("showGUI", false);
		trickyWords		= new ArrayList<Word>();
		
		if(loadFiles){
			fileHtml 		= (File)bundle.get("file");
			fileJSON 		= (File)bundle.get("json");
			
			html	= FileHelper.readFromFile(fileHtml);
			json	= FileHelper.readFromFile(fileJSON);
		} else {
			html = bundle.getString("html");
			json = bundle.getString("json");
		}
		
		categories 	= new ArrayList<String>();
		problems 	= new ArrayList<String>();
		
		setContentView(R.layout.activity_presentation_module);
		
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.edit().putBoolean("showGUI", showGUI).commit();
		int id = sp.getInt("id",-1);
		String token = sp.getString("authToken", "");
		if(id==-1 || token.isEmpty()) {
			finished(); // If you don't have an id something is terribly wrong
			throw new IllegalArgumentException("Missing id or token");
		}
		init();
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
		
		String userId = Integer.toString(sp.getInt("id", 0));
		String token = sp.getString("authToken", "");
		
		currentCategoryPos 	= 0;
		currentProblemPos 	= 0;
		updateColor(currentCategoryPos, currentProblemPos);
		updateRule(currentCategoryPos, currentProblemPos);

		spCategories.setOnItemSelectedListener(this);
		spProblems.setOnItemSelectedListener(this);
		
		new ProfileTask(this, showGUI, this, this).run(userId, token);

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
			int color = sp.getInt("pm_color_" + currentCategoryPos + "_" + currentProblemPos, DEFAULT_COLOR);
			ColorPickerDialog dialog = new ColorPickerDialog(this, color, new ColorPickerListener() {
				@Override
				public void onOk(ColorPickerDialog dialog, int color) {
					sp.edit().putInt("pm_color_" + currentCategoryPos + "_" + currentProblemPos, color).commit();
					updateColor(currentCategoryPos, currentProblemPos);
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
		
		sp.edit().putInt("pm_rule_"+currentCategoryPos+"_"+currentProblemPos, currentRule).commit();
		updateRule(currentCategoryPos, currentProblemPos);
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
		switch(parent.getId()){
		case R.id.categories:
			currentCategoryPos = pos;
			updateProblems(currentCategoryPos);
			updateRule(currentCategoryPos, currentProblemPos);
			break;
			
		case R.id.problems:
			currentProblemPos = pos;
			updateColor(currentCategoryPos, currentProblemPos);
			updateRule(currentCategoryPos, currentProblemPos);
			break;
		
		default:
			break;
		}
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}
	
	@Override
	public void onBackPressed() {
		Intent i = new Intent(this, LibraryActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(i);
		finish();
	}
	
	private void finished(){
		Intent intent = new Intent(PresentationModule.this, ReaderActivity.class);
		intent.putExtra("html", html);
		intent.putExtra("json", json);
		intent.putExtra("title", name);
		intent.putExtra("trickyWords", (ArrayList<Word>) trickyWords);
		startActivity(intent);
	}

	private void updateColor(int category, int problem){
		currentColor = sp.getInt("pm_color_"+category+"_"+problem, DEFAULT_COLOR);
		colorBox.setBackgroundColor(currentColor);
	}
	
	private void updateRule(int category, int problem){		
		currentRule = sp.getInt("pm_rule_"+category+"_"+problem, DEFAULT_RULE);
		switch(currentRule){
		case 1:
			rbtnRule1.setChecked(true);
			break;
		case 2:
			rbtnRule2.setChecked(true);
			break;
		case 3:
			rbtnRule3.setChecked(true);
			break;
		case 4:
			rbtnRule4.setChecked(true);
			break;
		}
	}
	
	private void updateProblems(int index){
		problemDescriptions = descriptions[index];
		currentProblemPos	= 0;
		
		problems.clear();
		for(int i=0; i<problemDescriptions.length; i++){
			String[] descriptions = problemDescriptions[i].getDescriptions();
			
			String str = "";
			for(int j=0; j<descriptions.length; j++){
				if(j+1<descriptions.length)
					str += descriptions[j] + " | ";
				else 
					str += descriptions[j];
			}
			
			problems.add((i+1) + ". " + str);
		}
		
		ArrayAdapter<String> problemAdapter = new BasicListAdapter(this, R.layout.textview_item_multiline, problems, true);
		problemAdapter.notifyDataSetChanged();
		spProblems.setAdapter(problemAdapter);
		
	}
	

	@Override
	public void onTokenExpired(final String... params) {
		if(HttpHelper.refreshTokens(this)){
			final String newToken = sp.getString("authToken", "");
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					new ProfileTask(PresentationModule.this, showGUI, PresentationModule.this, PresentationModule.this).run(params[0], newToken);
					Log.d(TAG, getString(R.string.token_error_retry));
					Toast.makeText(PresentationModule.this, getString(R.string.token_error_retry), Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	@Override
	public void onProfileFetched(ProfileResult profile) {
		trickyWords = (ArrayList<Word>) profile.userProblems.getTrickyWords();
		
		if(!showGUI){
			finished();
			return;
		}
		
		ProblemDefinitionIndex index 		= profile.userProblems.getProblems();
		
		definitions 	= index.getProblemsIndex();
		descriptions 	= index.getProblems();
		
		categories.clear();
		for(int i=0; i<definitions.length;i++){
				categories.add((i+1) + ". " + definitions[i].getType().getUrl());
		}
		
		currentCategoryPos 	= 0;
		updateProblems(currentCategoryPos);
		
		ArrayAdapter<String> categoryAdapter = new BasicListAdapter(this, R.layout.textview_item_multiline, categories, true);
		spCategories.setAdapter(categoryAdapter);
		
	}
	
}
