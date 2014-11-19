package com.ilearnrw.reader.results;

import com.google.gson.annotations.SerializedName;

public class TokenResult {

	@SerializedName("auth")
	public String authToken;
	
	@SerializedName("refresh")
	public String refreshToken;
}
