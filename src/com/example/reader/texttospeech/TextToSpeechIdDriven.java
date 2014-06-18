package com.example.reader.texttospeech;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import com.example.reader.interfaces.OnTextToSpeechComplete;
import com.example.reader.interfaces.TTSHighlightCallback;
import com.example.reader.interfaces.TTSReadingCallback;

public class TextToSpeechIdDriven extends TextToSpeechBase {

	private final TTSHighlightCallback cbHighlight;
	private final TTSReadingCallback cbReading;
	
	private int position;
	
	private final String SENTENCE_TAG;
	
	public TextToSpeechIdDriven(Context context, OnTextToSpeechComplete listener, String sentTag,
			TTSHighlightCallback highlight, TTSReadingCallback reading,
			int requestCode, int resultCode, Intent data) {
		super(context, listener, requestCode, resultCode, data);
		
		this.cbHighlight = highlight;
		this.cbReading = reading;
		
		SENTENCE_TAG = sentTag;
		
		final Context _c = context;
		if(Build.VERSION.SDK_INT >= 15){
			tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
				@Override
				public void onStart(String utteranceId) {
					if(!utteranceId.equals("rehighlight"))
						cbReading.OnStartedReading();
					
					if(utteranceId.equals("speakWithId")){
						cbHighlight.OnHighlight(position);
						PreferenceManager.getDefaultSharedPreferences(_c).edit().putInt(SENTENCE_TAG, position).commit();
					} else if(utteranceId.equals("lastSpeakWithId")){
						cbHighlight.OnHighlight(position);
						PreferenceManager.getDefaultSharedPreferences(_c).edit().putInt(SENTENCE_TAG, 0).commit();
					}
				}
				
				@Override
				public void onError(String utteranceId) {}
				
				@Override
				public void onDone(String utteranceId) {
					if(utteranceId.equals("speakWithId")){
						cbHighlight.OnRemoveHighlight(position, true);
					}else if(utteranceId.equals("lastSpeakWithId")){
						cbHighlight.OnRemoveHighlight(position, false);
						cbReading.OnFinishedReading();
					} else if(utteranceId.equals("rehighlight")){
						cbHighlight.OnHighlight(PreferenceManager.getDefaultSharedPreferences(_c).getInt(SENTENCE_TAG, 0));
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
	
	public void speak(String text, int position, boolean isFinal){
		if(text==null || text.isEmpty()){
			Toast.makeText(context, "TTS: Speak: No data to speak", Toast.LENGTH_LONG).show();
			return;
		}

		this.position = position;
		
		HashMap<String, String> params = new HashMap<String, String>();
		String value = !isFinal ? "speakWithId" : "lastSpeakWithId";
		params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, value);
		
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, params);
	}
	
	public void rehighlight(){
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "rehighlight");
		tts.speak("", TextToSpeech.QUEUE_FLUSH, params);
	}

}
