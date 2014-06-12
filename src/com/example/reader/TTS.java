package com.example.reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import com.example.reader.interfaces.TTSHighlightCallback;
import com.example.reader.interfaces.TTSReadingCallback;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.Toast;

public class TTS implements OnInitListener{

	public static final int TTS_INSTALLED_CODE = 0;
	
	private TextToSpeech tts;
	private ArrayList<String> ttsVoices;
	private String chosenVoice;
	private Context context;
	private final TTSHighlightCallback cbHighlight;
	private final TTSReadingCallback cbReading;

	private int position;
	
	private final String SENTENCE_TAG;
	
	public TTS(final Context context, String sentTag, TTSHighlightCallback highlight, TTSReadingCallback reading, int requestCode, int resultCode, Intent data){
		
		this.context = context;
		this.cbHighlight = highlight;
		this.cbReading = reading;
		
		SENTENCE_TAG = sentTag;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		chosenVoice = prefs.getString("ttsLanguage", Locale.US.toString());
		ttsVoices = new ArrayList<String>();
		
		if(requestCode == TTS_INSTALLED_CODE){
			if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
				tts = new TextToSpeech(context, this);
			} else {
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				context.startActivity(installIntent);
			}
		}
		
		Bundle bundle = data.getExtras();
		
		// These are available 'languages' not 'voices' and any third party app can do changes to these. 
		
		ArrayList<String> availableVoices = bundle.getStringArrayList(TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES);
	
		for(String voice : availableVoices){
			ttsVoices.add(voice);
		}
		
		if(Build.VERSION.SDK_INT >= 15){
			tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
				
				@Override
				public void onStart(String utteranceId) {
					if(!utteranceId.equals("rehighlight"))
						cbReading.OnStartedReading();
					
					if(utteranceId.equals("speak")){
						cbHighlight.OnHighlight(position);
						PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(SENTENCE_TAG, position).commit();
					} else if(utteranceId.equals("lastSpeak")){
						cbHighlight.OnHighlight(position);
						PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(SENTENCE_TAG, 0).commit();
					}
				}
				
				@Override
				public void onError(String utteranceId) {
					
				}
				
				@Override
				public void onDone(String utteranceId) {
					if(utteranceId.equals("speak")){
						cbHighlight.OnRemoveHighlight(position, true);
					}else if(utteranceId.equals("lastSpeak")){
						cbHighlight.OnRemoveHighlight(position, false);
						cbReading.OnFinishedReading();
					} else if(utteranceId.equals("rehighlight")){
						cbHighlight.OnHighlight(PreferenceManager.getDefaultSharedPreferences(context).getInt(SENTENCE_TAG, 0));
						cbReading.OnFinishedReading();
					}
				}
			});
		} else {
			// TODO: Should we handle devices older than VER: 4.0.3 - 4.0.4 
			// Then we need to handle the speaking differently
			//tts.setOnUtteranceCompletedListener(listener)
		}
	}
	
	@Override
	public void onInit(int status) {
		
		if(status == TextToSpeech.SUCCESS) {
			int result = TextToSpeech.LANG_MISSING_DATA;
			
			String language = "", country = "";
			int connectorPos = chosenVoice.indexOf("-");
			if(connectorPos == -1)
				connectorPos = chosenVoice.indexOf("_");
			
			if(connectorPos != -1){
				language  = chosenVoice.substring(0, connectorPos);
				country = chosenVoice.substring(connectorPos+1);
				result = tts.setLanguage(new Locale(language, country));
				
			} else {
				Toast.makeText(context, "TTS:onInit: Not a valid Locale, setting it to US", Toast.LENGTH_SHORT).show();
				result = tts.setLanguage(Locale.US);
			}
			
			if(result == TextToSpeech.LANG_MISSING_DATA ||
					result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Toast.makeText(context, "TTS:onInit: Language not supported", Toast.LENGTH_SHORT).show();
				Log.e("TTS", "Initialization failed");
			}
		} else {
			Toast.makeText(context, "TTS:onInit: Initialization failed", Toast.LENGTH_SHORT).show();
			Log.e("TTS", "Initialization failed");
		}
	}
	
	public void speak(String text, int position, boolean isFinal){
		if(text==null || text.isEmpty()){
			Toast.makeText(context, "TTS: Speak: No data to speak", Toast.LENGTH_LONG).show();
			return;
		}

		this.position = position;
		
		HashMap<String, String> params = new HashMap<String, String>();
		String value = !isFinal ? "speak" : "lastSpeak";
		params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, value);
		
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, params);
	}
	
	public void rehighlight(){
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "rehighlight");
		tts.speak("", TextToSpeech.QUEUE_FLUSH, params);
	}
	
	public void stop(){
		tts.stop();
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
		tts.setLanguage(language);
	}
	
	public boolean isSpeaking(){
		return tts.isSpeaking();
	}
	
	public void destroy(){
		if(tts != null){
			tts.stop();
			tts.shutdown();
		}
	}
	
	public ArrayList<String> getTTSVoices(){
		return ttsVoices;
	}
	
	
}
