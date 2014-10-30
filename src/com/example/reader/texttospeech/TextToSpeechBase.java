package com.example.reader.texttospeech;

import java.util.ArrayList;
import java.util.Locale;

import com.example.reader.ReaderActivity;
import com.example.reader.interfaces.OnTextToSpeechComplete;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.Toast;

public class TextToSpeechBase implements OnInitListener{

	public static final int TTS_INSTALLED_CODE = ReaderActivity.FLAG_CHECK_TTS;
	
	protected Context context;
	protected TextToSpeech tts;	
	
	protected ArrayList<String> ttsVoices;
	protected String chosenVoice;
	
	protected OnTextToSpeechComplete listener;
	
	public TextToSpeechBase(final Context context, OnTextToSpeechComplete listener, int requestCode, int resultCode, Intent data){
		this.context 	= context;
		this.ttsVoices	= new ArrayList<String>();
		this.listener	= listener;
		
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
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		chosenVoice = prefs.getString("ttsLanguage", Locale.US.toString());
		
		ArrayList<String> availableVoices = bundle.getStringArrayList(TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES);
	
		for(String voice : availableVoices){
			ttsVoices.add(voice);
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
		
		tts.setPitch(1.0f);
		tts.setSpeechRate(1.0f);
		listener.onTextToSpeechInitialized();
	}
	
	public void speak(String text){
		if(text==null || text.isEmpty()){
			Toast.makeText(context, "TTS: Speak: No data to speak", Toast.LENGTH_LONG).show();
			return;
		}
		
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
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
