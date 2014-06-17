package com.example.reader.popups;

import java.util.ArrayList;
import java.util.Arrays;

import com.example.reader.R;
import com.example.reader.types.WordPopupAdapter;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class WordActivity extends Activity {

	private TextView tvTitle;
	private ImageView ivSpeak;
	private ListView list;
	private Button btnAddTrickyWord, btnOk;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_activity_word);
		
		Bundle b = getIntent().getExtras();
		
		tvTitle 			= (TextView) findViewById(R.id.tv_word_title);
		ivSpeak 			= (ImageView) findViewById(R.id.iv_word_speak);
		list 				= (ListView) findViewById(R.id.lv_word_info);
		btnAddTrickyWord 	= (Button) findViewById(R.id.btn_add_tricky_word);
		btnOk				= (Button) findViewById(R.id.btn_word_ok);
		
		String def = getResources().getString(R.string.default_text);
		String word = b.getString("word", def);
		tvTitle.setText(word);

		
		String wordInSyllables 	= b.getString("wordInSyllables", "-"+word+"-");
		String stem 			= b.getString("stem", word);
		ArrayList<Integer> problems = b.getIntegerArrayList("problems");
		
		ivSpeak.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Todo: remake tts into a service
			}
		});
		
		btnAddTrickyWord.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Todo: add tricky word
			}
		});
		
		btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		ArrayList<String> objects = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.word_information)));
		ArrayList<Spannable> items		= new ArrayList<Spannable>();		//new ArrayList<String>(Arrays.asList("a", "b", "c", "d", "e", "f", "g"));
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
				currSpan = new SpannableString(word + "\n");
				currSpan.setSpan(new BackgroundColorSpan(Color.YELLOW), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			}
		}
		spans.add(currSpan);

		CharSequence seq = TextUtils.concat(spans.toArray(new Spannable[spans.size()]));
		
		Spannable span = seq == null ? new SpannableString("-") : new SpannableString(seq);
		items.add(span);
		
		ArrayAdapter<String> adapter = new WordPopupAdapter(this, R.layout.row_word_popup, objects, items, true);
		list.setAdapter(adapter);
	}

	
	
}
