package com.example.reader.texttospeech;


import java.util.ArrayList;
import java.util.Locale;

import com.example.reader.interfaces.OnTextToSpeechComplete;
import com.example.reader.utils.Helper;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.Toast;

public class TextToSpeechBase implements OnInitListener{

	public static final int TTS_INSTALLED_CODE = TextToSpeechUtils.FLAG_CHECK_TTS_DATA;
	
	protected Context context;
	protected TextToSpeech tts;	
	
	protected OnTextToSpeechComplete listener;
	
	public TextToSpeechBase(final Context context, OnTextToSpeechComplete listener, int requestCode, int resultCode, Intent data){
		this.context 	= context;
		this.listener	= listener;
		
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
		
		if(requestCode == TTS_INSTALLED_CODE){
			if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){								
				if(availableEngines.isEmpty())
					tts = new TextToSpeech(context, this);
				else 
					tts = new TextToSpeech(context, this, availableEngines.get(0));
			} else {
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				installIntent.setPackage(availableEngines.get(0)/*replace with the package name of the target TTS engine*/);
			 
				try {
			        Log.v("TTS", "Installing voice data: " + installIntent.toUri(0));
			        context.startActivity(installIntent);
			    } catch (ActivityNotFoundException ex) {
			        Log.e("TTS", "Failed to install TTS data, no acitivty found for " + installIntent + ")");
			    }
			}
		}
	}
	
	@Override
	public void onInit(final int status) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if(status == TextToSpeech.SUCCESS) {
					int result = TextToSpeech.LANG_MISSING_DATA;
					result = tts.setLanguage(Locale.UK);
					
					if(result == TextToSpeech.LANG_MISSING_DATA ||
							result == TextToSpeech.LANG_NOT_SUPPORTED) {
						Toast.makeText(context, "Initialization failed", Toast.LENGTH_SHORT).show();
						Log.e("TTS", "TTS:onInit: Language not supported");
					}
				} else {
					Toast.makeText(context, "Initialization failed", Toast.LENGTH_SHORT).show();
					Log.e("TTS", "TTS:onInit: Initialization failed");
				}
				
				tts.setPitch(1.0f);
				tts.setSpeechRate(1.0f);
				
				listener.onTextToSpeechInitialized();	
			}
		}).start();
		
	}
	
	public void speak(String text){
		if(text==null || text.isEmpty()){
			Log.e("TTS", "TTS: Speak: No data to speak");
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
		int result = TextToSpeech.LANG_MISSING_DATA;
		result = tts.setLanguage(language);
		
		if(result == TextToSpeech.LANG_MISSING_DATA ||
				result == TextToSpeech.LANG_NOT_SUPPORTED) {
			Toast.makeText(context, "Setting language failed", Toast.LENGTH_SHORT).show();
			Log.e("TTS", "TTS:setLanguage: Language not supported");
		}
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
