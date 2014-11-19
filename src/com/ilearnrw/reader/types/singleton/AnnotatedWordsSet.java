package com.ilearnrw.reader.types.singleton;

import ilearnrw.annotation.UserBasedAnnotatedWordsSet;
import android.content.Context;

import com.google.gson.Gson;

public class AnnotatedWordsSet {

	private static AnnotatedWordsSet aws;
	private static UserBasedAnnotatedWordsSet ubaws;
	private static String json;
	private static Context context;
	private static String bookName;
	
	private AnnotatedWordsSet(){
	}
	
	
	/**
	 * Make sure that you initUserBasedAnnotatedWordsSet() first time you run this method
	 */
	public static AnnotatedWordsSet getInstance(Context c){
		if(aws == null)
			aws = new AnnotatedWordsSet();
		
		if(context == null)
			context = c;
		
		return aws;
	}
	
	public UserBasedAnnotatedWordsSet getUserBasedAnnotatedWordsSet(){
		return ubaws;
	}
	
	public void initUserBasedAnnotatedWordsSet(String json, String bookName){
		String name = getBookName();
		if(name != null && !name.isEmpty()){
			if(!bookName.equals(name)){
				Context saved = context;
				AnnotatedWordsSet.getInstance(context).nullAnnotatedWordsSet();
				AnnotatedWordsSet.getInstance(saved);
			}
		}
		
		setJson(json);
		setBookName(bookName);
		
		if(json == null || json.isEmpty())
			return;
		
		if(ubaws == null)
			ubaws = new Gson().fromJson(json, UserBasedAnnotatedWordsSet.class);
	}
	
	private void setBookName(String name){
		bookName = name;
	}
	
	public String getBookName(){
		return bookName;
	}
	
	private void setJson(String j){
		json = j;
	}
	
	public String getJson(){
		return json;
	}
	
	public void nullAnnotatedWordsSet(){
		aws = null;
		ubaws = null;
		json = null;
		context = null;
		bookName = null;
	}
}
