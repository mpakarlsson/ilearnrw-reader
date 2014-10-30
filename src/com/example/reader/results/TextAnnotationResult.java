package com.example.reader.results;

import ilearnrw.annotation.UserBasedAnnotatedWordsSet;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class TextAnnotationResult {

	@SerializedName("html")
	public String html;
	
	@SerializedName("wordSet")
	public UserBasedAnnotatedWordsSet wordSet;
	
	@SerializedName("trickyWordsList")
	public List<String> trickyWordsList;
}
