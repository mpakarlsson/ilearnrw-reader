package com.example.reader.tasks;

import ilearnrw.textadaptation.TextAnnotationModule;
import ilearnrw.textclassification.Word;
import ilearnrw.user.profile.UserProfile;

import java.io.File;
import java.util.ArrayList;

import com.example.reader.R;
import com.example.reader.ReaderActivity;
import com.example.reader.types.Pair;
import com.example.reader.types.singleton.AnnotatedWordsSet;
import com.example.reader.types.singleton.ProfileUser;
import com.example.reader.utils.FileHelper;

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
	
	public OpenBookTask(){
	}
	
	public OpenBookTask(Context context, Pair<File> libItem, boolean isRulesActivated){
		this.context = context;
		this.filename = libItem.first().getName();
		this.libItem = libItem;
		this.isRulesActivated = isRulesActivated;
		this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
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
		
		String clean = FileHelper.readFromFile(libItem.first());
		String json = FileHelper.readFromFile(libItem.second());
		String name = libItem.first().getName();
		
		AnnotatedWordsSet.getInstance(context.getApplicationContext()).initUserBasedAnnotatedWordsSet(json, name);
		
		String html = clean;
		if(isRulesActivated)
			html = fireTxModule(clean, json);
		
		ArrayList<String> results = new ArrayList<String>();
		results.add(html);
		results.add(clean);
		results.add(json);
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
		intent.putExtra("cleanHtml", results.get(1));
		intent.putExtra("json", results.get(2));
		intent.putExtra("title", results.get(3));
		intent.putExtra("trickyWords", (ArrayList<Word>) profile.getUserProblems().getTrickyWords());
		
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
				
				txModule.getPresentationRulesModule().setPresentationRule(i, j, rule);
				
				txModule.getPresentationRulesModule().setTextColor(i, j, color);
				txModule.getPresentationRulesModule().setHighlightingColor(i, j, color);
				txModule.getPresentationRulesModule().setActivated(i, j, isChecked);
			}
		}
		txModule.setInputHTMLFile(html);
		txModule.setJSonObject(AnnotatedWordsSet.getInstance(context.getApplicationContext()).getUserBasedAnnotatedWordsSet());
		
		txModule.annotateText();
		return txModule.getAnnotatedHTMLFile();
	}
}