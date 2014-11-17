package com.example.reader.utils.groups;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.google.gson.Gson;

import ilearnrw.utils.LanguageCode;

public class ProblemGroupsFactory {
	public Groups getLanguageGroups(LanguageCode lc, InputStream is){
		try {
			return load(is);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Groups load(InputStream is) throws UnsupportedEncodingException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		Groups obj = new Gson().fromJson(reader, Groups.class);
		return obj;
	}
}
