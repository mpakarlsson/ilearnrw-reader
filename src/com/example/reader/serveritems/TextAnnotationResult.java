package com.example.reader.serveritems;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class TextAnnotationResult {

	@SerializedName("html")
	public String html;
	
	
	@SerializedName("wordSet")
	public List<TextAnnotationWordSet> wordSet;
	
	
	@SerializedName("trickyWordsList")
	public List<String> trickyWordsList;
	
}
