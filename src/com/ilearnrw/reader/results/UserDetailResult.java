package com.ilearnrw.reader.results;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
import com.google.gson.annotations.SerializedName;

public class UserDetailResult {

	@SerializedName("id")
	public Integer id;
	
	@SerializedName("username")
	public String username;
	
	@SerializedName("language")
	public String language;	
}
