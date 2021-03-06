package com.ilearnrw.reader;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
import ilearnrw.user.problems.ProblemDefinition;
import ilearnrw.user.problems.ProblemDefinitionIndex;
import ilearnrw.user.problems.ProblemDescription;
import ilearnrw.user.profile.UserProfile;

import java.util.ArrayList;

import com.ilearnrw.reader.R;
import com.ilearnrw.reader.interfaces.ColorPickerListener;
import com.ilearnrw.reader.interfaces.OnHttpListener;
import com.ilearnrw.reader.interfaces.OnProfileFetched;
import com.ilearnrw.reader.tasks.ProfileTask;
import com.ilearnrw.reader.types.ColorPickerDialog;
import com.ilearnrw.reader.types.adapters.BasicListAdapter;
import com.ilearnrw.reader.types.singleton.ProfileUser;
import com.ilearnrw.reader.utils.AppLocales;
import com.ilearnrw.reader.utils.Helper;
import com.ilearnrw.reader.utils.HttpHelper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
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

	private RelativeLayout colorLayout;
	private Button btnOk, btnCancel;
	private TextView chkSwitch;
	private RadioGroup rulesGroup;
	private RadioButton rbtnRule1, rbtnRule2, rbtnRule3, rbtnRule4;
	private Spinner spCategories, spProblems;
	private ImageView colorBox;
	
	
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
	private int userId;
	
	private int currentCategoryPos;
	private int currentProblemPos;
	private boolean editMode; // or add new rule mode
	
	private UserProfile profile;
	
	ArrayAdapter<String> problemAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_presentation_module);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        
        rulesGroup 	= (RadioGroup) findViewById(R.id.pm_rules);
		rbtnRule1 	= (RadioButton) findViewById(R.id.pm_rule1);
		rbtnRule2 	= (RadioButton) findViewById(R.id.pm_rule2);
		rbtnRule3 	= (RadioButton) findViewById(R.id.pm_rule3);
		rbtnRule4 	= (RadioButton) findViewById(R.id.pm_rule4);		
		
		colorLayout = (RelativeLayout) findViewById(R.id.pm_color_layout);
		colorBox	= (ImageView) findViewById(R.id.pm_color);
        
		//colorBox.setBackgroundColor(getResources().getColor(R.color.Yellow));
        
		AppLocales.setLocales(getApplicationContext(), sp.getString(getString(R.string.sp_user_language), "en"));
		
		TAG = getClass().getName();
		Bundle bundle 	= getIntent().getExtras();

		categories 	= new ArrayList<String>();
		problems 	= new ArrayList<String>();

		if(bundle.containsKey("category") && bundle.containsKey("index"))
			init(bundle.getInt("category"), bundle.getInt("index"), true);
		else
			init(0,0, false);
	}
	
	private void init(int category, int index, boolean editMode){		
		spCategories 	= (Spinner) findViewById(R.id.categories);
		spProblems 		= (Spinner) findViewById(R.id.problems);

		this.editMode = editMode;
		if (editMode){
			spCategories.setEnabled(false);
			spProblems.setEnabled(false);
		}
		
		btnOk 		= (Button) findViewById(R.id.pm_btn_ok);
		btnCancel 	= (Button) findViewById(R.id.pm_btn_cancel);
		
		chkSwitch	= (TextView) findViewById(R.id.pm_label);
		if (editMode)
			chkSwitch.setText(R.string.edit_mode);
		else 
			chkSwitch.setText(R.string.add_rule_mode);
		
		colorLayout.setOnClickListener(this);		
		btnCancel.setOnClickListener(this);
		btnOk.setOnClickListener(this);
		
		rulesGroup.setOnCheckedChangeListener(this);
		
		currentCategoryPos 	= category;
		currentProblemPos 	= index;
		userId				= sp.getInt(getString(R.string.sp_user_id), 0);
		
		updateEnabled(currentCategoryPos, currentProblemPos);
		updateColor(currentCategoryPos, currentProblemPos);
		updateRule(currentCategoryPos, currentProblemPos);

		updateRadioButtonTexts(currentCategoryPos, currentProblemPos);
		
		spCategories.setOnItemSelectedListener(this);
		spProblems.setOnItemSelectedListener(this);
		
		String jsonProfile = sp.getString(getString(R.string.sp_user_profile_json), "");
		initProfile(jsonProfile);
	}
	
	private void updateRadioButtonTexts(int category, int problem){
		int color = sp.getInt(userId+"pm_color_"+category+"_"+problem, DEFAULT_COLOR);
		CharSequence seq =  rbtnRule1.getText();
		int ind = TextUtils.lastIndexOf(seq, ' ');
		rbtnRule1.setText(Helper.colorString(seq, new ForegroundColorSpan(color), ind+1, ind+2));
		seq = rbtnRule2.getText();
		ind = TextUtils.lastIndexOf(seq, ' ');
		rbtnRule2.setText(Helper.colorString(seq, new ForegroundColorSpan(color), ind+1, seq.length()));
		seq = rbtnRule3.getText();
		ind = TextUtils.lastIndexOf(seq, ' ');
		rbtnRule3.setText(Helper.colorString(seq, new BackgroundColorSpan(color), ind+1, ind+2));
		seq = rbtnRule4.getText();
		ind = TextUtils.lastIndexOf(seq, ' ');
		rbtnRule4.setText(Helper.colorString(seq, new BackgroundColorSpan(color), ind+1, seq.length()));
	}
	
	private void initProfile(String jsonProfile){
		profile = ProfileUser.getInstance(this.getApplicationContext()).getProfile();	
		
		ProblemDefinitionIndex index = profile.getUserProblems().getProblems();
		
		definitions 	= index.getProblemsIndex();
		descriptions 	= index.getProblems();
		
		categories.clear();
		for(int i=0; i<definitions.length;i++){
			categories.add((i+1) + ". " + definitions[i].getUri());
		}
		
		//currentCategoryPos 	= 0;
		updateProblems(currentCategoryPos);
		
		ArrayAdapter<String> categoryAdapter = new BasicListAdapter(this, R.layout.textview_item_multiline, categories, true);
		spCategories.setAdapter(categoryAdapter);
		spCategories.setSelection(currentCategoryPos);
		
		problemAdapter = new BasicListAdapter(this, R.layout.textview_item_multiline, problems, true);
		problemAdapter.notifyDataSetChanged();
		spProblems.setAdapter(problemAdapter);
		spProblems.setSelection(currentProblemPos);
		
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		
		case R.id.pm_btn_ok:
			/*while (!preferences.isEmpty()){
				PreferenceItem pi = preferences.remove(0);
				sp.edit().putBoolean("pm_enabled_" + pi.cat + "_" + pi.idx, pi.enabled).commit();
				sp.edit().putInt("pm_color_" + pi.cat + "_" + pi.idx, pi.color).commit();
				sp.edit().putInt("pm_rule_"+pi.cat+"_"+pi.idx, pi.rule).commit();
			}*/
			String _id = getString(R.string.sp_user_id);
			SharedPreferences.Editor edit = sp.edit();
			edit.putBoolean(sp.getInt(_id, 0)+"pm_enabled_" + currentCategoryPos + "_" + currentProblemPos, true);
			edit.putInt(sp.getInt(_id, 0)+"pm_color_" + currentCategoryPos + "_" + currentProblemPos, currentColor);
			edit.putInt(sp.getInt(_id, 0)+"pm_rule_"+currentCategoryPos+"_"+currentProblemPos, currentRule);
			edit.apply();
			onBackPressed();
			break;
			
		case R.id.pm_btn_cancel:
			onBackPressed();
			break;
			
		case R.id.pm_color_layout:
			int color = sp.getInt(userId+"pm_color_" + currentCategoryPos + "_" + currentProblemPos, DEFAULT_COLOR);
			ColorPickerDialog dialog = new ColorPickerDialog(this, color, new ColorPickerListener() {
				@Override
				public void onOk(ColorPickerDialog dialog, int color) {
					sp.edit().putInt(userId+"pm_color_" + currentCategoryPos + "_" + currentProblemPos, color).apply();
					currentColor = color;
					colorBox.setBackgroundColor(currentColor);
					updateRadioButtonTexts(currentCategoryPos, currentProblemPos);
				}
				
				@Override
				public void onCancel(ColorPickerDialog dialog) {}
			});

			if (editMode || !sp.getBoolean(userId+"pm_enabled_"+currentCategoryPos+"_"+currentProblemPos, false)){
				dialog.show();
			}
			else {
				Toast toast = Toast.makeText(getApplicationContext(), R.string.dont_edit, Toast.LENGTH_SHORT);
				toast.show();
			}
			break;
		
		}
	};
	
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (!editMode && sp.getBoolean(userId+"pm_enabled_"+currentCategoryPos+"_"+currentProblemPos, false)){
			updateRule(currentCategoryPos, currentProblemPos);
			Toast toast = Toast.makeText(getApplicationContext(), R.string.dont_edit, Toast.LENGTH_SHORT);
			toast.show();
			return;
		}
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
			default:
				currentRule = 0;
			}

		default:
			break;
		}
		//sp.edit().putInt("pm_rule_"+currentCategoryPos+"_"+currentProblemPos, currentRule).commit();
		//updateRule(currentCategoryPos, currentProblemPos);
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
		switch(parent.getId()){
		case R.id.categories:
			currentCategoryPos = pos;
			currentProblemPos = 0;
			
			if(!editMode){
				updateProblems(currentCategoryPos);
				updateColor(currentCategoryPos, 0);
				updateRule(currentCategoryPos, 0);
				updateEnabled(currentCategoryPos, 0);
				updateRadioButtonTexts(currentCategoryPos, 0);
			}
			problemAdapter.notifyDataSetChanged();
			break;
			
		case R.id.problems:
			currentProblemPos = pos;
			if(!editMode){
				updateColor(currentCategoryPos, currentProblemPos);
				updateRule(currentCategoryPos, currentProblemPos);
				updateEnabled(currentCategoryPos, currentProblemPos);
				updateRadioButtonTexts(currentCategoryPos, currentProblemPos);
			}
			break;
		
		default:
			break;
		}
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}
	
	@Override
	public void onBackPressed() {
		Intent i = new Intent(this, ActiveRules.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(i);
		finish();
	}

	private void updateColor(int category, int problem){
		currentColor = sp.getInt(userId+"pm_color_"+category+"_"+problem, DEFAULT_COLOR);
		colorBox.setBackgroundColor(currentColor);
	}
	
	private void updateRule(int category, int problem){
		currentRule = sp.getInt(userId+"pm_rule_"+category+"_"+problem, DEFAULT_RULE);
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
	
	private void updateEnabled(int category, int problem){
		//boolean isChecked = sp.getBoolean("pm_enabled_"+category+"_"+problem, false);
		TextView enabledLabel = (TextView) findViewById(R.id.pm__enabled_label);
		if (sp.getBoolean(userId+"pm_enabled_"+category+"_"+problem, false))
			enabledLabel.setText(R.string.rule_enabled);
		else
			enabledLabel.setText(R.string.rule_disabled);
	}
	
	private void updateProblems(int index){
		problemDescriptions = descriptions[index];
		//currentProblemPos	= 0;
		
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
		
		
		//updateColor(currentCategoryPos, currentProblemPos);
		//updateRule(currentCategoryPos, currentProblemPos);
		//updateEnabled(currentCategoryPos, currentProblemPos);
	}
	

	@Override
	public void onTokenExpired(final String... params) {
		if(HttpHelper.refreshTokens(this)){
			final String newToken = sp.getString(getString(R.string.sp_authToken), "");
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					new ProfileTask(PresentationModule.this, PresentationModule.this, PresentationModule.this).run(params[0], newToken);
					Log.d(TAG, getString(R.string.token_error_retry));
					Toast.makeText(PresentationModule.this, getString(R.string.token_error_retry), Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	@Override
	public void onProfileFetched(String result) {
		initProfile(result);
	}
}
