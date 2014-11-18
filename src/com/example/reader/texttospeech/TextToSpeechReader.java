package com.example.reader.texttospeech;

import java.util.ArrayList;
import java.util.Locale;

import com.example.reader.R;
import com.example.reader.interfaces.TTSHighlightCallback;
import com.example.reader.interfaces.TTSReadingCallback;
import com.example.reader.utils.Helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.Toast;

public class TextToSpeechReader 
	implements 
		OnInitListener
		{

	private static TextToSpeechReader instance;
	private static Context context;
	private static TextToSpeech tts;
	private static Locale locale;
	
	private static int position;
	private static int startedPosition;
	
	private static final String SENTENCE_TAG = "sen";
	
	private static TTSHighlightCallback cbHighlight;
	private static TTSReadingCallback cbReading;
	
	private static SharedPreferences sp;
	
	private TextToSpeechReader(){
	}
	
	public static TextToSpeechReader getInstance(Context c){
		if(instance==null)
			instance = new TextToSpeechReader();
		
		if(context == null){
			context = c;
			sp = PreferenceManager.getDefaultSharedPreferences(context);
		}
		
		return instance;
	}

	@Override
	public void onInit(final int status) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if(status == TextToSpeech.SUCCESS){
					
					int result = TextToSpeech.LANG_MISSING_DATA;
					result = tts.setLanguage(locale);
					
					if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
						Intent installIntent = new Intent();
						installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
						installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(installIntent);
						
						Toast.makeText(context, "Initialization failed", Toast.LENGTH_SHORT).show();
						Log.e("TTS", "TTS:onInit: Language not supported");
					}
				} else {
					Toast.makeText(context, "Initialization failed", Toast.LENGTH_SHORT).show();
					Log.e("TTS", "TTS:onInit: Initialization failed");
				}
				
				tts.setPitch(1.0f);
				tts.setSpeechRate(1.0f);
			}
		}).start();
		
		tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
			@Override
			public void onStart(String utteranceId) {
				startedPosition = position;
				if(!utteranceId.equals("rehighlight"))
					cbReading.OnStartedReading();
				
				if(utteranceId.equals("speakWithId")){
					cbHighlight.OnHighlight(position);
					sp.edit().putInt(SENTENCE_TAG, position).apply();
				} else if(utteranceId.equals("lastSpeakWithId")){
					cbHighlight.OnHighlight(position);
					sp.edit().putInt(SENTENCE_TAG, 0).apply();
				}
			}
			
			@Override
			public void onError(String utteranceId) {}
			
			@Override
			public void onDone(String utteranceId) {
				boolean isStepping = sp.getBoolean(context.getString(R.string.sp_tts_reader_is_stepping), false);
				if(isStepping){
					sp.edit().putBoolean(context.getString(R.string.sp_tts_reader_is_stepping), false).apply();
					return;
				}
				
				if(startedPosition != position)
					return;
				
				if(utteranceId.equals("speakWithId")){
					cbHighlight.OnRemoveHighlight(position, true);
				}else if(utteranceId.equals("lastSpeakWithId")){
					cbHighlight.OnRemoveHighlight(position, false);
					cbReading.OnFinishedReading();
				} else if(utteranceId.equals("rehighlight")){
					cbHighlight.OnHighlight(sp.getInt(SENTENCE_TAG, 0));
					cbReading.OnFinishedReading();
				}
			}
		});
	}
	
	public void initializeTextToSpeech(Locale loc){
		if(tts == null){
			PackageManager pm 	= context.getPackageManager();
			ArrayList<String> availableEngines = new ArrayList<String>();
	
			String strGoogle 	= "com.google.android.tts";
			String strIvona 	= "com.ivona.tts";
			String strSamsung 	= "com.samsung.SMT";

			// Prioritize order - Google, Ivona, Samsung, Other
			if(Helper.isPackageInstalled(pm, strGoogle))
				availableEngines.add(strGoogle);
			
			if(Helper.isPackageInstalled(pm, strIvona))
				availableEngines.add(strIvona);
			
			if(Helper.isPackageInstalled(pm, strSamsung))
				availableEngines.add(strSamsung);
			
			if(availableEngines.isEmpty())
				tts = new TextToSpeech(context, this);
			else 
				tts = new TextToSpeech(context, this, availableEngines.get(0));
		}
		
		locale = loc;
	}
	
	public void activateIdDrive(Activity activity, TTSHighlightCallback highlight, TTSReadingCallback reading){
		cbHighlight 	= highlight;
		cbReading 		= reading;
	}
	
	public void deactivateIdDrive(){
		cbHighlight 	= null;
		cbReading 		= null;
	}
	
	public void stop(){
		tts.stop();
	}
	
	public void destroy(){
		if(tts != null){
			tts.stop();
			tts.shutdown();
		}
	}
	
	public boolean isSpeaking(){
		return tts.isSpeaking();
	}	
	
	public void speak(String text){
		if(text == null || text.isEmpty())
			return;
		
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	public void speak(String text, int position, boolean isFinal){
		if(text==null || text.isEmpty())
			return;

		this.position = position;
		
		String value = !isFinal ? "speakWithId" : "lastSpeakWithId";		
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, TextToSpeechUtils.makeParamsWith(value));
	}
	
	public void rehighlight(){
		tts.speak("", TextToSpeech.QUEUE_FLUSH, TextToSpeechUtils.makeParamsWith("rehighlight"));
	}
	
	public boolean setPitch(float pitch){
		if(pitch>0){
			tts.setPitch(pitch);
			return true;
		}
		return false;
	}

	public boolean setSpeechRate(float speechRate){
		if(speechRate>0){
			tts.setSpeechRate(speechRate);
			return true;
		}
		return false;
	}
	
	public void setLanguage(Locale language){
		int result = TextToSpeech.LANG_MISSING_DATA;
		result = tts.setLanguage(language);
		
		if(result == TextToSpeech.LANG_MISSING_DATA ||
				result == TextToSpeech.LANG_NOT_SUPPORTED) {
			Toast.makeText(context, "Setting language failed", Toast.LENGTH_SHORT).show();
			Log.e("TTS", "TTS:setLanguage: Language not supported");
		}
	}
}
