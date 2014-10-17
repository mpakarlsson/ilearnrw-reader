package com.example.reader.utils.groups;

import java.io.InputStream;
import java.util.ArrayList;

import android.content.SharedPreferences;

import com.example.reader.types.PresentationRulesAdapter;
import ilearnrw.user.profile.UserProfile;

public class GroupedRulesFacade {
	private ProblemGroups problemGroups;
	private PresentationRulesAdapter presentationRulesAdapter;
	public GroupedRulesFacade(ProblemGroups problemGroups,
			PresentationRulesAdapter presentationRulesAdapter) {
		this.problemGroups = problemGroups;
		this.presentationRulesAdapter = presentationRulesAdapter;
	}

	public GroupedRulesFacade(UserProfile profile, int userId, SharedPreferences preferences, InputStream is) {
		problemGroups = new ProblemGroupsFactory().getLanguageGroups(profile.getLanguage(), is);
		presentationRulesAdapter = new PresentationRulesAdapter(profile, userId, preferences);
	}

	public ProblemGroups getProblemGroups() {
		return problemGroups;
	}

	public void setProblemGroups(ProblemGroups problemGroups) {
		this.problemGroups = problemGroups;
	}

	public PresentationRulesAdapter getPresentationRulesAdapter() {
		return presentationRulesAdapter;
	}

	public void setPresentationRulesModule(
			PresentationRulesAdapter presentationRulesAdapter) {
		this.presentationRulesAdapter = presentationRulesAdapter;
	}
	
	public int getSubgroupEnabledItems(int group, int subgroup){
		int cnt = 0;
		Group g = problemGroups.getGroupedProblems().get(group);
		for (AnnotationItem ai : g.getSubgroups().get(subgroup).getItems()){
			cnt += presentationRulesAdapter.getActivated(ai.getCategory(), ai.getIndex())?1:0;
		}
		return cnt;
	}
	
	public boolean allEnabled(int group, int subgroup){
		int activated = 0, all = 0;
		Group g = problemGroups.getGroupedProblems().get(group);
		for (AnnotationItem ai : g.getSubgroups().get(subgroup).getItems()){
			activated += presentationRulesAdapter.getActivated(ai.getCategory(), ai.getIndex())?1:0;
			all++;
		}
		return activated == all;
	}
	
	public void enableAll(int group, int subgroup){
		Group g = problemGroups.getGroupedProblems().get(group);
		for (AnnotationItem ai : g.getSubgroups().get(subgroup).getItems()){
			presentationRulesAdapter.setActivated(ai.getCategory(), ai.getIndex(), true);
		}
	}
	
	public void disableAll(int group, int subgroup){
		Group g = problemGroups.getGroupedProblems().get(group);
		for (AnnotationItem ai : g.getSubgroups().get(subgroup).getItems()){
			presentationRulesAdapter.setActivated(ai.getCategory(), ai.getIndex(), false);
		}
	}
	
	public void setColour(int group, int subgroup, int colour){
		Group g = problemGroups.getGroupedProblems().get(group);
		for (AnnotationItem ai : g.getSubgroups().get(subgroup).getItems()){
			presentationRulesAdapter.setHighlightingColor(ai.getCategory(), ai.getIndex(), colour);
		}
	}
	
	public int getGroupEnabledItems(int group){
		int cnt = 0;
		for (int i=0; i<problemGroups.getGroupedProblems().get(group).getSubgroups().size(); i++){
			cnt += getSubgroupEnabledItems(group, i);
		}
		return cnt;
	}

	public ArrayList<Group> getGroupedProblems() {
		return problemGroups.getGroupedProblems();
	}
	
}
