package com.example.reader.serveritems;

import com.google.gson.annotations.SerializedName;

public class LoginResult {

	@SerializedName("auth")
	public String authToken;
	
	@SerializedName("refresh")
	public String refreshToken;
	
}
