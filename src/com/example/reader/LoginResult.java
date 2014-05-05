package com.example.reader;

import com.google.gson.annotations.SerializedName;

public class LoginResult {

	@SerializedName("auth")
	public String authToken;
	
	@SerializedName("refresh")
	public String refreshToken;
	
}
