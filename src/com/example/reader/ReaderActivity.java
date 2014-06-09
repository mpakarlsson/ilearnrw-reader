package com.example.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import com.example.reader.interfaces.TTSHighlightCallback;
import com.example.reader.interfaces.TTSReadingCallback;
import com.example.reader.popups.ModeActivity;
import com.example.reader.popups.SearchActivity;
import com.example.reader.types.Pair;
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
import android.os.Handler;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ReaderActivity extends Activity implements OnClickListener, OnLongClickListener, OnSeekBarChangeListener, TTSHighlightCallback, TTSReadingCallback {

	private final String TAG = getClass().getName();
	
	private TextView tvTitle, tvHighLightSpeed;
	private WebView reader;
	private ImageButton ibtnLib, ibtnSearch, ibtnMode, ibtnPrev, ibtnPlay, ibtnNext, ibtnSettings, ibtnSearchForward, ibtnSearchBack;
	private RelativeLayout bottom, searchbar, rlHighlightSpeed;
	private SeekBar sbHighLightSpeed;
	
	private ReaderMode reader_mode;
	private ReaderStatus reader_status;
	
	private TTS tts;
	private TTSHighlightCallback cbHighlight;
	private TTSReadingCallback cbSpoken;

	private HighlightRunnable highlightRunnable;
	private Handler highlightHandler;
	
	private ArrayList<String> texts;
	public HashMap<String, Pair<String>> highlightParts;
	
	private SharedPreferences sp;
	private SharedPreferences.Editor spEditor;
		
	public String CURR_SENT = "current";
	public static final String SENTENCE_TAG = "sen";
	private static final String DEFAULT_SENTENCE = "s0";
	
	private String current;
	private static String html, fileHtml;
	
	private double highlightSpeed;

	private boolean isHighlighting;
	
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
		
		Bundle libBundle 	= getIntent().getBundleExtra("LibraryBundle");
		File file 			= (File) libBundle.get("file");
		String title		= libBundle.getString("title");
		
		sp 			= PreferenceManager.getDefaultSharedPreferences(this);
		spEditor 	= sp.edit();
		Pair<String> bookTitle = Helper.splitFileName(title);
		CURR_SENT 		= CURR_SENT + "_" + bookTitle.first() + "_" + bookTitle.second().substring(1);
		
		tvTitle = (TextView) findViewById(R.id.tv_book_title_reader);
		tvTitle.setText(title);
		
		tvHighLightSpeed = (TextView) findViewById(R.id.tv_highlight_speed_value);
		
		sbHighLightSpeed = (SeekBar) findViewById(R.id.seekbar_highLight_speed);
		sbHighLightSpeed.setOnSeekBarChangeListener(this);

		highlightSpeed = Double.longBitsToDouble(sp.getLong(getString(R.string.pref_highlighter_speed), Double.doubleToLongBits(5.5)));
		int hlSpeed = (int) (highlightSpeed * 10);
		sbHighLightSpeed.setProgress(hlSpeed);
		
		tvHighLightSpeed.setText(String.format("%.1f", highlightSpeed) + "\n/\n" + String.format("%.1f", ((sbHighLightSpeed.getMax()*0.1) + 0.5)));
		
		ibtnLib 		= (ImageButton) findViewById(R.id.ibtn_lib_reader);
		ibtnSearch 		= (ImageButton) findViewById(R.id.ibtn_search_reader);
		ibtnMode 		= (ImageButton) findViewById(R.id.ibtn_mode_reader);
		ibtnPrev 		= (ImageButton) findViewById(R.id.ibtn_prev_reader);
		ibtnPlay 		= (ImageButton) findViewById(R.id.ibtn_play_reader);
		ibtnNext 		= (ImageButton) findViewById(R.id.ibtn_next_reader);
		ibtnSettings 	= (ImageButton) findViewById(R.id.ibtn_settings_reader);
		
		ibtnLib.setOnClickListener(this);
		ibtnSearch.setOnClickListener(this);
		ibtnMode.setOnClickListener(this);
		ibtnPrev.setOnClickListener(this);
		ibtnPlay.setOnClickListener(this);
		ibtnNext.setOnClickListener(this);
		ibtnSettings.setOnClickListener(this);
		
		bottom 				= (RelativeLayout) findViewById(R.id.reader_bottom);
		rlHighlightSpeed 	= (RelativeLayout) findViewById(R.id.reader_body_highlight_speed);
		
		highlightRunnable 	= new HighlightRunnable();
		highlightHandler 	= new Handler();
		
		
		Intent checkTTSIntent = new Intent(); 
		checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, 0);
		
		cbHighlight 	= this;
		cbSpoken 		= this;
		
		reader = (WebView) findViewById(R.id.webview_reader);
		reader.setOnLongClickListener(this);
		reader.setLongClickable(false);
		reader.getSettings().setJavaScriptEnabled(true);
		reader.addJavascriptInterface(new ReaderWebInterface(this), "ReaderInterface");
		reader.getSettings().setDefaultFontSize(22);
		
		reader.setWebViewClient(new MyWebViewClient());
		fileHtml 	= FileHelper.readFromFile(file);
		html 		= updateHtml(fileHtml);
		texts 		= new ArrayList<String>();
		
		int index = html.indexOf("<body");
		if(index != -1){
			String body = html.substring(index);
			String[] sentences = body.split("</" + SENTENCE_TAG +">");
			for(int i=0; i<sentences.length; i++){
				String s = sentences[i] + "</" + SENTENCE_TAG + ">";
				int ind = s.indexOf("<" + SENTENCE_TAG);
				s = ind == -1 ? "" :s.substring(ind);
				s = Html.fromHtml(s).toString();
				if(!s.trim().isEmpty())
					texts.add(s);
			}		
		}

		reader.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", "about:blank");

		reader_status = ReaderStatus.Disabled;
		ibtnPlay.setImageResource(R.drawable.play);
		
		searchbar 			= (RelativeLayout) findViewById(R.id.search_buttons_layout);
		ibtnSearchForward 	= (ImageButton) findViewById(R.id.ibtn_search_forward);
		ibtnSearchBack 		= (ImageButton) findViewById(R.id.ibtn_search_back);
		ibtnSearchForward.setOnClickListener(this);
		ibtnSearchBack.setOnClickListener(this);
		
		searchbar.setVisibility(RelativeLayout.GONE);
		
		current = sp.getString(CURR_SENT, DEFAULT_SENTENCE);
		isHighlighting =  sp.getBoolean("highlighting", true);

		highlightParts = new HashMap<String, Pair<String>>();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		int mode = sp.getInt("readerMode", -1);
		if(mode==-1){
			spEditor.putInt("readerMode", ReaderMode.Listen.getValue()).commit();
			Toast.makeText(this, "No reader mode set. Listen mode is selected", Toast.LENGTH_LONG).show();
		}
		
		switch(mode){
		case 0:
			reader_mode = ReaderMode.Listen;
			rlHighlightSpeed.setVisibility(View.GONE);
			break;
		case 1:
			reader_mode = ReaderMode.Guidance;
			rlHighlightSpeed.setVisibility(View.VISIBLE);
			break;
		case 2: 
			reader_mode = ReaderMode.Chunking;
			rlHighlightSpeed.setVisibility(View.GONE);
			break;
		default:
			reader_mode = ReaderMode.Listen;
			rlHighlightSpeed.setVisibility(View.GONE);
			break;
		}
	}

	@Override
	protected void onPause() {
		highlightHandler.removeCallbacks(highlightRunnable);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		tts.destroy();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(tts==null){
			tts = new TTS(ReaderActivity.this, CURR_SENT, cbHighlight, cbSpoken, requestCode, resultCode, data);
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
				Log.d(TAG + " onActivityResult - FLAG_MODE", "Chosen mode: " + Integer.toString(mode));
				
				switch(mode){
				case 0:
					reader_mode = ReaderMode.Listen;
					rlHighlightSpeed.setVisibility(View.GONE);
					break;
				case 1:
					reader_mode = ReaderMode.Guidance;
					rlHighlightSpeed.setVisibility(View.VISIBLE);
					break;
				case 2: 
					reader_mode = ReaderMode.Chunking;
					rlHighlightSpeed.setVisibility(View.GONE);
					break;
				default:
					reader_mode = ReaderMode.Listen;
					rlHighlightSpeed.setVisibility(View.GONE);
					break;
				}
				
				spEditor.putInt("readerMode", mode).commit();
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
			Intent i = new Intent(this, SettingsActivity.class);
			i.putExtra("setting", "reader");
			startActivityForResult(i, FLAG_REFRESH_WEBVIEW);
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
			Intent i = new Intent(this, SettingsActivity.class);
			i.putExtra("setting", "reader");
			startActivityForResult(i, FLAG_REFRESH_WEBVIEW);
			break;
			
		case R.id.ibtn_prev_reader:
			current = sp.getString(CURR_SENT, DEFAULT_SENTENCE);
			
			String identifier = Helper.findIdentifier(current);
			int prev =  Helper.findPosition(current);
			prev = prev > 0 ? --prev : 0;
			
			String previous = identifier + Integer.toString(prev);
			spEditor.putString(CURR_SENT, previous).commit();
			
			removeHighlight(current);
			highlight(previous);
			
			if(reader_status == ReaderStatus.Enabled){
				if(reader_mode == ReaderMode.Listen)
					speakFromSentence(previous);
				else if(reader_mode == ReaderMode.Guidance){
					resetGuidance();
				}
			} 
			
			break;
			
		case R.id.ibtn_play_reader:
			current = sp.getString(CURR_SENT, DEFAULT_SENTENCE);
			
			if(reader_status == ReaderStatus.Disabled){
				setPlayStatus(ReaderStatus.Enabled);
				if(reader_mode == ReaderMode.Listen) {
					speakFromSentence(current);
				} else if(reader_mode == ReaderMode.Guidance){
					resetGuidance();
				}
			} else {
				spEditor.putString(CURR_SENT, current).commit();
				setPlayStatus(ReaderStatus.Disabled);
				if(reader_mode == ReaderMode.Listen){
					tts.stop();
					tts.rehighlight();
				} else if(reader_mode == ReaderMode.Guidance){
					highlightHandler.removeCallbacks(highlightRunnable);
				}
			}
			
			break;
			
		case R.id.ibtn_next_reader:
			current = sp.getString(CURR_SENT, DEFAULT_SENTENCE);

			String identifier2 = Helper.findIdentifier(current);
			int n = Helper.findPosition(current);
			String next;
			
			next = n+1 >= texts.size() ? current : identifier2 + Integer.toString(++n);			
			
			spEditor.putString(CURR_SENT, next).commit();
			removeHighlight(current);
			highlight(next);
			
			if(reader_status == ReaderStatus.Enabled){
				if(reader_mode == ReaderMode.Listen)
					speakFromSentence(next);
				else if(reader_mode == ReaderMode.Guidance){
					resetGuidance();
				}
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
		removeSearches();
		return false;
	};
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		switch(seekBar.getId()){
		case R.id.seekbar_highLight_speed:
			highlightSpeed = (seekBar.getProgress() * 0.1) + 0.5; // Slider values goes from 0.5 to 10.5
			double max = ((seekBar.getMax()*0.1) + 0.5);
			tvHighLightSpeed.setText(String.format("%.1f", highlightSpeed) + "\n/\n" + String.format("%.1f", max));
			break;
		default:
			break;
		
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		switch (seekBar.getId()) {
		case R.id.seekbar_highLight_speed:
			highlightHandler.removeCallbacks(highlightRunnable);
			break;

		default:
			break;
		}
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		
		switch (seekBar.getId()) {
		case R.id.seekbar_highLight_speed:
			
			highlightSpeed =  (seekBar.getProgress() * 0.1);
			spEditor.putLong(getString(R.string.pref_highlighter_speed), Double.doubleToRawLongBits(highlightSpeed)).commit();
			
			highlightSpeed += 0.5;
			
			if(reader_status == ReaderStatus.Enabled) {
				long millis = (long) (highlightSpeed * 1000);
				highlightHandler.postDelayed(highlightRunnable, millis);
			}
			break;

		default:
			break;
		}
		
	}
	
	public void removeSearches(){
		reader.clearMatches();
		searchbar.setVisibility(View.GONE);
	}
	
	
	public void highlight(String id){
		if(id.isEmpty())
			return;
		
		reader.loadUrl("javascript:scrollToElement('" + id + "');");
		
		String highlightColor = "#" + Integer.toHexString(sp.getInt(getString(R.string.pref_highlight_color_title),  Color.argb(255, 255, 255, 0))).substring(2);
		reader.loadUrl("javascript:highlight('" + id + "', '" + highlightColor + "');");
	}
	
	public void highlight(String id, String hexColor){
		if(id.isEmpty())
			return;
		
		reader.loadUrl("javascript:highlight('" + id + "', '" + hexColor + "');");
	}
	
	public void highlightPart(final String id, final int start, final int end, final String hexColor){
		if(id.isEmpty())
			return;
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				reader.loadUrl("javascript:highlightPart('" + id + "', '" + start + "', '" + end + "', '" + hexColor + "');");
			}
		});
		
	}
	
	public void removeHighlight(String id){
		if(id.isEmpty())
			return;
		
		String backgroundColor = "#" + Integer.toHexString(sp.getInt(getString(R.string.pref_background_color_title), Color.argb(255,255,255,255))).substring(2);
		reader.loadUrl("javascript:highlight('" + id + "', '" + backgroundColor + "');");
	}
	
	public void removeHighlightPart(final String id, final Pair<String> span){
		if(id.isEmpty())
			return;
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				reader.loadUrl("javascript:unhighlight('" + id + "', '" + span.first() + "', '" + span.second() + "');");
			}
		});
	}
	
	
	private void resetGuidance(){
		highlightHandler.removeCallbacks(highlightRunnable);
		long millis = (long) (highlightSpeed * 1000);
		highlightHandler.postDelayed(highlightRunnable, millis);
	}
	
	
	private void setPlayStatus(ReaderStatus status){	
		if(status == ReaderStatus.Enabled)	
			ibtnPlay.setImageResource(R.drawable.pause);
		else
			ibtnPlay.setImageResource(R.drawable.play);
		reader_status = status;
	}
	
	private void setTTS(){		
		int pitchRate 	= sp.getInt(getString(R.string.pref_pitch_title), 9);
		int speechRate 	= sp.getInt(getString(R.string.pref_speech_rate_title), 9);
		String language = sp.getString(getString(R.string.pref_tts_language_title), "en_GB");
		
		double pitch = ((pitchRate + 1.0) / 10.0);
		tts.setPitch((float)pitch);
		double speech = ((speechRate + 1.0) / 10.0);
		tts.setSpeechRate((float)speech);
		
		Locale loc = new Locale(language.substring(0, language.indexOf("_")), language.substring(language.indexOf("_")+1));
		tts.setLanguage(loc);
	}
	
	private void speakFromSentence(String id){
		if(id == null || id.isEmpty())
			id = DEFAULT_SENTENCE;
		
		ArrayList<String> sentences = new ArrayList<String>();
		int pos = Helper.findPosition(id);
		
		for(int i=pos; i<texts.size(); i++)
			sentences.add(texts.get(i));
		
		tts.speak(sentences, pos, id);
	}

	private String updateHtml(String html){
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
		
		if(hasHead){
			int hEnd = secondPart.indexOf("</head>");
			secondPart = secondPart.substring(hEnd);
		}
		if(!hasHead){
			firstPart += "<head>";
			secondPart = "</head>" + secondPart;
		}
		
		String startScripts = "<script type=\"text/javascript\">";
		String stopScripts = "</script>";
		
		String showToast = 
				"function showToast(toast){" +
					"ReaderInterface.showToast(toast);" +
				"}";
		
		String retrieveBodyContent =
				"function getBodyContent(str) {" +
				"	var bodyHTML = document.body.innerHTML;" +
				"	ReaderInterface.splitSentencesSpeak(bodyHTML, 0, 'button');" +
				"}";
		

		String scrollToElement =
				"function scrollToElement(id){" +
					"var elem = document.getElementById(id);" +
					"var x = 0;" +
					"var y = 0;" +
					"" +
					"while(elem != null) {" +
						"x += elem.offsetLeft;" +
						"y += elem.offsetTop;" +
						"elem = elem.offsetParent" +
					"}" +
					"window.scrollTo(x,y);" +
				"}";
		
		String highlightSentence = 
				"function highlight(id, color){" +
					"var element = document.getElementById(id);" +
					"element.style.backgroundColor=color;" +
					"" +
					"return true;" + 
				"}";
		
		String highlightPart =
				"function highlightPart(id, start, end, color){" +
					"var node =  document.getElementById(id);" +
					"var range = document.createRange();" +
					"range.setStart(node.firstChild, start);" +
					"range.setEnd(node.firstChild, end);" +
					"" +
					"var span = document.createElement('span');" +
					"span.className = id;" +
					"span.style.backgroundColor = color;" +
					"" +
					"range.surroundContents(span);" +
					"range.detach();" +
					"ReaderInterface.saveHighlightInformation(id, start, end);" +
					"" +
					"return true;" +
				"}";
		
		String unhighlightPart = 
				"function unhighlight(id, start, end){" +
					"var spans = document.getElementsByTagName(\"span\");" +
					"for(var i=0; i<spans.length; i++){" +
						"if(spans[i].className == id){" +
							"var container = spans[i].parentNode;" +
							"var node = spans[i].firstChild;" +
							"container.insertBefore(node, spans[i]);" +
							"container.removeChild(spans[i]);" +
							"ReaderInterface.removeHighlightInformation(id);" +
						"}" +
					"}" + 
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
		
		String showMoreInformation = 
				"function showMoreInformation() {" +
						"ReaderInterface.showMoreInformation(document.getSelection().toString());" +
				"}";
		
		String setCSSLink = "<link rel='stylesheet' href='css/default.css' type='text/css'>";
		
		String backgroundColor 	= Integer.toHexString(sp.getInt(getString(R.string.pref_background_color_title), Color.argb(255,255,255,255)));
		String textColor 		= Integer.toHexString(sp.getInt(getString(R.string.pref_text_color_title), Color.argb(255,0,0,0)));
		
		backgroundColor 		= "#" + backgroundColor.substring(2);
		textColor 				= "#" + textColor.substring(2);
		
		String lineHeight 		= sp.getString(getString(R.string.pref_line_height_title), "100");
		int fSize				= sp.getInt(getString(R.string.pref_font_size_title), 20);
		String fontSize;
		String letterSpacing 	= sp.getString(getString(R.string.pref_letter_spacing_title), "0");
		String margin 			= sp.getString(getString(R.string.pref_margin_title), "0");
		String fontFamily 		= sp.getString(getString(R.string.pref_font_face_title), "default");
		
		lineHeight 				= lineHeight.equals("0") ? "line-height: normal;" : "line-height: " + lineHeight + "%;";
		fontSize 				= fSize==0 ? "font-size: 20pt;" : "font-size: " + fSize + "pt;";
		fontFamily 				= fontFamily.indexOf(".") == -1 ? fontFamily : fontFamily.substring(0, fontFamily.lastIndexOf("."));
		
		String cssBody = "" +
				"<style type='text/css'>" +
				"body " +
				"{ " +
					"font-family:" + fontFamily +", sans-serif; "+
					fontSize +
					"background-color:" + backgroundColor + "!important; " +
					"color:" + textColor +"; " +
					lineHeight +
					"letter-spacing: " + letterSpacing + "pt;" +
					"margin: " + margin + "%; " +
				"}" +
				"</style>" +
				"";
		
		return firstPart +
				startScripts + 
				highlightSentence +
				highlightPart +
				unhighlightPart +
				retrieveBodyContent +
				scrollToElement +
				setSentenceOnClick +
				showToast + 
				showMoreInformation + 
				stopScripts +
				setCSSLink +
				cssBody +
				secondPart;
	}
	
	
	private class MyWebViewClient extends WebViewClient{

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			Log.e(TAG + " WebViewClient", "shouldOverrideUrlLoading");
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			reader.loadUrl("javascript:setOnClickEvents();");
			String curr = sp.getString(CURR_SENT, DEFAULT_SENTENCE);
			if(isHighlighting)
				highlight(curr);
			
			highlight("w0", "#FFFFCC");
			highlight("w1", "#00FF00");
			highlight("w2", "#00FFFF");
			
			highlightPart("w2", 1, 3, "#FF0000");
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
		public void showMoreInformation(String word){
			
			new AlertDialog.Builder(context)
			.setTitle(word)
			.setMessage("Word: " + word)
			.setPositiveButton(android.R.string.ok, null).show();
		}
		
		@JavascriptInterface
		public void saveHighlightInformation(String id, String startPos, String endPos){
			Pair<String> span = new Pair<String>(startPos, endPos);
			highlightParts.put(id, span);			
		}
		
		@JavascriptInterface
		public void removeHighlightInformation(String id){
			highlightParts.remove(id);
		}
		
		@JavascriptInterface
		public void clickSentence(String html, final String id){
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					removeSearches();
					
					String curr = sp.getString(CURR_SENT, DEFAULT_SENTENCE);
					removeHighlight(curr);
					
					if(!isHighlighting || !curr.equals(id)){						
						highlight(id);
						spEditor.putString(CURR_SENT, id).commit();
						isHighlighting = true;
						
						if(reader_status == ReaderStatus.Enabled){
							if(reader_mode == ReaderMode.Listen)
								speakFromSentence(id);
							else if(reader_mode == ReaderMode.Guidance){
								highlightHandler.removeCallbacks(highlightRunnable);
								long millis = (long) (highlightSpeed * 1000);
								highlightHandler.postDelayed(highlightRunnable, millis);							
							}
						}
					} else {
						spEditor.putString(CURR_SENT, DEFAULT_SENTENCE).commit();
						isHighlighting = false;
					}
					
					spEditor.putBoolean("highlighting", isHighlighting).commit();
				}
			});
		}
	}


	@Override
	public void OnHighlight(final String id) {
		
		this.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				highlight(id);
			}
		});
	}

	@Override
	public void OnRemoveHighlight(final String id) {
		this.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				removeHighlight(id);
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
	
	private class HighlightRunnable implements Runnable{

		@Override
		public void run() {
			String curr = sp.getString(CURR_SENT, DEFAULT_SENTENCE);
			String identifier = Helper.findIdentifier(curr);
			int pos = Helper.findPosition(curr);
			
			if(pos>=texts.size()-1){
				ibtnPlay.callOnClick();
				return;
			}
			
			removeHighlight(curr);
			curr = identifier + ++pos;
			highlight(curr);
			
			spEditor.putString(CURR_SENT, curr).commit();
			
			long millis = (long) (highlightSpeed * 1000);
			highlightHandler.postDelayed(this, millis);
		}
		
	}
}
