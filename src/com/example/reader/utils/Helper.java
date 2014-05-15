package com.example.reader.utils;

import android.text.Html;

public class Helper {

	public static String getHtmlString(String text, String startId){
		int index = text.indexOf(startId);
		
		String part = text.substring(0, index);
		part = part.substring(part.lastIndexOf("<"));
		String temp = part + text.substring(index);		
		
		return Html.fromHtml(temp).toString();
	}
	
	
	public static String nextInt(String current){
		int num = Integer.parseInt(current);
		return Integer.toString(++num);
	}
	
	public static String previousInt(String current){
		int num = Integer.parseInt(current);
		
		if(num == 0)
			return current;
		
		return Integer.toString(--num);
	}
	
}