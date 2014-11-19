package com.ilearnrw.reader.types.singleton;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ilearnrw.reader.R;
import com.google.gson.Gson;

import ilearnrw.user.profile.UserProfile;

public class ProfileUser{
	
	private static ProfileUser profile;
	private static UserProfile profileData;
	private static String json;
	private static Context context;
	private static SharedPreferences sp;
	
	private ProfileUser(){
	}
	
	public static ProfileUser getInstance(Context c){
		if(profile == null)
			profile = new ProfileUser();
		
		if(context == null)
			context = c;
		
		if(sp == null)
			sp = PreferenceManager.getDefaultSharedPreferences(context);
		
		json = sp.getString(c.getString(R.string.sp_user_profile_json), "");
		
		if(json.isEmpty())
			return null;
		
		if(profileData==null)
			profileData = new Gson().fromJson(json, UserProfile.class);
		
		return profile;
	}
	
	public UserProfile getProfile(){
		return profileData;
	}
	
	public void nullProfile(){
		profile = null;
		profileData = null;
		json = null;
		context = null;
		sp = null;
	}
}
