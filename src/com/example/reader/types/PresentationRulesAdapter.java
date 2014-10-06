package com.example.reader.types;

import android.content.SharedPreferences;
import ilearnrw.textadaptation.PresentationRulesModule;
import ilearnrw.user.profile.UserProfile;


public class PresentationRulesAdapter {
	private PresentationRulesModule presentationRules;
	private SharedPreferences preferences;
	private int userId;

	private final int DEFAULT_COLOR = 0xffff0000;
	private final int DEFAULT_RULE	= 3;

	public PresentationRulesAdapter(UserProfile profile, int userId, SharedPreferences preferences) {
		this.presentationRules = new PresentationRulesModule(profile);
		this.userId = userId;
		this.preferences = preferences;
		loadModule();
	}
	
	private void loadModule(){
		for (int i=0;i<presentationRules.getRulesTable().length; i++){
			for (int j=0;j<presentationRules.getRulesTable()[i].length; j++){
				presentationRules.getRulesTable()[i][j].setActivated(preferences.getBoolean(userId+"pm_enabled_"+i+"_"+j, false));
				presentationRules.getRulesTable()[i][j].setHighlightingColor(preferences.getInt(userId+"pm_color_"+i+"_"+j, DEFAULT_COLOR));
				presentationRules.getRulesTable()[i][j].setPresentationStyle(preferences.getInt(userId+"pm_rule_"+i+"_"+j, DEFAULT_RULE));
			}
		}
	}
	
	public void saveModule(){
		for (int i=0;i<presentationRules.getRulesTable().length; i++){
			for (int j=0;j<presentationRules.getRulesTable()[i].length; j++){
				preferences.edit().putBoolean(userId+"pm_enabled_" + i + "_" + j , presentationRules.getActivated(i, j)).commit();
				preferences.edit().putInt(userId+"pm_color_" + i + "_" + j, presentationRules.getHighlightingColor(i, j)).commit();
				preferences.edit().putInt(userId+"pm_rule_"+i+"_"+j, presentationRules.getPresentationRule(i, j)).commit();
        	}
		}
	}
	
	public boolean getActivated(int i, int j){
		return presentationRules.getActivated(i, j);
	}
	
	public void setActivated(int i, int j, boolean activated){
		presentationRules.getRulesTable()[i][j].setActivated(activated);
	}
	
	public int getHighlightingColor(int i, int j){
		return presentationRules.getHighlightingColor(i, j);
	}
	
	public void setHighlightingColor(int i, int j, int color){
		presentationRules.getRulesTable()[i][j].setHighlightingColor(color);
	}
	
	public int getPresentationStyle(int i, int j){
		return presentationRules.getRulesTable()[i][j].getPresentationStyle();
	}
	
	public void setPresentationStyle(int i, int j, int rule){
		presentationRules.getRulesTable()[i][j].setPresentationStyle(rule);
	}
	
	public int getTextColor(int i, int j){
		return presentationRules.getRulesTable()[i][j].getTextColor();
	}
	
	public void setTextColor(int i, int j, int textColor){
		presentationRules.getRulesTable()[i][j].setTextColor(textColor);
	}
}
