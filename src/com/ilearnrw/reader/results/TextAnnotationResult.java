package com.ilearnrw.reader.results;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
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
