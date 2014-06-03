package com.example.reader.utils;

import com.example.reader.types.Pair;

public class Helper {
	
	
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
	
}