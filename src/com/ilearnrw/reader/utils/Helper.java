package com.ilearnrw.reader.utils;

import java.security.InvalidParameterException;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.util.Log;

import com.ilearnrw.reader.types.Pair;

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
	
	public static boolean endOfLineCharacter(char c){
		if(c=='.' || c=='!' || c=='?' || c=='\n' || c=='\r')
			return true;
		
		return false;
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
	
	public static SpannableStringBuilder colorString(CharSequence cSeq, CharacterStyle style, int startPos, int endPos){
		SpannableStringBuilder builder = new SpannableStringBuilder(cSeq);
		builder.setSpan(style, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return builder;
	}
	
	public static SpannableStringBuilder colorString(SpannableStringBuilder builder, CharacterStyle style, int startPos, int endPos){
		builder.setSpan(style, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return builder;
	}
	
	public static String colorToHex(int color){
		return "#" + Integer.toHexString(color);
	}
	
	public static int hexToColor(String hex){
		if(hex.startsWith("#"))
			hex = hex.substring(1);
		
		if(hex.length()==8){
			hex = hex.substring(2);
			return Integer.parseInt(hex, 16) + 0xFF000000;
		} else 
			return Integer.parseInt(hex, 16) + 0xFF000000;
	}
	
	public static String fixHex(String hex){
		if(hex.length()!=9){
			StringBuilder builder;
			if(hex.startsWith("#"))
				builder = new StringBuilder(hex.substring(1));
			else
				builder = new StringBuilder(hex);
			
			switch (builder.length()) {
			case 3:
				int cnt = 0;
				for(char c : hex.toCharArray()){
					builder.insert(++cnt, c);
					cnt++;
				}
				break;

			case 6:
				builder.insert(0, "FF");
				break;
				
				
			case 8: 
				break;
			}
			
			builder.insert(0, "#");
			hex = builder.toString();
		} 
		
		if(hex.length()==9){
			if(!hex.startsWith("#"))
				throw new InvalidParameterException("Invalid hex, missing character #");
			int cnt = 0;
			for(char c : hex.toCharArray()){
				if("0123456789abcdefABCDEF".indexOf(c)==-1){
					if(cnt++==0 && c=='#')
						continue;
					
					throw new InvalidParameterException("Invalid hex, invalid character");
				}
				
				cnt++;
			}
		}
		
		return hex;
		
	}
	
	public static void logBundle(Bundle bundle){
		if(bundle==null){
			Log.e("Bundle", "The bundle is null");
			return;
		}
		for (String key : bundle.keySet()) {
		    Object value = bundle.get(key);
		    Log.d("Bundle", String.format("%s %s (%s)", key,  
		        value.toString(), value.getClass().getName()));
		}
	}
	
	public static boolean isPackageInstalled(PackageManager pm, String packageName) {
        try {
            pm.getPackageInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            return false;
        }
        return true;
	}
	
	public static StringBuilder removeSpans(StringBuilder builder){
		int openIndex = builder.indexOf("<");
		int closeIndex = builder.indexOf(">");
		
		if(openIndex == -1 || closeIndex == -1)
			return builder;
		
		if(closeIndex <= openIndex)
			return builder;
		
		builder = builder.delete(openIndex, closeIndex+1);
		builder = removeSpans(builder);
		return builder;
	}
	
	public static StringBuilder removeSubstring(StringBuilder builder, String str){
		int index = builder.indexOf(str);
		
		if(index == -1)
			return builder;
		
		return builder = builder.delete(index, index+str.length());
	}
}