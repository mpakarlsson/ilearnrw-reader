package com.example.reader.popups;

import java.util.ArrayList;
import java.util.Arrays;

import com.example.reader.R;
import com.example.reader.types.BasicListAdapter;

import android.app.Activity;
import android.os.Bundle;
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
	private Button btnAddTrickyWord;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_activity_word);
		
		Bundle b = getIntent().getExtras();
		
		tvTitle 			= (TextView) findViewById(R.id.tv_word_word);
		ivSpeak 			= (ImageView) findViewById(R.id.iv_word_speak);
		list 				= (ListView) findViewById(R.id.lv_word_info);
		btnAddTrickyWord 	= (Button) findViewById(R.id.btn_add_tricky_word);
	
		String title = b.getString("title", getResources().getString(R.string.default_text));
		tvTitle.setText(title);
	
		
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
		
		ArrayList<String> objects = new ArrayList<String>(Arrays.asList("a", "b", "c", "d", "e", "f", "g"));
		
		ArrayAdapter<String> adapter = new BasicListAdapter(this, R.layout.row_word_popup, objects, true);
		list.setAdapter(adapter);
	}

	
	
}
