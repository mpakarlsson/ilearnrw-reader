package com.example.reader.texttospeech;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.example.reader.R;
import com.example.reader.interfaces.OnTextToSpeechComplete;
import com.example.reader.interfaces.TTSHighlightCallback;
import com.example.reader.interfaces.TTSReadingCallback;

public class TextToSpeechReaderId extends TextToSpeechReaderBase {
	private final TTSHighlightCallback cbHighlight;
	private final TTSReadingCallback cbReading;
	
	private int position;
	private int startedPosition;
	
	private final String sentenceTag;
	
	public TextToSpeechReaderId(final Context context, OnTextToSpeechComplete listener, Locale loc, String sentTag,
			TTSHighlightCallback highlight, TTSReadingCallback reading){
		super(context, listener, loc);
		
		cbHighlight = highlight;
		cbReading = reading;
		
		this.sentenceTag = sentTag;
		
		final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);		
		tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
			@Override
			public void onStart(String utteranceId) {
				startedPosition = position;
				if(!utteranceId.equals("rehighlight"))
					cbReading.OnStartedReading();
				
				if(utteranceId.equals("speakWithId")){
					cbHighlight.OnHighlight(position);
					sp.edit().putInt(sentenceTag, position).apply();
				} else if(utteranceId.equals("lastSpeakWithId")){
					cbHighlight.OnHighlight(position);
					sp.edit().putInt(sentenceTag, 0).apply();
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
					cbHighlight.OnHighlight(sp.getInt(sentenceTag, 0));
					cbReading.OnFinishedReading();
				}
			}
		});
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
}
