package com.ilearnrw.reader.results;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
import com.google.gson.annotations.SerializedName;

public class TokenResult {

	@SerializedName("auth")
	public String authToken;
	
	@SerializedName("refresh")
	public String refreshToken;
}
