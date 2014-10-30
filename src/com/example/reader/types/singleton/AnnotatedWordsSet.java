package com.example.reader.types.singleton;

import ilearnrw.annotation.UserBasedAnnotatedWordsSet;
import android.content.Context;

import com.google.gson.Gson;

public class AnnotatedWordsSet {

	private static AnnotatedWordsSet aws;
	private static UserBasedAnnotatedWordsSet ubaws;
	private static String json;
	private static Context context;
	
	private AnnotatedWordsSet(){
	}
	
	public static AnnotatedWordsSet getInstance(Context c, String _json){
		if(aws == null)
			aws = new AnnotatedWordsSet();
		
		if(context == null)
			context = c;
		
		json = _json;
		
		if(json==null || json.isEmpty())
			return null;
		
		if(ubaws==null)
			ubaws = new Gson().fromJson(json, UserBasedAnnotatedWordsSet.class);
		
		return aws;
	}
	
	public UserBasedAnnotatedWordsSet getUserBasedAnnotatedWordsSet(){
		return ubaws;
	}	
}
