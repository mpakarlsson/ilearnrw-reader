package com.example.reader.utils.groups;

import java.io.InputStream;
import java.util.ArrayList;

import android.content.SharedPreferences;

import com.example.reader.types.PresentationRulesAdapter;

import ilearnrw.textadaptation.PresentationRulesModule;
import ilearnrw.textadaptation.Rule;
import ilearnrw.user.problems.ProblemDefinition;
import ilearnrw.user.problems.ProblemDefinitionIndex;
import ilearnrw.user.problems.ProblemDescription;
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
	
	public void setPresentationStyle(int group, int subgroup, int style){
		Group g = problemGroups.getGroupedProblems().get(group);
		for (AnnotationItem ai : g.getSubgroups().get(subgroup).getItems()){
			presentationRulesAdapter.setPresentationStyle(ai.getCategory(), ai.getIndex(), style);
		}
	}
	
	public int getPresentationStyle(int group, int subgroup){
		Group g = problemGroups.getGroupedProblems().get(group);
		AnnotationItem a = g.getSubgroups().get(subgroup).getItems().get(0);
		int ps = presentationRulesAdapter.getPresentationStyle(a.getCategory(), a.getIndex());
		for (AnnotationItem ai : g.getSubgroups().get(subgroup).getItems()){
			if (presentationRulesAdapter.getPresentationStyle(ai.getCategory(), ai.getIndex()) != ps)
				return 3;
		}
		return ps;
	}
	
	private int getSubgroupTopScore(Subgroup subgroup){
		int sum = 0;
		ProblemDefinition probs[] = presentationRulesAdapter.getPresentationRules().
				getUserProfile().getUserProblems().getProblems().getProblemsIndex();
		for (AnnotationItem ai : subgroup.getItems()){
			if (probs[ai.getCategory()].getSeverityType().equalsIgnoreCase("binary"))
				sum++;
			else 
				sum +=3;
		}
		return sum;
	}
	
	private int getSubgroupScore(Subgroup subgroup){
		int sum = 0;
		int sev[][] = presentationRulesAdapter.getPresentationRules().
				getUserProfile().getUserProblems().getUserSeverities().getSeverities();
		for (AnnotationItem ai : subgroup.getItems()){
			sum += sev[ai.getCategory()][ai.getIndex()];
		}
		return sum;
	}
	
	public int[][] getSuggestedSubgroups(){
		double max1 = -1, max2 = -1;
		int group1 = -1, subgroup1 = -1, group2 = -1, subgroup2 = -1;
		for (int i=0; i<problemGroups.getGroupedProblems().size(); i++){
			for (int j=0; j<problemGroups.getGroupedProblems().get(i).getSubgroups().size(); j++){
				Subgroup s = problemGroups.getGroupedProblems().get(i).getSubgroups().get(j);
				double val = (double)getSubgroupScore(s)/getSubgroupTopScore(s);
				if (val>max1){
					max2 = max1;
					group2 = group1;
					subgroup2 = subgroup1;
					max1 = val;
					group1 = i;
					subgroup1 = j;
				}
				else if (val>max2){
					max2 = val;
					group2 = i;
					subgroup2 = j;
				}
			}
		}
		if (group1 >=0 && group2 >=0){
			int p[][] = {{group1, subgroup1}, {group2, subgroup2}};
			return p;
		}
		if (group1 >=0){
			int p[][] = {{group1, subgroup1}};
			return p;
		}
		return null;
	}
	
	public int getGroupEnabledItems(int group){
		int cnt = 0;
		for (int i=0; i<problemGroups.getGroupedProblems().get(group).getSubgroups().size(); i++){
			cnt += getSubgroupEnabledItems(group, i);
		}
		return cnt;
	}
	
	public int getTotalNumberOfActiveRules(){
		Rule rules[][] = presentationRulesAdapter.getPresentationRules().getRulesTable();
		int cnt = 0;
		for (int i=0; i<rules.length; i++){
			for (int j=0;j<rules[i].length; j++){
				if (rules[i][j].getActivated())
					cnt++;
			}
		}
		return cnt;
	}

	public ArrayList<Group> getGroupedProblems() {
		return problemGroups.getGroupedProblems();
	}
	
}
