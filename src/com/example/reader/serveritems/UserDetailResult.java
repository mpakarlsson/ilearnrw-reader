package com.example.reader.serveritems;

import com.google.gson.annotations.SerializedName;

public class UserDetailResult {

	@SerializedName("id")
	public Integer id;
	
	@SerializedName("username")
	public String username;
	
	@SerializedName("language")
	public String language;	
}
