package com.example.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

import com.example.reader.interfaces.TTSHighlightCallback;
import com.example.reader.interfaces.TTSReadingCallback;
import com.example.reader.popups.ModeActivity;
import com.example.reader.popups.SearchActivity;
import com.example.reader.tts.TTS;
import com.example.reader.utils.FileHelper;
import com.example.reader.utils.Helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
	private static String html, fileHtml;
	private String current;
	public static String CURR_SENT = "current";
	public static final String SENTENCE_TAG = "s";
	private ArrayList<String> texts;
	
	
	private final static int FLAG_SEARCH = 10000;
	private final static int FLAG_MODE = 10001;
	private final static int FLAG_REFRESH_WEBVIEW = 10002;
	
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
		
		
		int endingIndex =  title.lastIndexOf(".");
		String ending = title.substring(endingIndex+1);
		title = title.substring(0, endingIndex);
		CURR_SENT += "_" + title + "_" + ending;
		
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
		fileHtml = FileHelper.readFromFile(file);
		html = updateHtml(fileHtml);
		texts = new ArrayList<String>();
		
		int index =  html.indexOf("<body");
		String sentence = null;
		if(index != -1){
			String body = html.substring(index);
			sentence = Html.fromHtml(body).toString();		
		}
		
		StringTokenizer tokens = new StringTokenizer(sentence, ".");
		while(tokens.hasMoreTokens()){
			String value = tokens.nextToken();
			value = value.trim();
			
			if(!value.isEmpty() && value != null)
				texts.add(value + ".");
		}

		reader.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", "about:blank");
		
		reader_status = ReaderStatus.Disabled;
		ibtnPlay.setImageResource(R.drawable.play);
		
		
		searchbar = (RelativeLayout) findViewById(R.id.search_buttons_layout);
		ibtnSearchForward = (ImageButton) findViewById(R.id.ibtn_search_forward);
		ibtnSearchBack = (ImageButton) findViewById(R.id.ibtn_search_back);
		ibtnSearchForward.setOnClickListener(this);
		ibtnSearchBack.setOnClickListener(this);
		
		searchbar.setVisibility(RelativeLayout.GONE);
		
		
		current = PreferenceManager.getDefaultSharedPreferences(this).getString(CURR_SENT, null);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
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
		if(tts==null){
			tts = new TTS(ReaderActivity.this, cbHighlight, cbSpoken, requestCode, resultCode, data);
			setTTS();
		}
		
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
			
		case FLAG_REFRESH_WEBVIEW:
		{
			html = updateHtml(fileHtml);
			reader.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", "about:blank");
			setTTS();
		}
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
			tts.stop();
			startActivityForResult(new Intent(this, ReaderSettingsActivity.class), FLAG_REFRESH_WEBVIEW);
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
			tts.stop();
			startActivityForResult(new Intent(this, ReaderSettingsActivity.class), FLAG_REFRESH_WEBVIEW);
			break;
			
		case R.id.ibtn_prev_reader:
			current = PreferenceManager.getDefaultSharedPreferences(this).getString(CURR_SENT, null);
			if(current == null)
				current = "0";
			
			String previous = Helper.previousInt(current);
			PreferenceManager.getDefaultSharedPreferences(this).edit().putString(CURR_SENT, previous).commit();
			removeHighLight(current);
			highLight(previous);
			if(reader_status == ReaderStatus.Enabled){
				speakFromSentence(Integer.parseInt(previous));
			} 
			
			break;
			
		case R.id.ibtn_play_reader:
			current = PreferenceManager.getDefaultSharedPreferences(this).getString(CURR_SENT, null);
			if(current == null)
				current = "0";
			
			if(reader_status == ReaderStatus.Disabled){
				setPlayStatus(ReaderStatus.Enabled);
				if(reader_mode == ReaderMode.Listen) {
					int c = Integer.parseInt(current);
					speakFromSentence(c);
				}
			} else {
				PreferenceManager.getDefaultSharedPreferences(this).edit().putString(CURR_SENT, current).commit();
				setPlayStatus(ReaderStatus.Disabled);
				if(reader_mode == ReaderMode.Listen){
					tts.stop();
				}
			}
			
			break;
			
		case R.id.ibtn_next_reader:
			current = PreferenceManager.getDefaultSharedPreferences(this).getString(CURR_SENT, null);
			if(current == null)
				current = "0";
			
			int c = Integer.parseInt(current);
			String next;
			if(c+1>=texts.size())
				next = current;
			else 
				next = Helper.nextInt(current);
			
			
			
			PreferenceManager.getDefaultSharedPreferences(this).edit().putString(CURR_SENT, next).commit();
			removeHighLight(current);
			highLight(next);
			
			if(reader_status == ReaderStatus.Enabled){
				speakFromSentence(Integer.parseInt(next));
			} 
			
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
	
	public void highLight(String id){
		String highlightColor = "#" + Integer.toHexString(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getInt(getString(R.string.pref_highlight_color_title),  Color.argb(255, 255, 255, 0))).substring(2);
		reader.loadUrl("javascript:highlight('" + SENTENCE_TAG + id + "', '" + highlightColor + "');");

		/*String[] sents = html.split("[\\n\\r]");
		sentences.clear();
		for (int i = 0; i < sents.length; i++)
			if(!sents[i].trim().isEmpty())
				sentences.add(sents[i].trim());
		*/
	}
	
	public void removeHighLight(String id){
		String backgroundColor = "#" + Integer.toHexString(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getInt(getString(R.string.pref_background_color_title), Color.argb(255,255,255,255))).substring(2);
		reader.loadUrl("javascript:highlight('" + SENTENCE_TAG + id + "', '" + backgroundColor + "');");
	}
	
	
	private void setPlayStatus(ReaderStatus status){	
		if(status == ReaderStatus.Enabled)	
			ibtnPlay.setImageResource(R.drawable.pause);
		else
			ibtnPlay.setImageResource(R.drawable.play);
		reader_status = status;
	}
	
	private void setTTS(){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		int pitchRate = preferences.getInt(getString(R.string.pref_pitch_title), 9);
		int speechRate = preferences.getInt(getString(R.string.pref_speech_rate_title), 9);
		String language = preferences.getString(getString(R.string.pref_tts_language_title), "en_GB");
		
		double pitch = ((pitchRate + 1.0) / 10.0);
		tts.setPitch((float)pitch);
		double speech = ((speechRate + 1.0) / 10.0);
		tts.setSpeechRate((float)speech);
		
		Locale loc = new Locale(language.substring(0, language.indexOf("_")), language.substring(language.indexOf("_")+1));
		tts.setLanguage(loc);
	}
	
	private void speakFromSentence(int id){
		ArrayList<String> sentences = new ArrayList<String>();
		for(int i=id; i<texts.size(); i++)
			sentences.add(texts.get(i));
		
		tts.speak(sentences, id);
	}

	private String updateHtml(String html){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
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

		String setSentenceOnClick =
				"function setOnClickEvents(){" +
					"var sents = document.getElementsByTagName('" + SENTENCE_TAG + "');" +
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
							"ReaderInterface.clickSentence(body, this.id);" +
						"};" +					
					"}" +
				"}";
		
		String setCSSLink = "<link rel='stylesheet' href='css/default.css' type='text/css'>";
		
		String backgroundColor =  Integer.toHexString(preferences.getInt(getString(R.string.pref_background_color_title), Color.argb(255,255,255,255)));
		String textColor = Integer.toHexString(preferences.getInt(getString(R.string.pref_text_color_title), Color.argb(255,0,0,0)));
		
		backgroundColor = "#" + backgroundColor.substring(2);
		textColor = "#" + textColor.substring(2);
		
		String lineHeight = preferences.getString(getString(R.string.pref_line_height_title), "0");
		String fontSize = preferences.getString(getString(R.string.pref_font_size_title), "20");
		String letterSpacing = preferences.getString(getString(R.string.pref_letter_spacing_title), "0");
		String margin = preferences.getString(getString(R.string.pref_margin_title), "0");
		String fontFamily = preferences.getString(getString(R.string.pref_font_face_title), "default");
		
		lineHeight = lineHeight.equals("0") ? "line-height: normal;" : "line-height: " + lineHeight + "px;";
		fontSize = fontSize.equals("0") ? "font-size: 20px;" : "font-size: " + fontSize + "px;";
		fontFamily = fontFamily.indexOf(".") == -1 ? fontFamily : fontFamily.substring(0, fontFamily.lastIndexOf("."));
		
		String cssBody = "" +
				"<style type='text/css'>" +
				"body " +
				"{ " +
					"font-family:" + fontFamily +"; "+
					fontSize +
					"background-color:" + backgroundColor + "!important; " +
					"color:" + textColor +"; " +
					lineHeight +
					"letter-spacing: " + letterSpacing + "px;" +
					"margin: " + margin + "px; " +
				"}" +
				"</style>" +
				"";
		
		return firstPart +
				startScripts + 
				highlightSentence +
				retrieveBodyContent +
				setSentenceOnClick +
				stopScripts +
				setCSSLink +
				cssBody +
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
			String curr = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(CURR_SENT, null);
			if(curr != null)
				highLight(curr);
			
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
		public void clickSentence(String html, String id){			
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
			
			final int sentId = Integer.parseInt(id.substring(pos));
			
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					String curr = prefs.getString(CURR_SENT, null);
					int c = -1;
					
					
					if(curr!=null){
						c = Integer.parseInt(curr);
						removeHighLight(Integer.toString(c));
					}
					
					if(c!=sentId){
						String sId = Integer.toString(sentId);
						
						highLight(sId);
						prefs.edit().putString(CURR_SENT, sId).commit();
						
						if(reader_status == ReaderStatus.Enabled)
							speakFromSentence(sentId);
					} else 
						prefs.edit().putString(CURR_SENT, null).commit();
				}
			});
		}
	}


	@Override
	public void OnHighlight(final String id) {
		
		this.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				String highlightColor = "#" + Integer.toHexString(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getInt(getString(R.string.pref_highlight_color_title),  Color.argb(255, 255, 255, 0))).substring(2);
				reader.loadUrl("javascript:highlight('" + SENTENCE_TAG + id + "', '" + highlightColor + "');");
			}
		});
	}

	@Override
	public void OnRemoveHighlight(final String id) {
		this.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				String backgroundColor = "#" + Integer.toHexString(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getInt(getString(R.string.pref_background_color_title), Color.argb(255,255,255,255))).substring(2);
				reader.loadUrl("javascript:highlight('" + SENTENCE_TAG + id + "', '" + backgroundColor + "');");
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
