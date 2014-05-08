package com.example.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.example.reader.interfaces.TTSHighlightCallback;
import com.example.reader.interfaces.TTSReadingCallback;
import com.example.reader.popups.ModeActivity;
import com.example.reader.popups.SearchActivity;
import com.example.reader.tts.TTS;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ReaderActivity extends Activity implements OnClickListener, OnLongClickListener, TTSHighlightCallback, TTSReadingCallback {

	public static TextView tvTitle;
	public static WebView reader;
	public static ImageButton ibtnLib, ibtnSearch, ibtnMode, ibtnPrev, ibtnPlay, ibtnNext, ibtnSettings, ibtnSearchForward, ibtnSearchBack;
	public static RelativeLayout top, bottom, searchbar;
	public static ReaderMode reader_mode;
	public static ReaderStatus reader_status;
	private TTS tts;
	private TTSHighlightCallback cbHighlight;
	private TTSReadingCallback cbSpoken;
	private String sentenceColor;
	private static String html;
	
	private final static int FLAG_SEARCH = 10000;
	private final static int FLAG_MODE = 10001;
	
	public static enum ReaderMode {
		Listen("Listen", 0),
		Guidance("Guidance", 1),
		Chunking("Chunking", 2);
		
		private String name;
		private int value;
		private ReaderMode(String name, int value){
			this.name = name;
			this.value = value;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public int getValue(){
			return value;
		}
	}
	
	public static enum ReaderStatus {
		Enabled,
		Disabled
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader);
		
		Bundle libBundle = getIntent().getBundleExtra("LibraryBundle");
		File file = (File) libBundle.get("file");
		String title = libBundle.getString("title");
		
		tvTitle = (TextView) findViewById(R.id.tv_book_title_reader);
		tvTitle.setText(title);
		
		ibtnLib = (ImageButton) findViewById(R.id.ibtn_lib_reader);
		ibtnSearch = (ImageButton) findViewById(R.id.ibtn_search_reader);
		ibtnMode = (ImageButton) findViewById(R.id.ibtn_mode_reader);
		ibtnPrev = (ImageButton) findViewById(R.id.ibtn_prev_reader);
		ibtnPlay = (ImageButton) findViewById(R.id.ibtn_play_reader);
		ibtnNext = (ImageButton) findViewById(R.id.ibtn_next_reader);
		ibtnSettings = (ImageButton) findViewById(R.id.ibtn_settings_reader);
		
		ibtnLib.setOnClickListener(this);
		ibtnSearch.setOnClickListener(this);
		ibtnMode.setOnClickListener(this);
		ibtnPrev.setOnClickListener(this);
		ibtnPlay.setOnClickListener(this);
		ibtnNext.setOnClickListener(this);
		ibtnSettings.setOnClickListener(this);
		
		top = (RelativeLayout) findViewById(R.id.reader_top);
		bottom = (RelativeLayout) findViewById(R.id.reader_bottom);
	
		Intent checkTTSIntent = new Intent(); 
		checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, 0);
		
		cbHighlight = this;
		cbSpoken = this;
		
		reader = (WebView) findViewById(R.id.webview_reader);
		reader.setOnLongClickListener(this);
		reader.setLongClickable(false);
		reader.getSettings().setJavaScriptEnabled(true);
		reader.addJavascriptInterface(new ReaderWebInterface(this), "ReaderInterface");
		reader.getSettings().setDefaultFontSize(22);
		
		reader.setWebViewClient(new MyWebViewClient());
		html = getHtml(file);
		html = updateHtml();
		
		reader.loadDataWithBaseURL(null, html, "text/html", "UTF-8", "about:blank");
		
		reader_status = ReaderStatus.Disabled;
		ibtnPlay.setImageResource(R.drawable.play);
		
		
		searchbar = (RelativeLayout) findViewById(R.id.search_buttons_layout);
		ibtnSearchForward = (ImageButton) findViewById(R.id.ibtn_search_forward);
		ibtnSearchBack = (ImageButton) findViewById(R.id.ibtn_search_back);
		ibtnSearchForward.setOnClickListener(this);
		ibtnSearchBack.setOnClickListener(this);
		
		searchbar.setVisibility(RelativeLayout.GONE);
		
		// TODO: Create settings menu, 
		// * tts options, "Pitch", "SpeechRate", "Language"(dropdown with available languages)
		// * reader options, "FontSize", "Font" 
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		int mode = prefs.getInt("readerMode", -1);
		if(mode==-1){
			prefs.edit().putInt("readerMode", ReaderMode.Listen.getValue()).commit();
			Toast.makeText(this, "No reader mode set. Listen mode is selected", Toast.LENGTH_LONG).show();
		}
		
		switch(mode){
		case 0:
			reader_mode = ReaderMode.Listen;
			break;
		case 1:
			reader_mode = ReaderMode.Guidance;
			break;
		case 2: 
			reader_mode = ReaderMode.Chunking;
			break;
		default:
			reader_mode = ReaderMode.Listen;
			break;
		}
	}

	@Override
	protected void onDestroy() {
		tts.destroy();
		super.onDestroy();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(tts==null)
			tts = new TTS(ReaderActivity.this, cbHighlight, cbSpoken, requestCode, resultCode, data);
		
		switch (requestCode) {
		case FLAG_SEARCH:
		{
			if(resultCode == RESULT_OK){
				String searchString = data.getStringExtra("searchString");
				searchbar.setVisibility(RelativeLayout.VISIBLE);
				reader.findAllAsync(searchString);
			}
		}
			break;
			
		case FLAG_MODE:
		{
			if(resultCode == RESULT_OK){
				int mode = data.getExtras().getInt("chosenMode");
				Log.d("onActivityResult - FLAG_MODE", "Chosen mode: " + Integer.toString(mode));
				
				switch(mode){
				case 0:
					reader_mode = ReaderMode.Listen;
					break;
				case 1:
					reader_mode = ReaderMode.Guidance;
					break;
				case 2: 
					reader_mode = ReaderMode.Chunking;
					break;
				default:
					reader_mode = ReaderMode.Listen;
					break;
				}
			}
		}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}



	@Override
	public void onBackPressed() {
		Intent i = new Intent(this, LibraryActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(i);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.reader, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibtn_lib_reader:
			Intent lib_intent = new Intent(this, LibraryActivity.class);
			lib_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(lib_intent);
			finish();
			break;
			
		case R.id.ibtn_search_reader:
			Intent search_intent = new Intent(this, SearchActivity.class);
			search_intent.putExtra("posX", ibtnSearch.getX());
			search_intent.putExtra("posY", (ibtnSearch.getY()+ibtnSearch.getHeight()));
			search_intent.putExtra("imageHeight", ibtnSearch.getHeight());
			startActivityForResult(search_intent, FLAG_SEARCH);
			break;
			
		case R.id.ibtn_mode_reader:
			Intent mode_intent = new Intent(this, ModeActivity.class);
			mode_intent.putExtra("posX", ibtnMode.getX());
			mode_intent.putExtra("posY", (bottom.getY()-ibtnMode.getHeight()));
			mode_intent.putExtra("imageHeight", ibtnMode.getHeight());
			mode_intent.putExtra("readerMode", reader_mode);
			startActivityForResult(mode_intent, FLAG_MODE);
			break;
			
		case R.id.ibtn_settings_reader:
			startActivity(new Intent(this, ReaderSettingsActivity.class));
			break;
			
		case R.id.ibtn_prev_reader:
			break;
			
		case R.id.ibtn_play_reader:			
			if(reader_status == ReaderStatus.Disabled){
				setPlayStatus(ReaderStatus.Enabled);
				if(reader_mode == ReaderMode.Listen) {
					reader.loadUrl("javascript:getBodyContent('');");
				}
			} else {
				setPlayStatus(ReaderStatus.Disabled);
				if(reader_mode == ReaderMode.Listen){
					tts.stop();
				}
			}
			
			break;
			
		case R.id.ibtn_next_reader:
			break;
			
		case R.id.ibtn_search_forward:
			reader.findNext(true);
			break;
			
		case R.id.ibtn_search_back:
			reader.findNext(false);
			break;

		default:
			break;
		}
	}
	
	@Override
	public boolean onLongClick(View v) {
		return false;
	};
	
	private void setPlayStatus(ReaderStatus status){	
		if(status == ReaderStatus.Enabled)	
			ibtnPlay.setImageResource(R.drawable.pause);
		else
			ibtnPlay.setImageResource(R.drawable.play);
		reader_status = status;
	}
	
	private String getHtml(File f){
		StringBuilder sBuilder = null;
		FileInputStream fStream = null;
		BufferedReader buffReader = null;
		
		try {
			fStream = new FileInputStream(f);
			buffReader = new BufferedReader(new InputStreamReader(fStream, "UTF-8"));
			sBuilder = new StringBuilder();
			
			String line = buffReader.readLine();
			
			while(line != null){
				sBuilder.append(line);
				line = buffReader.readLine();
				
				if(line != null)
					sBuilder.append("\n");
			}
			
			buffReader.close();
			fStream.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sBuilder.toString();
	}

	private String updateHtml(){
		boolean hasHead = true;
		int splitPos = html.indexOf("<head");
		
		if(splitPos == -1){
			splitPos = html.indexOf("<html");
			hasHead = false;
		}
		
		splitPos = html.indexOf(">", splitPos) + 1;
		
		String firstPart = "", secondPart = "";
		firstPart = html.substring(0, splitPos);
		secondPart = html.substring(splitPos);
		
		if(!hasHead){
			firstPart += "<head>";
			secondPart = "</head>" + secondPart;
		}
		
		String startScripts = "<script type=\"text/javascript\">";
		String stopScripts = "</script>";
		
		String retrieveBodyContent =
				"function getBodyContent(str) {" +
				"	var bodyHTML = document.body.innerHTML;" +
				"	ReaderInterface.splitSentencesSpeak(bodyHTML, 0, 'button');" +
				"}";
		
		
		
		String highlightSentence = 
				"function highlight(id, color){" +
					"document.getElementById(id).style.backgroundColor=color;" +
				"return true;" + 
				"}";
		
		String getSentenceStyle = 
				"function getSentenceStyle(id){" +
				"var s = document.getElementById(id).style.backgroundColor;" +
				"ReaderInterface.getSentenceStyle(s);" +
				"}";

		String setSentenceOnClick =
				"function setOnClickEvents(){" +
					"var sents = document.getElementsByTagName('sent');" +
					"var timer;" +
					"var longPressTime = 2000;" +
					"for(var i = 0; i < sents.length; i++) {" +
						"sents[i].onclick = function() {" +
							"var body = document.body.innerHTML;" +
							"var index = body.indexOf(this.id);" +
							"var part = body.substring(0, index);" +
							"var lastIndex = part.lastIndexOf('<');" +
							"part = part.substring(lastIndex);" +
							"body = body.substring(body.indexOf(this.id));" +
							"body = part + body;" +
							"ReaderInterface.splitSentencesSpeak(body, this.id, 'click');" +
						"};" +					
					"}" +
				"}";
		
		
		return firstPart +
				startScripts + 
				highlightSentence +
				getSentenceStyle +
				retrieveBodyContent +
				setSentenceOnClick +
				stopScripts +
				secondPart;
	}
	
	
	private class MyWebViewClient extends WebViewClient{

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			Log.e("WebViewClient", "shouldOverrideUrlLoading");
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			reader.loadUrl("javascript:setOnClickEvents();");
			
			/*reader.loadUrl("javascript:highlight('s0', 'LightGreen');");
			reader.loadUrl("javascript:highlight('s1', '#00FF66');");
			reader.loadUrl("javascript:highlight('sent0', '#FF3300');");
			reader.loadUrl("javascript:highlight('sent1', '#0066FF');");
			reader.loadUrl("javascript:highlight('sent2', '#996600');");
			reader.loadUrl("javascript:highlight('sent5', '#FF6633');");
			reader.loadUrl("javascript:highlight('sent6', '#9933FF');");
			reader.loadUrl("javascript:highlight('sent6', '#9933FF');");
			reader.loadUrl("javascript:ReaderInterface.showToast('onPageFinished');");*/
		}	
	};
	
	
	private class ReaderWebInterface {
		Context context;
		
		public ReaderWebInterface(Context c){
			context = c;
		}
		
		@JavascriptInterface
		public void showHTML(String html){
			new AlertDialog.Builder(context)
				.setTitle("Show HTML")
				.setMessage(html)
				.setPositiveButton(android.R.string.ok, null)
			.setCancelable(false)
			.create()
			.show();
		}
		
		@JavascriptInterface
		public void showToast(String text){
			Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
		}
		
		@JavascriptInterface
		public void logMessage(String tag, String text){
			Log.d(tag, text);
		}
		
		@JavascriptInterface
		public void splitSentencesSpeak(String html, String id, String clickType){						
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					setPlayStatus(ReaderStatus.Enabled);
				}
			});
			
			String tempString = Html.fromHtml(html).toString();

			int pos = -1;
			for(int i=id.length() - 1; i>=0; i--){
				Character c = id.charAt(i);

				if(Character.isDigit(c)){
					pos = i;
				} else if(Character.isLetter(c)){
					break;
				}
			}
			
			if(pos==-1){
				showToast("Id does not contain a number");
				return;
			}
			
			int sentId = Integer.parseInt(id.substring(pos));
			
			tempString = tempString.replaceAll("[:](?=[A-Z])", ":\n");
			tempString = tempString.replaceAll("[.](?= )", ".\n");
			String[] sentences = tempString.split("[\\n\\r]");
			
			ArrayList<String> sentencesRemoved = new ArrayList<String>();
			
			for (int i = 0; i < sentences.length; i++) {
				if(!sentences[i].trim().isEmpty()){
					sentencesRemoved.add(sentences[i].trim());
				}
			}
			
			tts.speak(sentencesRemoved, sentId, clickType);
		}		
		
		@JavascriptInterface
		public void getSentenceStyle(String style){
			if(style.isEmpty()){
				sentenceColor = ""; 
				return;
			}
			sentenceColor = style.substring(style.indexOf("(")+1);
			sentenceColor = sentenceColor.substring(0, sentenceColor.indexOf(")"));
		}
		
	}


	@Override
	public void OnHighlight(final String id) {
		
		this.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				reader.loadUrl("javascript:getSentenceStyle('sent"+id+"');");
				reader.loadUrl("javascript:highlight('sent"+id+"', '#FFFF00');");
			}
		});
	}

	@Override
	public void OnRemoveHighlight(final String id) {
		this.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				String[] colors = new String[3];
				if(sentenceColor.isEmpty()){
					reader.loadUrl("javascript:highlight('sent"+id+"', '#FFFFFF');");
					return;
				}
				else
					colors = sentenceColor.split(",");
				
				String hex = "#";
				for(int i = 0; i < colors.length; i++){
					String color = colors[i].trim();
					
					if(color.equals("0"))
						hex += "00";
					else
						hex += Integer.toHexString(Integer.parseInt(colors[i].trim()));
				}
				
				reader.loadUrl("javascript:highlight('sent"+id+"', '"+hex+"');");
			}
		});
	}

	@Override
	public void OnFinishedReading() {
		this.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				setPlayStatus(ReaderStatus.Disabled);
			}
		});
		
		
	}

	@Override
	public void OnStartedReading() {
		this.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				setPlayStatus(ReaderStatus.Enabled);
			}
		});
		
	}
}
