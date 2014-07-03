package com.example.reader.popups;

import ilearnrw.textclassification.Word;
import ilearnrw.user.profile.UserProfile;

import java.util.ArrayList;
import java.util.Arrays;

import com.example.reader.R;
import com.example.reader.ReaderActivity;
import com.example.reader.interfaces.OnHttpListener;
import com.example.reader.interfaces.OnProfileFetched;
import com.example.reader.interfaces.OnTextToSpeechComplete;
import com.example.reader.interfaces.OnTrickyWordListener;
import com.example.reader.tasks.ProfileTask;
import com.example.reader.tasks.TrickyWordsTask;
import com.example.reader.texttospeech.TextToSpeechBase;
import com.example.reader.types.WordPopupAdapter;
import com.example.reader.utils.HttpHelper;
import com.google.gson.Gson;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class WordActivity 
	extends 
		Activity 
	implements
		OnTextToSpeechComplete,
		OnHttpListener,
		OnTrickyWordListener,
		OnProfileFetched {

	
	private final String TAG = getClass().getName();
	
	private TextView tvTitle;
	private ImageView ivSpeak;
	private ListView list;
	private Button btnAddTrickyWord, btnOk;
	private TextToSpeechBase tts;
	
	private SharedPreferences sp;
	
	private ArrayList<Word> trickyWords;
	private Word currentWord;
	private String strWord;
	private int currentIndex;
	private Boolean isAdding;
	private Boolean isFetchingProfile;
	
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_activity_word);
		
		Intent checkTTSIntent = new Intent(); 
		checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, ReaderActivity.FLAG_CHECK_TTS);
		
		Bundle b = getIntent().getExtras();
		
		tvTitle 			= (TextView) findViewById(R.id.tv_word_title);
		ivSpeak 			= (ImageView) findViewById(R.id.iv_word_speak);
		list 				= (ListView) findViewById(R.id.lv_word_info);
		btnAddTrickyWord 	= (Button) findViewById(R.id.btn_add_tricky_word);
		btnOk				= (Button) findViewById(R.id.btn_word_ok);
		
		String def = getResources().getString(R.string.default_text);
		strWord = b.getString("word", def);
		tvTitle.setText(strWord);

		
		String wordInSyllables 	= b.getString("wordInSyllables", "-"+strWord+"-");
		String stem 			= b.getString("stem", strWord);
		ArrayList<Integer> problems = b.getIntegerArrayList("problems");
		trickyWords					= (ArrayList<Word>) b.get("trickyWords");
		
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		final int id = sp.getInt("id",-1);
		final String token = sp.getString("authToken", "");
		if(id==-1 || token.isEmpty()) {
			throw new IllegalArgumentException("Missing id or token");
		}
		
		setWord(strWord);
		
		ivSpeak.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tts.speak(tvTitle.getText().toString());
			}
		});
		ivSpeak.setEnabled(false);
		
		btnAddTrickyWord.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(currentWord == null){
					isAdding = true;
					new TrickyWordsTask(WordActivity.this, true, WordActivity.this, WordActivity.this).run(Integer.toString(id), strWord, token);
				} else {
					isAdding = false;
					new TrickyWordsTask(WordActivity.this, false, WordActivity.this, WordActivity.this).run(Integer.toString(id), strWord, token);
				}
			}
		});
		
		btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent data = new Intent();
				data.putExtra("trickyWords", trickyWords);
				setResult(RESULT_OK, data);
				finish();
			}
		});
		
		ArrayList<String> objects 		= new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.word_information)));
		ArrayList<Spannable> items		= new ArrayList<Spannable>();
		items.add(new SpannableString(stem));
		items.add(new SpannableString(wordInSyllables));
		
		
		Spannable currSpan = null;
		int pos=-1;
		
		ArrayList<Spannable> spans = new ArrayList<Spannable>();
		for(int i=problems.size()-1; i>=0; i-=3){
			int oldPos = pos;
			pos 	= problems.get(i-2);
			int start 	= problems.get(i-1);
			int end 	= problems.get(i);
			
			if(pos==oldPos){
				currSpan.setSpan(new BackgroundColorSpan(Color.YELLOW), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			} else{
				if(currSpan!=null)
					spans.add(currSpan);
				currSpan = new SpannableString(strWord + "\n");
				currSpan.setSpan(new BackgroundColorSpan(Color.YELLOW), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			}
		}
		spans.add(currSpan);

		CharSequence seq = TextUtils.concat(spans.toArray(new Spannable[spans.size()]));
		
		Spannable span = seq == null ? new SpannableString("-") : new SpannableString(seq);
		items.add(span);
		
		ArrayAdapter<String> adapter = new WordPopupAdapter(this, R.layout.row_word_popup, objects, items, true);
		list.setAdapter(adapter);
		
		isFetchingProfile = false;
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case ReaderActivity.FLAG_CHECK_TTS:
			if(resultCode == RESULT_OK || resultCode == RESULT_FIRST_USER) {
				tts = new TextToSpeechBase(this, this, requestCode, resultCode, data);
			}
			
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
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
	protected void onDestroy() {
		tts.destroy();
		super.onDestroy();
	}

	private void setWord(String word){
		currentWord 	= null;
		currentIndex 	= -1;
		isAdding 		= true;
		btnAddTrickyWord.setText(getResources().getString(R.string.add_tricky_word));
		
		
		for(int i=0; i<trickyWords.size(); i++){
			Word w = trickyWords.get(i);
			if(w==null)
				continue;
			
			if(w.getWord().equals(word)){
				currentWord 	= w;
				currentIndex 	= i;
				isAdding		= false;
				btnAddTrickyWord.setText(getResources().getString(R.string.remove_tricky_word));
				break;
			}
		}
		
	}

	@Override
	public void onTokenExpired(final String... params) {
		if(HttpHelper.refreshTokens(this)){
			final String newToken = PreferenceManager.getDefaultSharedPreferences(this).getString("authToken", "");
			
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(!isFetchingProfile){
						new TrickyWordsTask(WordActivity.this, isAdding, WordActivity.this, WordActivity.this).run(params[0], params[1], newToken);
					}
					else {
						new ProfileTask(WordActivity.this, WordActivity.this, WordActivity.this).run(params[0], newToken);
						isFetchingProfile = false;
					} 
					Log.d(TAG, getString(R.string.token_error_retry));
					Toast.makeText(WordActivity.this, getString(R.string.token_error_retry), Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	@Override
	public void onTrickyWord(Boolean success) {
		if(success){
			if(isAdding){
				Toast.makeText(this, getString(R.string.success_added_word), Toast.LENGTH_SHORT).show();
				btnAddTrickyWord.setText(getResources().getString(R.string.remove_tricky_word));
				
				trickyWords.add(currentWord);
			}
			else {
				Toast.makeText(this, getString(R.string.success_removed_word), Toast.LENGTH_SHORT).show();
				btnAddTrickyWord.setText(getResources().getString(R.string.add_tricky_word));
				
				if(currentIndex != -1)
					trickyWords.remove(currentIndex);
			}
			//setWord(strWord);
			
		} else {
			if(isAdding)
				Toast.makeText(this, getString(R.string.failed_add_word), Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(this, getString(R.string.failed_remove_word), Toast.LENGTH_SHORT).show();
		}
		
		
		// TODO: Change so server returns the word that has been added instead of "ok" and then remove this call to get profile information
		btnOk.setEnabled(false);
		String userId = Integer.toString(sp.getInt("id", 0));
		String token = sp.getString("authToken", "");
		isFetchingProfile = true;
		new ProfileTask(this, this, this).run(userId, token);
	}


	@Override
	public void onProfileFetched(String result) {
		
		UserProfile profile = new Gson().fromJson(result, UserProfile.class);
		
		trickyWords = (ArrayList<Word>) profile.getUserProblems().getTrickyWords();
		
		if(isAdding){
			btnAddTrickyWord.setText(getResources().getString(R.string.remove_tricky_word));
		}
		else {
			btnAddTrickyWord.setText(getResources().getString(R.string.add_tricky_word));
		}
		setWord(strWord);
		btnOk.setEnabled(true);
	}
	
	
}
