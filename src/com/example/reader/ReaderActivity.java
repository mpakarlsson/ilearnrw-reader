package com.example.reader;

import ilearnrw.annotation.UserBasedAnnotatedWord;
import ilearnrw.annotation.UserBasedAnnotatedWordsSet;
import ilearnrw.textclassification.SeverityOnWordProblemInfo;
import ilearnrw.textclassification.StringMatchesInfo;
import ilearnrw.textclassification.Word;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import com.example.reader.interfaces.OnTextToSpeechComplete;
import com.example.reader.interfaces.TTSHighlightCallback;
import com.example.reader.interfaces.TTSReadingCallback;
import com.example.reader.popups.ModeActivity;
import com.example.reader.popups.SearchActivity;
import com.example.reader.popups.WordActivity;
import com.example.reader.texttospeech.TextToSpeechReader;
import com.example.reader.texttospeech.TextToSpeechUtils;
import com.example.reader.types.Pair;
import com.example.reader.types.singleton.AnnotatedWordsSet;
import com.example.reader.utils.AppLocales;
import com.example.reader.utils.Helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
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

public class ReaderActivity 
	extends 
		Activity 
	implements 
		OnClickListener, 
		OnLongClickListener, 
		OnSeekBarChangeListener, 
		TTSHighlightCallback, 
		TTSReadingCallback,
		OnTextToSpeechComplete {

	private final String TAG = getClass().getName();
	
	private TextView tvTitle;
	private WebView reader;
	private ImageButton ibtnLib, ibtnSearch, ibtnMode, ibtnPrev, ibtnPlay, ibtnNext, ibtnSettings, ibtnSearchForward, ibtnSearchBack;
	private RelativeLayout bottom, searchbar, rlHighlightSpeed;
	private SeekBar sbHighLightSpeed;
	
	private ReaderMode reader_mode;
	private ReaderStatus reader_status;

	private HighlightRunnable highlightRunnable;
	private Handler highlightHandler;
	
	public HashMap<String, Pair<String>> highlightParts;
	
	private SharedPreferences sp;
	private SharedPreferences.Editor spEditor;
	
	private TextToSpeechReader ttsReader;
	
	public String CURR_SENT;
	public String CURR_WORD;
	public final String SENTENCE_TAG	= "sen";
	public final String WORD_TAG 		= "w";
	private ArrayList<String> sentenceIds, wordIds;
	private String defaultSentence = "", defaultWord = "";
	private int currSentPos, currWordPos;
	private String touchedId;
	
	private static String html, bundleJSON, bundleHtml, cleanHtml;
	private String libraryTitle;
	
	private double highlightSpeed;

	private boolean isHighlighting;

	private UserBasedAnnotatedWordsSet annotationData;
	
	private ArrayList<Word> trickyWords;
	
	public final static int FLAG_SEARCH 			= 10000;
	public final static int FLAG_MODE 				= 10001;
	public final static int FLAG_REFRESH_WEBVIEW 	= 10002;
	public final static int FLAG_WORD_POPUP 		= 10004;
	
	
	public static enum ReaderMode {
		Listen("Listen", 0),
		Guidance("Guidance", 1);
		
		private String name;
		private int value;		
		private ReaderMode(String name, int value){
			this.name 	= name;
			this.value 	= value;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public int getValue(){
			return value;
		}
		
		public String getName(){
			return name;
		}
	}
	
	public static enum HighlightMode{
		Sentence("Sentence", 1),
		Word("Word", 2);
		
		private String name;
		private int value;
		
		private HighlightMode(String name, int value){
			this.name 	= name;
			this.value 	= value;
		}

		@Override
		public String toString() {
			return name;
		}
		
		public int getValue(){
			return value;
		}
		
		public String getName(){
			return name;
		}
	}
	
	public static enum ReaderStatus {
		Enabled,
		Disabled
	}
	
	@SuppressWarnings("unchecked")
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
		AppLocales.setLocales(getApplicationContext(), sp.getString(getString(R.string.sp_user_language), "en"));
		
		Bundle libBundle 	= getIntent().getExtras();
		
		bundleHtml			= libBundle.getString("html");
		cleanHtml			= libBundle.getString("cleanHtml");
		bundleJSON			= libBundle.getString("json");
		libraryTitle		= libBundle.getString("title");
		annotationData		= AnnotatedWordsSet.getInstance(this.getApplicationContext()).getUserBasedAnnotatedWordsSet();
		trickyWords			= (ArrayList<Word>) libBundle.get("trickyWords");
		
		spEditor 	= sp.edit();
		Pair<String> bookTitle = Helper.splitFileName(libraryTitle);
		CURR_SENT 		= bookTitle.first() + "_" + bookTitle.second().substring(1) + "_sent";
		CURR_WORD		= bookTitle.first() + "_" + bookTitle.second().substring(1) + "_word";
		
		tvTitle = (TextView) findViewById(R.id.tv_book_title_reader);
		
		if(libraryTitle.endsWith(".txt"))
			libraryTitle = libraryTitle.substring(0, libraryTitle.length()-4);
		
		tvTitle.setText(libraryTitle);
		
		sbHighLightSpeed = (SeekBar) findViewById(R.id.seekbar_highLight_speed);
		sbHighLightSpeed.setOnSeekBarChangeListener(this);

		highlightSpeed = Double.longBitsToDouble(sp.getLong(getString(R.string.pref_highlighter_speed), Double.doubleToLongBits(5.5)));
		
		int hlSpeed = (int) (highlightSpeed * 10);
		int flipValue = sbHighLightSpeed.getMax() - hlSpeed;
		
		sbHighLightSpeed.setProgress(flipValue);
		
		ibtnLib 		= (ImageButton) findViewById(R.id.ibtn_lib_reader);
		ibtnSearch 		= (ImageButton) findViewById(R.id.ibtn_search_reader);
		ibtnMode 		= (ImageButton) findViewById(R.id.ibtn_mode_reader);
		ibtnPrev 		= (ImageButton) findViewById(R.id.ibtn_prev_reader);
		ibtnPlay 		= (ImageButton) findViewById(R.id.ibtn_play_reader);
		ibtnNext 		= (ImageButton) findViewById(R.id.ibtn_next_reader);
		ibtnSettings 	= (ImageButton) findViewById(R.id.ibtn_settings_reader);
		
		searchbar 			= (RelativeLayout) findViewById(R.id.search_buttons_layout);
		ibtnSearchForward 	= (ImageButton) findViewById(R.id.ibtn_search_forward);
		ibtnSearchBack 		= (ImageButton) findViewById(R.id.ibtn_search_back);
		
		bottom 				= (RelativeLayout) findViewById(R.id.reader_bottom);
		rlHighlightSpeed 	= (RelativeLayout) findViewById(R.id.reader_body_highlight_speed);
		
		ibtnLib.setOnClickListener(this);
		ibtnSearch.setOnClickListener(this);
		ibtnMode.setOnClickListener(this);
		ibtnPrev.setOnClickListener(this);
		ibtnPlay.setOnClickListener(this);
		ibtnNext.setOnClickListener(this);
		ibtnSettings.setOnClickListener(this);
		ibtnSearchForward.setOnClickListener(this);
		ibtnSearchBack.setOnClickListener(this);
		
		ibtnPlay.setEnabled(false);
		ibtnNext.setEnabled(false);
		ibtnPrev.setEnabled(false);
		
		searchbar.setVisibility(RelativeLayout.GONE);
		
		//TextToSpeechUtils.checkLanguageData(this);
	
		ttsReader = TextToSpeechReader.getInstance(getApplicationContext());
			
		reader = (WebView) findViewById(R.id.webview_reader);
		reader.setOnLongClickListener(this);
		reader.setLongClickable(false);
		reader.getSettings().setJavaScriptEnabled(true);
		reader.addJavascriptInterface(new ReaderWebInterface(this), "ReaderInterface");
		reader.getSettings().setDefaultFontSize(22);
		
		reader.setWebViewClient(new MyWebViewClient());
		html 		= updateHtml(bundleHtml);
		reader.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", "about:blank");
		
		reader_status = ReaderStatus.Disabled;
		ibtnPlay.setImageResource(R.drawable.image_selector_play);

		touchedId = "w0";
		currSentPos 	= sp.getInt(CURR_SENT, 0);
		currWordPos		= sp.getInt(CURR_WORD, 0);
		isHighlighting 	=  sp.getBoolean(getString(R.string.sp_highlighting), true);

		highlightParts = new HashMap<String, Pair<String>>();
		
		int mode = sp.getInt(getString(R.string.sp_reader_mode), -1);
		if(mode==-1){
			reader_mode = ReaderMode.Listen;
			mode = reader_mode.getValue();
			spEditor.putInt(getString(R.string.sp_reader_mode), mode).commit();
			rlHighlightSpeed.setVisibility(View.GONE);
			Toast.makeText(this, "No reader mode set. Listen mode is selected", Toast.LENGTH_LONG).show();
		} else {
			switch(mode){
			case 0:
				reader_mode = ReaderMode.Listen;
				rlHighlightSpeed.setVisibility(View.GONE);
				break;
			case 1:
				reader_mode = ReaderMode.Guidance;
				rlHighlightSpeed.setVisibility(View.VISIBLE);
				break;
			}
		}
		
		highlightRunnable	= new HighlightRunnable();
		highlightHandler 	= new Handler();
		ibtnPlay.setEnabled(true);
		ibtnNext.setEnabled(true);
		ibtnPrev.setEnabled(true);	

		updateGUI();
	}
	
	@Override
	protected void onResume() {
		TextToSpeechReader.getInstance(getApplicationContext()).activateIdDrive(this, this, this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		turnOffHandler();
		TextToSpeechReader.getInstance(getApplicationContext()).deactivateIdDrive();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {		
		switch (requestCode) {
		case TextToSpeechUtils.FLAG_CHECK_TTS_DATA:
			//if(tts==null){
			//	tts = new TextToSpeechIdDriven(ReaderActivity.this, this,  CURR_SENT, cbHighlight, cbSpoken, requestCode, resultCode, data);
			//	setTTS();
			//}
			break;
		case FLAG_SEARCH:
		{
			if(resultCode == RESULT_OK){
				String searchString = data.getStringExtra("searchString");
				searchbar.setVisibility(RelativeLayout.VISIBLE);
				reader.findAllAsync(searchString);
			}
		}
			break;
		case FLAG_WORD_POPUP:
		{
			if(resultCode == RESULT_OK){
				trickyWords = (ArrayList<Word>) data.getExtras().get("trickyWords");
			}
		}
			break;
		case FLAG_MODE:
		{
			if(resultCode == RESULT_OK){
				int mode = data.getExtras().getInt("chosenMode");				
				switch(mode){
				case 0:
					reader_mode = ReaderMode.Listen;
					rlHighlightSpeed.setVisibility(View.GONE);
					currSentPos = sp.getInt(CURR_SENT, 0);
					highlight(sentenceIds.get(currSentPos));
					removeHighlight(wordIds.get(currWordPos));
					break;
				case 1:
					reader_mode = ReaderMode.Guidance;
					rlHighlightSpeed.setVisibility(View.VISIBLE);
					currWordPos = sp.getInt(CURR_WORD, 0);
					highlight(wordIds.get(currWordPos));
					removeHighlight(sentenceIds.get(currSentPos));
					break;
				}
				spEditor.putInt(getString(R.string.sp_reader_mode), mode).commit();
			}
		}
			break;
			
		case FLAG_REFRESH_WEBVIEW:
		{
			if(data!=null){
				Bundle b = data.getExtras();
				if(b.containsKey("showGUI")){
					boolean show = b.getBoolean("showGUI", false);
					
					if(show){
						Intent i = new Intent(this, ActiveRules.class);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(i);
						return;
					} else {
						Intent intent = new Intent(ReaderActivity.this, ActiveRules.class);
						
						intent.putExtra("html", bundleHtml);
						intent.putExtra("json", bundleJSON);
						intent.putExtra("cleanHtml", cleanHtml);
						intent.putExtra("title", libraryTitle);
						intent.putExtra("loadFiles", false);
						intent.putExtra("showGUI", true);
						startActivity(intent);
						return;
					}
					
				}
			}
			
			updateGUI();
			html = updateHtml(bundleHtml);
			reader.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", "about:blank");
			setTTS();
		}
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void turnOffHandler(){
		highlightHandler.removeCallbacks(highlightRunnable);
	}

	@Override
	public void onBackPressed() {
		Intent i = new Intent(this, LibraryActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(i);
		finish();
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
			if(reader_status == ReaderStatus.Enabled)
				setPlayStatus(ReaderStatus.Disabled, true);
			else if(reader_status == ReaderStatus.Disabled)
				setPlayStatus(ReaderStatus.Disabled, false);
			highlightHandler.removeCallbacks(highlightRunnable);

			if(reader_mode == ReaderMode.Listen)				ttsReader.stop();

			Intent mode_intent = new Intent(this, ModeActivity.class);
			mode_intent.putExtra("posX", ibtnMode.getX());
			mode_intent.putExtra("posY", (bottom.getY()-ibtnMode.getHeight()));
			mode_intent.putExtra("imageHeight", ibtnMode.getHeight());
			mode_intent.putExtra("readerMode", reader_mode);
			startActivityForResult(mode_intent, FLAG_MODE);
			break;
			
		case R.id.ibtn_settings_reader:
			highlightHandler.removeCallbacks(highlightRunnable);
			
			if(reader_status == ReaderStatus.Enabled)
				setPlayStatus(ReaderStatus.Disabled, true);
			
			if(ttsReader.isSpeaking())				ttsReader.stop();
			
			Intent i = new Intent(this, SettingsActivity.class);
			i.putExtra("setting", "reader");
			startActivityForResult(i, FLAG_REFRESH_WEBVIEW);
			break;
			
		case R.id.ibtn_prev_reader:
			stepReader(false);
			break;
			
		case R.id.ibtn_play_reader:
			if(reader_mode == ReaderMode.Listen){
				if(reader_status == ReaderStatus.Disabled){
					setPlayStatus(ReaderStatus.Enabled, true);
					currSentPos = sp.getInt(CURR_SENT, 0);
					String c = sentenceIds.get(currSentPos);
					speakFromSentence(c);
				} else {
					setPlayStatus(ReaderStatus.Disabled, true);
					spEditor.putInt(CURR_SENT, currSentPos).commit();					ttsReader.stop();					ttsReader.rehighlight();
				}
			} else if(reader_mode == ReaderMode.Guidance){
				if(reader_status == ReaderStatus.Disabled){
					setPlayStatus(ReaderStatus.Enabled, true);
					resetGuidance();
				} else {
					setPlayStatus(ReaderStatus.Disabled, true);
					highlightHandler.removeCallbacks(highlightRunnable);
				}
			}
			break;
			
		case R.id.ibtn_next_reader:
			stepReader(true);
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
	
	private void stepReader(boolean forward){
		boolean isSpeaking =ttsReader.isSpeaking();
		boolean doHighlight=true;
		int direction = forward ? -1 : 1;
		
		if(reader_mode == ReaderMode.Listen){
			spEditor.putBoolean(getString(R.string.sp_tts_reader_is_stepping), true).commit();			ttsReader.stop();
			
			if(isSpeaking){
				setPlayStatus(ReaderStatus.Disabled, false);
			}
			
			if(!forward){
				currSentPos--;
				if(currSentPos<0){
					currSentPos=0;
					doHighlight = false;
				}
			} else {
				currSentPos++;
				if(currSentPos>sentenceIds.size()-1){
					currSentPos=sentenceIds.size()-1;
					doHighlight = false;
				}
			}
			
			String current = sentenceIds.get(currSentPos);
			String other = sentenceIds.get(currSentPos+direction);
			
			if(doHighlight){
				removeHighlight(other);
				highlight(current);
			}
			
			spEditor.putInt(CURR_SENT, currSentPos).commit();
			
			if(isSpeaking)
				setPlayStatus(ReaderStatus.Enabled, false);
			
			reader.loadUrl("javascript:updateCurrentPosition('"+sentenceIds.get(currSentPos)+"', 0);");
			
			if(reader_status == ReaderStatus.Enabled)
				speakFromSentence(current);
		} else if(reader_mode == ReaderMode.Guidance){
			if(!forward){
				currWordPos--;
				if(currWordPos<0){
					currWordPos = 0;
					doHighlight = false;
				}
			} else{
				currWordPos++;
				if(currWordPos>wordIds.size()-1){
					currWordPos = wordIds.size()-1;
					doHighlight = false;
				}
			}
			
			String current = wordIds.get(currWordPos);
			String other = wordIds.get(currWordPos+direction);
			
			spEditor.putInt(CURR_WORD, currWordPos).commit();
			
			if(doHighlight){
				removeHighlight(other);
				highlight(current);
			}
			
			reader.loadUrl("javascript:updateCurrentPosition('"+wordIds.get(currWordPos)+"', 1);");
			if(reader_status == ReaderStatus.Enabled)
				resetGuidance();
		}
	}
	
	@Override
	public boolean onLongClick(View v) {
		removeSearches();
		reader.loadUrl("javascript:longClick('"+touchedId+"');");
		return true;
	};
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		switch(seekBar.getId()){
		case R.id.seekbar_highLight_speed:
			int flipValue = seekBar.getMax() - seekBar.getProgress();
			highlightSpeed = (flipValue * 0.1) + 0.5; // Slider values goes from 0.5 to 10.5
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
			int flipValue = seekBar.getMax() - seekBar.getProgress();
			highlightSpeed =  (flipValue * 0.1);
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
	
	private void updateGUI(){
		int sliderProgressColor = 0xff555555;
		
		int backgroundColor = sp.getInt("pref_background_color", 0xffffffff);
		
		int colors[] = new int[3];
		colors[0] = Helper.darkenColor(sliderProgressColor, 0.1f);
		colors[1] = sliderProgressColor;
		colors[2] = Helper.lightenColor(sliderProgressColor, 0.9f);
		
		GradientDrawable shape = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
		shape.setCornerRadius(5.0f);
		shape.setStroke((int)getResources().getDimension(R.dimen.slider_stroke), 0x50999999);
		
		Rect bounds = sbHighLightSpeed.getProgressDrawable().getBounds();
		sbHighLightSpeed.setProgressDrawable(shape);
		sbHighLightSpeed.getProgressDrawable().setBounds(bounds);
		
		int sliderColor = Helper.darkenColor(backgroundColor, 0.95f);
		sbHighLightSpeed.setBackgroundColor(sliderColor);
		rlHighlightSpeed.setBackgroundColor(sliderColor);
	}
	
	public void highlight(String id){
		id = checkId(id);
		if(id != null){
			//reader.loadUrl("javascript:scrollToElement('" + id + "');");
			
			String highlightColor = "#" + Integer.toHexString(sp.getInt(getString(R.string.pref_highlight_color_title),  Color.argb(255, 255, 255, 0))).substring(2);
			reader.loadUrl("javascript:highlight('" + id + "', '" + highlightColor + "');");
		}
	}
	
	public void highlight(String id, String hexColor){
		id = checkId(id);
		if(id != null)
			reader.loadUrl("javascript:highlight('" + id + "', '" + hexColor + "');");

	}
	
	public void highlightPart(String id, final int start, final int end, final String hexColor){
		final String fId = checkId(id);
		if(fId == null)
			return;
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				reader.loadUrl("javascript:highlightPart('" + fId + "', '" + start + "', '" + end + "', '" + hexColor + "');");
			}
		});
		
	}
	
	public void removeHighlight(String id){
		id = checkId(id);
		if(id != null){
			reader.loadUrl("javascript:removeHighlight('" + id + "');");
		}
	}
	
	public void removeHighlightPart(String id, final Pair<String> span){
		final String fId = checkId(id);
		if(fId == null)
			return;
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				reader.loadUrl("javascript:unhighlight('" + fId + "', '" + span.first() + "', '" + span.second() + "');");
			}
		});
	}
	
	private String checkId(String id){
		if(id == null || id.isEmpty()){
			if(reader_mode == ReaderMode.Listen){
				if(!defaultSentence.isEmpty())
					return defaultSentence;
				else
					return null;
			} else if(reader_mode == ReaderMode.Guidance){
				if(!defaultWord.isEmpty())
					return defaultWord;
				else
					return null;
			}
		}
		return id;
	}
	
	private void resetGuidance(){
		highlightHandler.removeCallbacks(highlightRunnable);
		long millis = (long) (highlightSpeed * 1000);
		highlightHandler.postDelayed(highlightRunnable, millis);
	}
	
	private void setPlayStatus(ReaderStatus status, boolean changeImage){	
		if(changeImage){
			if(status == ReaderStatus.Enabled)	
				ibtnPlay.setImageResource(R.drawable.image_selector_pause);
			else
				ibtnPlay.setImageResource(R.drawable.image_selector_play);
		}
		reader_status = status;
	}
	
	private void setTTS(){		
		int pitchRate 	= sp.getInt(getString(R.string.pref_pitch_title), 9);
		int speechRate 	= sp.getInt(getString(R.string.pref_speech_rate_title), 9);

		double pitch = ((pitchRate + 1.0) / 10.0);		ttsReader.setPitch((float)pitch);
		double speech = ((speechRate + 1.0) / 10.0);		ttsReader.setSpeechRate((float)speech);
		
		//tts.setLanguage(Locale.UK);
	}
	
	private void speakFromSentence(String id){
		id = checkId(id);
		if(id==null)
			return;
		
		final String _id = id;
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				reader.loadUrl("javascript:speakSentence('" + _id + "');");
			}
		});
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
		
		String removeHighlight =
				"function removeHighlight(id){" +
						"var element = document.getElementById(id);" +
						"element.style.backgroundColor='transparent';" +
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
		
		String getSentences = 
				"function getSentences(){" +
					"var sents = document.getElementsByTagName('" + SENTENCE_TAG + "');" +
					"var result = '';" +
					"for(var i=0; i<sents.length; i++){" +
						"if(i+1==sents.length){" +
							"result += sents[i].id;" +
						"} else {" +
							"result += sents[i].id + ',';" +
						"}" +
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
					"ReaderInterface.getSentences(result);" +
				"}";
		
		String getWords =
				"function getWords(){" +
						
					"var words = document.getElementsByTagName('" +  WORD_TAG + "');" +
					"var result = '';" +
					"for(var i=0; i<words.length; i++){" +
						"words[i].ontouchstart = function(){" +
							"ReaderInterface.touchWord(this.id);" +
						"};" +
						
						"if(i+1==words.length){" +
							"result += words[i].id;" +
						"} else {" +
							"result += words[i].id + ',';" +
						"}" +
							
						"words[i].onclick = function() {" +
							"var body = document.body.innerHTML;" +
							"var index = body.indexOf(this.id);" +
							"var part = body.substring(0, index);" +
							"var lastIndex = part.lastIndexOf('<');" +
							"part = part.substring(lastIndex);" +
							"body = body.substring(body.indexOf(this.id));" +
							"body = part + body;" +
							"ReaderInterface.clickWord(body, this.id);" + 
						"};" +
					"}" +
					"ReaderInterface.getWords(result);" +
				"}";
		
		String updatePosition = 
				"function updateCurrentPosition(id, checkParent){" +
					"var node =  document.getElementById(id);" +
					"var other = '';" +
					"if(checkParent==1) {" +
						"other = node.parentNode.id;" +
					"} else {" +
						"other = node.childNodes[0].id;" +
					"}" +
					"ReaderInterface.updateCurrentPosition(other, checkParent);" +
					
				"}";
		
		String speakSentence = 
				"function speakSentence(id){" +
					"var element = document.getElementById(id);" +
					"ReaderInterface.speakSentence(element.innerText);" +
				"}";
		
		String longClick = 
				"function longClick(id) {" +
					"var word = document.getElementById(id);" +
					"ReaderInterface.longClick(word.innerHTML);" +
				"}";
		
		String setCSSLink = "<link rel='stylesheet' href='css/default.css' type='text/css'>";
		
		String backgroundColor 	= Integer.toHexString(sp.getInt(getString(R.string.pref_background_color_title), Color.argb(255,255,255,255)));
		String textColor 		= Integer.toHexString(sp.getInt(getString(R.string.pref_text_color_title), Color.argb(255,0,0,0)));
		
		backgroundColor 		= "#" + backgroundColor.substring(2);
		textColor 				= "#" + textColor.substring(2);
		
		String lineHeight 		= sp.getString(getString(R.string.pref_line_height_title), "125");
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
					
					"outline-style: none;" +
					"-webkit-touch-callout: none;" +
					"-webkit-tap-highlight-color: rgba(0,0,0,0);" +
				"}" +
				"</style>" +
				"";
		
		return firstPart +
				startScripts + 
				highlightSentence +
				highlightPart +
				removeHighlight +
				unhighlightPart +
				retrieveBodyContent +
				scrollToElement +
				getSentences +
				getWords +
				speakSentence +
				updatePosition +
				showToast + 
				longClick +
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
			
			reader.loadUrl("javascript:getSentences();");
			reader.loadUrl("javascript:getWords();");
			
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
		public void saveHighlightInformation(String id, String startPos, String endPos){
			Pair<String> span = new Pair<String>(startPos, endPos);
			highlightParts.put(id, span);			
		}
		
		@JavascriptInterface
		public void removeHighlightInformation(String id){
			highlightParts.remove(id);
		}
		
		@JavascriptInterface
		public void getSentences(String sentences){			
			sentenceIds = new ArrayList<String>(Arrays.asList(sentences.split(",")));
			defaultSentence = sentenceIds.get(0);
			
			if(currSentPos>=sentenceIds.size()-1)
				currSentPos=sentenceIds.size()-1;
			
			final String current = sentenceIds.get(currSentPos);
			
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(isHighlighting && reader_mode==ReaderMode.Listen)
						highlight(current);	
				}
			});
		}
		
		@JavascriptInterface
		public void getWords(String words){
			wordIds = new ArrayList<String>(Arrays.asList(words.split(",")));
			defaultWord = wordIds.get(0);
			
			if(currWordPos >= wordIds.size()-1)
				currWordPos =  wordIds.size()-1;
			
			final String current = wordIds.get(currWordPos);
			
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(isHighlighting && reader_mode==ReaderMode.Guidance)
						highlight(current);
				}
			});
			
		}
		
		@JavascriptInterface
		public void speakSentence(String text){
			if(currSentPos==sentenceIds.size()-1)				ttsReader.speak(text, currSentPos, true);
			else				ttsReader.speak(text, currSentPos, false);
		}
		
		@JavascriptInterface
		public void touchWord(String id){
			touchedId = id;
		}
		
		@JavascriptInterface
		public void longClick(String jsWord){		
			ArrayList<UserBasedAnnotatedWord> words = annotationData.getWords();
			String _word = "";
			
			_word = jsWord.toLowerCase(Locale.getDefault());
			
			_word = _word.replace("\n", "");
			_word = _word.trim();
			
			ArrayList<Integer> values = new ArrayList<Integer>();
			ArrayList<String> datas = new ArrayList<String>();
			
			for(int i=0; i<words.size(); i++){
				UserBasedAnnotatedWord word = words.get(i);
				if(_word.equals(word.getWord())){
					Intent in = new Intent(getBaseContext(), WordActivity.class);
					in.putExtra("word", word.getWord());
					in.putExtra("stem", word.getStem());
					in.putExtra("phoneme", word.getPhonetics());
					in.putExtra("wordInSyllables", word.getWordInToSyllables());
					in.putExtra("trickyWords", trickyWords);
					
					ArrayList<SeverityOnWordProblemInfo> problems = word.getUserSeveritiesOnWordProblems();
					for(int j=0; j<problems.size(); j++){
						SeverityOnWordProblemInfo problem = problems.get(j);
						problem.getCategory();
						problem.getIndex();
						
						ArrayList<StringMatchesInfo> infos = problem.getMatched();
						
						for(int k=0; k<infos.size(); k++){
							StringMatchesInfo info = infos.get(k);
							
							values.add(j);
							values.add(info.getStart());
							values.add(info.getEnd());
							
							datas.add(null);
							datas.add(Integer.toString(problem.getCategory()));
							datas.add(Integer.toString(problem.getIndex()));
						}
					}
					
					in.putIntegerArrayListExtra("problems", values);
					in.putStringArrayListExtra("data", datas);
					startActivityForResult(in, FLAG_WORD_POPUP);
					break;
				}
			}
		}
		
		@JavascriptInterface
		public void clickSentence(String html, final String id){
			if(reader_mode != ReaderMode.Listen)
				return;
			
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					removeSearches();
					
					spEditor.putBoolean(getString(R.string.sp_tts_reader_is_stepping), false).commit();
					

					String curr = sentenceIds.get(currSentPos);
					removeHighlight(curr);
					
					for(int i=0;i<sentenceIds.size(); i++){
						if(sentenceIds.get(i).equals(id)){
							currSentPos = i;
							break;
						}
					}
					
					if(!isHighlighting || !curr.equals(id)){						
						highlight(id);
						spEditor.putInt(CURR_SENT, currSentPos).commit();
						isHighlighting = true;
						
						if(reader_status == ReaderStatus.Enabled)
							speakFromSentence(id);
						
					} else {
						spEditor.putInt(CURR_SENT, currSentPos).commit();
						isHighlighting = false;
					}
					spEditor.putBoolean("highlighting", isHighlighting).commit();
					reader.loadUrl("javascript:updateCurrentPosition('"+sentenceIds.get(currSentPos)+"', 0);");
				}
			});
		}
		
		@JavascriptInterface
		public void clickWord(String html, final String id){
			if(reader_mode != ReaderMode.Guidance)
				return;
			
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					removeSearches();
					
					String curr = wordIds.get(currWordPos);
					removeHighlight(curr);
					
					for(int i=0;i<wordIds.size(); i++){
						if(wordIds.get(i).equals(id)){
							currWordPos = i;
							break;
						}
					}
					
					if(!isHighlighting || !curr.equals(id)){						
						highlight(id);
						spEditor.putInt(CURR_WORD, currWordPos).commit();
						isHighlighting = true;
						
						if(reader_status == ReaderStatus.Enabled){
								highlightHandler.removeCallbacks(highlightRunnable);
								long millis = (long) (highlightSpeed * 1000);
								highlightHandler.postDelayed(highlightRunnable, millis);							
						}
					} else {
						spEditor.putInt(CURR_WORD, currWordPos).commit();
						isHighlighting = false;
					}
					
					spEditor.putBoolean(getString(R.string.sp_highlighting), isHighlighting).commit();
					
					reader.loadUrl("javascript:updateCurrentPosition('"+wordIds.get(currWordPos)+"', 1);");
				}
			});
			
		}
	
		@JavascriptInterface
		public void updateCurrentPosition(String other, int isParent){
			switch (isParent) {
			case 0:
				for(int i=0; i<wordIds.size(); i++){
					if(currWordPos != i && wordIds.get(i).equals(other)){
						spEditor.putInt(CURR_WORD, i).commit();
						break;
					}
				}
				
				break;

			case 1:
				for(int i=0; i<sentenceIds.size(); i++){
					if(currSentPos != i && sentenceIds.get(i).equals(other)){
						spEditor.putInt(CURR_SENT, i).commit();
						break;
					}
				}
				break;
			}
		}
	}

	@Override
	public void OnHighlight(int id) {
		final String curr = sentenceIds.get(id);
		
		this.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				highlight(curr);
			}
		});
	}

	@Override
	public void OnRemoveHighlight(int id, boolean read) {
		final String curr = sentenceIds.get(id);
		
		if(read){
			if(reader_status == ReaderStatus.Enabled){
				int next = ++id;
				
				if(next==++currSentPos){
					spEditor.putInt(CURR_SENT, next).commit();
					currSentPos = next;
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							reader.loadUrl("javascript:updateCurrentPosition('"+sentenceIds.get(currSentPos)+"', 0);");
						}
					});
					
					speakFromSentence(sentenceIds.get(next));
				}
			}
		}

		this.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				removeHighlight(curr);
			}
		});
	}

	@Override
	public void OnFinishedReading() {
		this.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				if(reader_status != ReaderStatus.Disabled)
					setPlayStatus(ReaderStatus.Disabled, true);
			}
		});
	}

	@Override
	public void OnStartedReading() {
		this.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				if(reader_status != ReaderStatus.Enabled)
					setPlayStatus(ReaderStatus.Enabled, true);
			}
		});
	}
	
	@Override
	public void onTextToSpeechInitialized() {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ibtnPlay.setEnabled(true);
				ibtnNext.setEnabled(true);
				ibtnPrev.setEnabled(true);
			}
		});
		
	}
	
	private class HighlightRunnable implements Runnable{
		@Override
		public void run() {
			String prev = "", current = "";
			if(reader_mode == ReaderMode.Listen){
				if(currSentPos==sentenceIds.size()-1){
					ibtnPlay.callOnClick();
					return;
				}
				prev 	= sentenceIds.get(currSentPos++);
				current	= sentenceIds.get(currSentPos);
				spEditor.putInt(CURR_SENT, currSentPos).commit();
				
				runOnUiThread(new Runnable() {
					public void run() {
						reader.loadUrl("javascript:updateCurrentPosition('"+sentenceIds.get(currSentPos)+"', 0);");
					}
				});
				
			} else if(reader_mode == ReaderMode.Guidance){
				if(currWordPos==wordIds.size()-1){
					ibtnPlay.callOnClick();
					return;
				}
				prev 	= wordIds.get(currWordPos++);
				current	= wordIds.get(currWordPos);
				spEditor.putInt(CURR_WORD, currWordPos).commit();
				runOnUiThread(new Runnable() {
					public void run() {
						reader.loadUrl("javascript:updateCurrentPosition('"+wordIds.get(currWordPos)+"', 1);");
					}
				});
				
			}
			
			removeHighlight(prev);
			highlight(current);
			
			long millis = (long) (highlightSpeed * 1000);
			highlightHandler.postDelayed(this, millis);
		}
	}
}
