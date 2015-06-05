package com.ilearnrw.reader.texttospeech;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;

public class TextToSpeechUtils {

	public final static int FLAG_CHECK_TTS_DATA = 10003;
	private static final String NEW_LINE = "\n";
	
	public String checkBestVoiceMatch(Locale loc, List<String> possibleVoices){
		if(possibleVoices.isEmpty())
			return null;
		
		String bestMatch = null;
		String countryToMatch = loc.getISO3Country();
		
		for(String possibleVoice : possibleVoices){
			if(possibleVoice.toLowerCase(Locale.getDefault()).contains(countryToMatch))
				bestMatch = possibleVoice;
		}
		
		if(bestMatch.contains("eng")){
			if(loc.getCountry().equals("US"))
				bestMatch = "eng-USA";
			else
				bestMatch = "eng-GBR";			
		}
		
		return bestMatch;
	}
	
	
	public static List<Locale> getSupportedLocales(Context context, TextToSpeech tts){
		List<Locale> supportedLocales = new ArrayList<Locale>();
		for(Locale loc : Locale.getAvailableLocales()){
			if(isLanguageAvailable(loc, tts))
				supportedLocales.add(loc);
		}
		return supportedLocales;
	}
	
	public static boolean isLanguageAvailable(Locale language, TextToSpeech tts){		
		boolean available = false;
		switch(tts.isLanguageAvailable(language))
		{
			case TextToSpeech.LANG_AVAILABLE:
			case TextToSpeech.LANG_COUNTRY_AVAILABLE:
			case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE:
				available = true;
				break;
				
			case TextToSpeech.LANG_MISSING_DATA:
			case TextToSpeech.LANG_NOT_SUPPORTED:
				available = false;
				break;
		}
		return available;
	}
	
	public static void installLanguageData(Context context){
		Intent installIntent = new Intent();
		installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
		context.startActivity(installIntent);
	}
	
	public static void checkLanguageData(Activity activity){
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		activity.startActivityForResult(checkIntent, FLAG_CHECK_TTS_DATA);
	}
	
	public static HashMap<String, String> makeParamsWith(String value){
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, value);
		return params;
	}
	
	public static String getLanguageAvailableDescription(TextToSpeech tts){
		StringBuilder sb = new StringBuilder();
		
		for(Locale loc : Locale.getAvailableLocales()){
			int availableCheck = tts.isLanguageAvailable(loc);
			sb.append(loc.toString()).append(" ");
			switch (availableCheck) {
			case TextToSpeech.LANG_AVAILABLE:
				break;
			case TextToSpeech.LANG_COUNTRY_AVAILABLE:
				sb.append("COUNTRY_AVAILABLE");
				break;
			case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE:
				sb.append("COUNTRY_VARIANT_AVAILABLE");
				break;
			case TextToSpeech.LANG_MISSING_DATA:
				sb.append("MISSING_DATA");
				break;
			case TextToSpeech.LANG_NOT_SUPPORTED:
				sb.append("NOT_SUPPORTED");
				break;
			}
			sb.append(NEW_LINE);
		}
		return sb.toString();
	}	
}
