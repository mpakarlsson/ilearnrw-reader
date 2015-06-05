package com.ilearnrw.reader.tasks;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
import ilearnrw.textadaptation.TextAnnotationModule;
import ilearnrw.textclassification.Word;
import ilearnrw.user.profile.UserProblems;
import ilearnrw.user.profile.UserProfile;

import java.io.File;
import java.util.ArrayList;

import com.ilearnrw.reader.R;
import com.ilearnrw.reader.ReaderActivity;
import com.ilearnrw.reader.types.Pair;
import com.ilearnrw.reader.types.SystemTags;
import com.ilearnrw.reader.types.singleton.AnnotatedWordsSet;
import com.ilearnrw.reader.types.singleton.ProfileUser;
import com.ilearnrw.reader.utils.FileHelper;
import com.ilearnrw.reader.utils.Helper;
import com.ilearnrw.reader.utils.HttpHelper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class OpenBookTask extends AsyncTask<String, Void, ArrayList<String>> {
	private ProgressDialog dialog;
	private Context context;
	private String filename;
	private Pair<File> libItem;
	private boolean isRulesActivated;
	private SharedPreferences preferences;
	
	private UserProfile profile;

	private TextAnnotationModule txModule;
	private final int DEFAULT_COLOR = 0xffff0000;
	private final int DEFAULT_RULE	= 3;
	
	private String activeRules;
	
	public OpenBookTask(){
	}
	
	public OpenBookTask(Context context, Pair<File> libItem, boolean isRulesActivated){
		this.context = context;
		this.filename = libItem.first().getName();
		this.libItem = libItem;
		this.isRulesActivated = isRulesActivated;
		this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
		activeRules = "";
	}
	
	public void run(String... params){
		this.execute(params);
	}

	@Override
	protected void onPreExecute() {
		dialog = new ProgressDialog(context);
		dialog.setTitle(filename);
		dialog.setMessage(context.getString(R.string.dialog_open_book_summary));
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
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
	protected ArrayList<String> doInBackground(String... params) {
		profile = ProfileUser.getInstance(context.getApplicationContext()).getProfile();
		
		String clean = libItem.first().getName();
		String json = FileHelper.readFromFile(libItem.second());
		String name = clean;
		
		AnnotatedWordsSet.getInstance(context.getApplicationContext()).initUserBasedAnnotatedWordsSet(json, name);
		
		String html = clean;
		if(isRulesActivated)
			fireTxModule(FileHelper.readFromFile(libItem.first()), json);
		
		ArrayList<String> results = new ArrayList<String>();
		results.add(html);
		results.add(name);
		return results;
	}

	@Override
	protected void onPostExecute(ArrayList<String> results) {
		if(dialog.isShowing()) {
			dialog.dismiss();
		}
		
		Intent intent = new Intent(context, ReaderActivity.class);
		intent.putExtra("html", results.get(0));
		intent.putExtra("title", results.get(1));
		intent.putExtra("trickyWords", (ArrayList<Word>) profile.getUserProblems().getTrickyWords());
		intent.putExtra("annotated", isRulesActivated ? true : false);
		context.startActivity(intent);
		super.onPostExecute(results);
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
				String id 			= context.getString(R.string.sp_user_id);
				int color 			= preferences.getInt(preferences.getInt(id, 0)+"pm_color_"+i+"_"+j, DEFAULT_COLOR);
				int rule 			= preferences.getInt(preferences.getInt(id, 0)+"pm_rule_"+i+"_"+j, DEFAULT_RULE); 
				boolean isChecked 	= preferences.getBoolean(preferences.getInt(id, 0)+"pm_enabled_"+i+"_"+j, false);
				
				if(isChecked){
					UserProblems up = txModule.getPresentationRulesModule().getUserProfile().getUserProblems();
					activeRules += up.getProblemDefinition(i).getUri().toString() + " ";					
					activeRules += Helper.convertStringArray(up.getProblemDescription(i, j).getDescriptions(), " ");
					activeRules += Helper.colorToHex(color) + ", ";
				}
				
				txModule.getPresentationRulesModule().setPresentationRule(i, j, rule);
				
				txModule.getPresentationRulesModule().setTextColor(i, j, color);
				txModule.getPresentationRulesModule().setHighlightingColor(i, j, color);
				txModule.getPresentationRulesModule().setActivated(i, j, isChecked);
			}
		}
		
		if(!activeRules.isEmpty()){
			activeRules = activeRules.substring(0, activeRules.length()-2);
			
			HttpHelper.log(context, "Active rules: " + activeRules, SystemTags.APP_READ_RULES_ACTIVATED);
			
		} else {
			isRulesActivated = false;
			return html;
		}
		
		txModule.setInputHTMLFile(html);
		txModule.setJSonObject(AnnotatedWordsSet.getInstance(context.getApplicationContext()).getUserBasedAnnotatedWordsSet());
		
		txModule.annotateText();
		
		File tempDir = context.getDir(context.getString(R.string.temp_location), Activity.MODE_PRIVATE);
		FileHelper.saveFile(txModule.getAnnotatedHTMLFile(), new File(tempDir, "annotatedData.txt"));
		
		return txModule.getAnnotatedHTMLFile();
	}
}
