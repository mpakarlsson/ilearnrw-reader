package com.example.reader.utils;

import android.content.Context;
import android.graphics.Color;

import com.example.reader.types.Pair;

public class Helper {

	
	public static Pair<String> splitFileName(String name){
		String fileName = name;
		int index = fileName.lastIndexOf(".");
		
		if(index==-1)
			return null;
		
		String ext = fileName.substring(index);
		fileName = fileName.substring(0, index);
		
		return new Pair<String>(fileName, ext);
	}
	
	public static String findIdentifier(String word){
		for(int j=0; j<word.length();j++){
			if(Character.isDigit(word.charAt(j)))
				return word.substring(0, j);
		}
		
		return "";
	}
	
	public static int findPosition(String word){
		for(int j=0; j<word.length();j++){
			if(Character.isDigit(word.charAt(j)))
				return Integer.parseInt(word.substring(j));
		}
		
		return -1;
	}
	
	public static int convertDpToPx(float dp, Context c){
		return (int) ((dp * c.getResources().getDisplayMetrics().density) + 0.5f);
	}
	
	public static int convertPxToDp(float px, Context c){
		return (int) ((px / c.getResources().getDisplayMetrics().density) + 0.5f);
	}
	
	public static int lightenColor(int color, float factor){
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = (color >> 0) & 0xFF;
		
		float[] colors = new float[3];

		Color.colorToHSV(color, colors);
		if(r == 0 && g == 0 && b == 0)
			colors[2] = 0.1f;
		
		colors[2] = colors[2] + colors[2]*factor;
		if(colors[2]>1.0f) colors[2]=1.0f;
		
		color = Color.HSVToColor(colors);
		return color;
	}
	
	public static int darkenColor(int color, float factor){
		float[] colors = new float[3];
		Color.colorToHSV(color, colors);
		
		colors[2] = colors[2]*factor;
		
		color = Color.HSVToColor(colors);
		return color;
	}
	
}