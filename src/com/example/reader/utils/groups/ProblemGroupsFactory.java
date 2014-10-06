package com.example.reader.utils.groups;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.google.gson.Gson;

import ilearnrw.utils.LanguageCode;

public class ProblemGroupsFactory {
	public ProblemGroups getLanguageGroups(LanguageCode lc, InputStream is){
		try {
			if (lc == LanguageCode.EN)
				return loadUK(is);
			return loadGR(is);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public UKGroups loadUK(InputStream is) throws UnsupportedEncodingException{
		Gson gson = new Gson();
		BufferedReader buffered = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		UKGroups obj = gson.fromJson(buffered, UKGroups.class);
		return obj;
	}
	public GreekGroups loadGR(InputStream is) throws UnsupportedEncodingException{
		Gson gson = new Gson();
		BufferedReader buffered = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		GreekGroups obj = gson.fromJson(buffered, GreekGroups.class);
		return obj;
	}
}
