package com.example.reader;

import ilearnrw.textadaptation.TextAnnotationModule;
import ilearnrw.textclassification.Word;
import ilearnrw.user.problems.ProblemDefinition;
import ilearnrw.user.problems.ProblemDefinitionIndex;
import ilearnrw.user.problems.ProblemDescription;
import ilearnrw.user.profile.UserProfile;

import java.io.File;
import java.util.ArrayList;

import com.example.reader.interfaces.ColorPickerListener;
import com.example.reader.interfaces.OnHttpListener;
import com.example.reader.interfaces.OnProfileFetched;
import com.example.reader.tasks.ProfileTask;
import com.example.reader.types.ColorPickerDialog;
import com.example.reader.types.BasicListAdapter;
import com.example.reader.types.singleton.AnnotatedWordsSet;
import com.example.reader.types.singleton.ProfileUser;
import com.example.reader.utils.AppLocales;
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

	private LinearLayout colorLayout, container;
	private Button btnOk, btnCancel;
	private TextView chkSwitch;
	private RadioGroup rulesGroup;
	private RadioButton rbtnRule1, rbtnRule2, rbtnRule3, rbtnRule4;
	private Spinner spCategories, spProblems;
	private ImageView colorBox;
	private File fileHtml = null;
	private File fileJSON = null;
	private String html, json, cleanHtml;
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
	private boolean editMode; // or add new rule mode
	
	private UserProfile profile;
	private TextAnnotationModule txModule;
	
	ArrayAdapter<String> problemAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	

        sp = PreferenceManager.getDefaultSharedPreferences(this);
		AppLocales.setLocales(getApplicationContext(), sp.getString(getString(R.string.sp_user_language), "en"));
		
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

			json		= FileHelper.readFromFile(fileJSON);
			html		= FileHelper.readFromFile(fileHtml);
			cleanHtml 	= html;
		} else {
			json 		= bundle.getString("json");
			html 		= bundle.getString("cleanHtml");
			cleanHtml 	= html;
		}
		
		categories 	= new ArrayList<String>();
		problems 	= new ArrayList<String>();
		
		setContentView(R.layout.activity_presentation_module);

		sp.edit().putBoolean(getString(R.string.sp_show_gui), showGUI).commit();
		int id = sp.getInt(getString(R.string.sp_user_id),-1);
		String token = sp.getString(getString(R.string.sp_authToken), "");		
		
		if(id==-1 || token.isEmpty()) {
			finished(); // If you don't have an id something is terribly wrong
			throw new IllegalArgumentException("Missing id or token");
		}

		if(bundle.containsKey("category") && bundle.containsKey("index"))
			init(bundle.getInt("category"), bundle.getInt("index"), true);
		else
			init(0,0, false);
	}
	
	private void init(int category, int index, boolean editMode){
		container		= (LinearLayout) findViewById(R.id.presentation_module_container);
		if(showGUI)
			container.setVisibility(View.VISIBLE);
		else
			container.setVisibility(View.GONE);
		
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
		
		String userId = Integer.toString(sp.getInt(getString(R.string.sp_user_id), 0));
		String token = sp.getString(getString(R.string.sp_authToken), "");
		
		currentCategoryPos 	= category;
		currentProblemPos 	= index;
		updateEnabled(currentCategoryPos, currentProblemPos);
		updateColor(currentCategoryPos, currentProblemPos);
		updateRule(currentCategoryPos, currentProblemPos);

		spCategories.setOnItemSelectedListener(this);
		spProblems.setOnItemSelectedListener(this);
		
		String jsonProfile = sp.getString(getString(R.string.sp_user_profile_json), "");
		
		if(jsonProfile.isEmpty())
			new ProfileTask(this, this, this).run(userId, token);
		else {
			initProfile(jsonProfile);
		}
	}
	
	private void initProfile(String jsonProfile){
		profile = ProfileUser.getInstance(this.getApplicationContext()).getProfile();
		trickyWords = (ArrayList<Word>) profile.getUserProblems().getTrickyWords();		
		
		
		ProblemDefinitionIndex index = profile.getUserProblems().getProblems();
		
		definitions 	= index.getProblemsIndex();
		descriptions 	= index.getProblems();
		
		if(!showGUI){
			btnOk.callOnClick();
			finished();
			return;
		}
		
		categories.clear();
		for(int i=0; i<definitions.length;i++){
			categories.add((i+1) + ". " + definitions[i].getUri());
			//categories.add(getTitle(i));
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
	private String fireTxModule(String html, String json){
		if (txModule==null)
			txModule = new TextAnnotationModule(html);
		
		if (txModule.getPresentationRulesModule() == null)
			txModule.initializePresentationModule(profile);

		for (int i = 0; i < profile.getUserProblems().getNumerOfRows(); i++)
		{
			int problemSize = profile.getUserProblems().getRowLength(i);
			for (int j = 0; j < problemSize; j++)
			{
				String _id = getString(R.string.sp_user_id);
				int color 			= sp.getInt(sp.getInt(_id, 0)+"pm_color_"+i+"_"+j, DEFAULT_COLOR);
				int rule 			= sp.getInt(sp.getInt(_id, 0)+"pm_rule_"+i+"_"+j, DEFAULT_RULE); 
				boolean isChecked 	= sp.getBoolean(sp.getInt(_id, 0)+"pm_enabled_"+i+"_"+j, false);
				
				txModule.getPresentationRulesModule().setPresentationRule(i, j, rule);
				
				txModule.getPresentationRulesModule().setTextColor(i, j, color);
				txModule.getPresentationRulesModule().setHighlightingColor(i, j, color);
				txModule.getPresentationRulesModule().setActivated(i, j, isChecked);
			}
		}

		txModule.setInputHTMLFile(html);
		txModule.setJSonObject(AnnotatedWordsSet.getInstance(this.getApplicationContext(), json).getUserBasedAnnotatedWordsSet());
		
		txModule.annotateText();
		return txModule.getAnnotatedHTMLFile();
	}
	
	/*private String getTitle(int category){
		if (profile.getLanguage() == LanguageCode.GR){
			switch (category){
			case 0:
				return "Συλλαβισμός";
			case 1:
				return "Σύμφωνα";
			case 2:
				return "Φωνήεντα";
			case 3:
				return "Καταλήξεις (παραγωγικές)";
			case 4:
				return "Καταλήξεις (κλίσης)";
			case 5:
				return "Προθέματα";
			case 6:
				return "Γραφή-Ήχος";
			case 7:
				return "Ειδικές Λέξεις";
			case 8:
				return "Ειδικοί Χαρακτήρες";
			}
		}
		else if (profile.getLanguage() == LanguageCode.EN){
			switch (category){
			case 0:
				return "Syllabification";
			case 1:
				return "Vowels";
			case 2:
				return "Suffixing";
			case 3:
				return "Prefixing";
			case 4:
				return "Grapheme-Phoneme";
			case 5:
				return "Lettern Patterns";
			case 6:
				return "Letter Names";
			case 7:
				return "Sight Words";
			case 8:
				return "Confusing Letter Shapes";
			}
		}
		return "";
	}*/
	
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
			sp.edit().putBoolean(sp.getInt(_id, 0)+"pm_enabled_" + currentCategoryPos + "_" + currentProblemPos, true).commit();
			sp.edit().putInt(sp.getInt(_id, 0)+"pm_color_" + currentCategoryPos + "_" + currentProblemPos, currentColor).commit();
			sp.edit().putInt(sp.getInt(_id, 0)+"pm_rule_"+currentCategoryPos+"_"+currentProblemPos, currentRule).commit();
			onBackPressed();
			break;
			
		case R.id.pm_btn_cancel:
			onBackPressed();
			break;
			
		case R.id.pm_color_layout:
			String _id2 = getString(R.string.sp_user_id);
			int color = sp.getInt(sp.getInt(_id2, 0)+"pm_color_" + currentCategoryPos + "_" + currentProblemPos, DEFAULT_COLOR);
			ColorPickerDialog dialog = new ColorPickerDialog(this, color, new ColorPickerListener() {
				@Override
				public void onOk(ColorPickerDialog dialog, int color) {
					//sp.edit().putInt("pm_color_" + currentCategoryPos + "_" + currentProblemPos, color).commit();
					//updateColor(currentCategoryPos, currentProblemPos);
					currentColor = color;
					colorBox.setBackgroundColor(currentColor);
				}
				
				@Override
				public void onCancel(ColorPickerDialog dialog) {}
			});

			if (editMode || !sp.getBoolean(sp.getInt(_id2, 0)+"pm_enabled_"+currentCategoryPos+"_"+currentProblemPos, false)){
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
		if (!editMode && sp.getBoolean(sp.getInt(getString(R.string.sp_user_id), 0)+"pm_enabled_"+currentCategoryPos+"_"+currentProblemPos, false)){
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
			updateProblems(currentCategoryPos);
			updateColor(currentCategoryPos, 0);
			updateRule(currentCategoryPos, 0);
			updateEnabled(currentCategoryPos, 0);
			problemAdapter.notifyDataSetChanged();
			break;
			
		case R.id.problems:
			currentProblemPos = pos;
			updateColor(currentCategoryPos, currentProblemPos);
			updateRule(currentCategoryPos, currentProblemPos);
			updateEnabled(currentCategoryPos, currentProblemPos);
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
	
	private void finished(){
		Intent intent = new Intent(PresentationModule.this, ReaderActivity.class);
		intent.putExtra("html", html);
		intent.putExtra("cleanHtml", cleanHtml);
		intent.putExtra("json", json);
		intent.putExtra("title", name);
		intent.putExtra("trickyWords", (ArrayList<Word>) trickyWords);
		startActivity(intent);
	}

	private void updateColor(int category, int problem){
		currentColor = sp.getInt(sp.getInt(getString(R.string.sp_user_id), 0)+"pm_color_"+category+"_"+problem, DEFAULT_COLOR);
		colorBox.setBackgroundColor(currentColor);
	}
	
	private void updateRule(int category, int problem){
		currentRule = sp.getInt(sp.getInt(getString(R.string.sp_user_id), 0)+"pm_rule_"+category+"_"+problem, DEFAULT_RULE);
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
		if (sp.getBoolean(sp.getInt(getString(R.string.sp_user_id), 0)+"pm_enabled_"+category+"_"+problem, false))
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
