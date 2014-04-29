package com.example.reader.tts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;

import com.example.reader.callbacks.TTSFinishedReadingCallback;
import com.example.reader.callbacks.TTSHighlightCallback;

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
	private TTSHighlightCallback cbHighlight;
	private TTSFinishedReadingCallback cbFinished;
	private LinkedList<Integer> positionList;
	
	public TTS(Context context, final TTSHighlightCallback cbHighlight, final TTSFinishedReadingCallback cbFinished, int requestCode, int resultCode, Intent data){
		
		this.context = context;
		this.cbHighlight = cbHighlight;
		this.cbFinished = cbFinished;
		positionList = new LinkedList<Integer>();
		
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
		ArrayList<String> availableVoices = bundle.getStringArrayList("availableVoices");
	
		// TODO: Should this be an array on the tts or should this information be within SharedPreferences?
		for(String voice : availableVoices){
			ttsVoices.add(voice);
		}
		
		if(Build.VERSION.SDK_INT >= 15){
			tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
				
				@Override
				public void onStart(String utteranceId) {
					
					int id = positionList.getFirst();
					
					if(utteranceId.equals("speak")){
						Log.d("Speak: Done", "Done");
						cbHighlight.OnHighlight(Integer.toString(id));
					} else if(utteranceId.equals("lastSpeak")){
						cbHighlight.OnHighlight(Integer.toString(id));
					}
				}
				
				@Override
				public void onError(String utteranceId) {
					
				}
				
				@Override
				public void onDone(String utteranceId) {
					
					int id = positionList.pollFirst();
					
					if(utteranceId.equals("speak")){
						Log.d("Speak: Started", "Start");
						cbHighlight.OnRemoveHighlight(Integer.toString(id));
						//currentId++;
					}else if(utteranceId.equals("lastSpeak")){
						cbHighlight.OnRemoveHighlight(Integer.toString(id));
						cbFinished.OnFinishedReading();
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
	
	public void speak(ArrayList<String> texts, int id, String clickType){
		if(texts == null || texts.isEmpty()){
			Toast.makeText(context, "TTS:Speak: No data to speak", Toast.LENGTH_LONG).show();
		} else {
			
			if(!positionList.isEmpty()){
				if(clickType.equals("button")){
						positionList.clear();
				} else if(clickType.equals("click")){
						int currId = positionList.pollFirst();
						positionList.clear();
						positionList.add(currId);
				}
			}
			
			
			
			positionList.addLast(id);
			
			HashMap<String, String> params = new HashMap<String, String>();
			HashMap<String, String> lastParams = new HashMap<String, String>();
			params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "speak");
			lastParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "lastSpeak");
			
			for(int i=0; i<texts.size(); i++){
				positionList.addLast(id + i + 1);
				if(i==0 && texts.size()>1)
					tts.speak(texts.get(i), TextToSpeech.QUEUE_FLUSH, params);
				else if(i==0 && texts.size()==1)
					tts.speak(texts.get(i), TextToSpeech.QUEUE_FLUSH, lastParams);
				else if(i>0 && i<texts.size()-1)
					tts.speak(texts.get(i), TextToSpeech.QUEUE_ADD, params);
				else
					tts.speak(texts.get(i), TextToSpeech.QUEUE_ADD, lastParams);
			}
		}
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
	
	
}
