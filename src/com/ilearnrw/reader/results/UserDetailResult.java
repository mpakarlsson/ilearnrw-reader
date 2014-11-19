package com.ilearnrw.reader.results;

import com.google.gson.annotations.SerializedName;

public class UserDetailResult {

	@SerializedName("id")
	public Integer id;
	
	@SerializedName("username")
	public String username;
	
	@SerializedName("language")
	public String language;	
}
