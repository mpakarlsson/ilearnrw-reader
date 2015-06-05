package com.ilearnrw.reader.popups;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
import ilearnrw.textclassification.Word;
import ilearnrw.user.profile.UserProfile;

import java.util.ArrayList;
import java.util.Locale;

import com.ilearnrw.reader.R;
import com.ilearnrw.reader.interfaces.OnTextToSpeechComplete;
import com.ilearnrw.reader.texttospeech.TextToSpeechReader;
import com.ilearnrw.reader.types.adapters.WordPopupDetailsAdapter;
import com.ilearnrw.reader.types.singleton.ProfileUser;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class WordActivity 
	extends 
		Activity
	implements
		OnTextToSpeechComplete{
	
	private TextView tvTitle;
	private ImageView ivSpeak;
	private Button btnOk;
	
	private ArrayList<Word> trickyWords;
	private String strWord;
	
	//private TextToSpeechReaderBase tts;
	
	private TextView tvStemTitle, tvStemInfo, tvSyllablesTitle, tvSyllablesInfo, tvSoundsTitle, tvSoundsInfo, tvPhonicsTitle;
	private ListView lvPhonics;
	private WordPopupDetailsAdapter detailsAdapter;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_activity_word);
		
		//tts = new TextToSpeechReaderBase(this, this, Locale.UK);
		
		Bundle b = getIntent().getExtras();
		tvTitle 			= (TextView) findViewById(R.id.tv_word_title);
		ivSpeak 			= (ImageView) findViewById(R.id.iv_word_speak);
		
		
		tvStemTitle			= (TextView) findViewById(R.id.tv_wp_stem_title);
		tvStemInfo			= (TextView) findViewById(R.id.tv_wp_stem_info);
		
		tvSyllablesTitle 	= (TextView) findViewById(R.id.tv_wp_syllables_title);
		tvSyllablesInfo 	= (TextView) findViewById(R.id.tv_wp_syllables_info);
		
		tvSoundsTitle		= (TextView) findViewById(R.id.tv_wp_sounds_title);
		tvSoundsInfo		= (TextView) findViewById(R.id.tv_wp_sounds_info);
		
		tvPhonicsTitle		= (TextView) findViewById(R.id.tv_wp_phonics_title);
		lvPhonics			= (ListView) findViewById(R.id.lv_wp_phonics_info);
		
		btnOk				= (Button) findViewById(R.id.btn_word_ok);
		
		String def = getResources().getString(R.string.default_text);
		strWord = b.getString("word", def);
		tvTitle.setText(strWord);
		
		tvStemTitle.setText(getString(R.string.wp_stem));
		tvSyllablesTitle.setText(getString(R.string.wp_syllables));
		tvSoundsTitle.setText(getString(R.string.wp_sounds));
		tvPhonicsTitle.setText(getString(R.string.wp_phonics));
		
		tvStemTitle.setTypeface(tvStemTitle.getTypeface(), Typeface.BOLD);
		tvSyllablesTitle.setTypeface(tvSyllablesTitle.getTypeface(), Typeface.BOLD);
		tvSoundsTitle.setTypeface(tvSoundsTitle.getTypeface(), Typeface.BOLD);
		tvPhonicsTitle.setTypeface(tvPhonicsTitle.getTypeface(), Typeface.BOLD);
		
		
		String wordInSyllables 	= b.getString("wordInSyllables", strWord.toLowerCase(Locale.getDefault()));
		String stem 			= b.getString("stem", strWord);
		String phoneme			= b.getString("phoneme", "-");
		ArrayList<Integer> problems = b.getIntegerArrayList("problems");
		ArrayList<String> datas		= b.getStringArrayList("data");
		trickyWords					= (ArrayList<Word>) b.get("trickyWords");
		
		StringBuilder sb =  new StringBuilder(wordInSyllables);
		
		if(sb.charAt(0) == '-')
			sb.deleteCharAt(0);
		
		if(sb.charAt(sb.length()-1) == '-')
			sb.deleteCharAt(sb.length()-1);
		
		wordInSyllables = sb.toString().toLowerCase(Locale.getDefault());
		
		tvStemInfo.setText(stem);		
		tvSyllablesInfo.setText(wordInSyllables);
		
		if(phoneme.isEmpty())
			phoneme = "-";
		
		tvSoundsInfo.setText(phoneme);
		
		
		ivSpeak.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TextToSpeechReader.getInstance(getApplicationContext()).speak(tvTitle.getText().toString());
			}
		});
		ivSpeak.setEnabled(true);
		
		btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent data = new Intent();
				data.putExtra("trickyWords", trickyWords);
				setResult(RESULT_OK, data);
				finish();
			}
		});
		
		Spannable currSpan = null;
		int pos=-1;
		
		ArrayList<Spannable> spans = new ArrayList<Spannable>();
		int currentCategory = -1;
		detailsAdapter = new WordPopupDetailsAdapter(this);
		UserProfile p = ProfileUser.getInstance(this.getApplicationContext()).getProfile();
		
		for(int i=problems.size()-1; i>=0; i-=3){
			int oldPos = pos;
			pos 	= problems.get(i-2);
			int start 	= problems.get(i-1);
			int end 	= problems.get(i);
			
			int category = Integer.valueOf(datas.get(i-1));
			int index = Integer.valueOf(datas.get(i));

			String[] descriptions =  p.getUserProblems().getProblemDescription(category, index).getHumanReadableDescription().split("<>");
			
			if(pos==oldPos){
				currSpan.setSpan(new BackgroundColorSpan(Color.YELLOW), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			} else {
				if(currSpan!=null)
					spans.add(currSpan);
				//currSpan = new SpannableString(strWord + " - Problem(" + datas.get(i-1) + "," + datas.get(i) + ") " + datas.get(i-2) + "\n");
				
				
				if(category != currentCategory){
					currentCategory = category;
					detailsAdapter.addSectionHeader(new SpannableString(p.getUserProblems().getProblemDefinition(category).getUri()));
				}
				
				
				
				currSpan = new SpannableString(strWord);
				currSpan.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.LightYellow)), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
				
			}
			
			detailsAdapter.addItem(currSpan, descriptions[0]);
		}
		lvPhonics.setAdapter(detailsAdapter);
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}

	@Override
	public void onTextToSpeechInitialized() {
		ivSpeak.setEnabled(true);
	}

	@Override
	public void onTextToSpeechInstall() {
	}

	@Override
	protected void onDestroy() {
		//tts.destroy();
		super.onDestroy();
	}
	
	
}
