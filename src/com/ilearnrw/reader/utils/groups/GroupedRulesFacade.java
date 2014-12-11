package com.ilearnrw.reader.utils.groups;

import java.io.InputStream;
import java.util.ArrayList;

import android.content.Context;

import com.ilearnrw.reader.types.adapters.PresentationRulesAdapter;

import ilearnrw.textadaptation.Rule;
import ilearnrw.user.problems.ProblemDefinition;
import ilearnrw.user.profile.UserProfile;

public class GroupedRulesFacade {
	private Groups problemGroups;
	private PresentationRulesAdapter presentationRulesAdapter;
	public GroupedRulesFacade(Groups problemGroups,
			PresentationRulesAdapter presentationRulesAdapter) {
		this.problemGroups = problemGroups;
		this.presentationRulesAdapter = presentationRulesAdapter;
	}

	public GroupedRulesFacade(Context context, UserProfile profile, int userId, InputStream is) {
		problemGroups = new ProblemGroupsFactory().getLanguageGroups(profile.getLanguage(), is);
		presentationRulesAdapter = new PresentationRulesAdapter(context, profile, userId);
	}

	public Groups getProblemGroups() {
		return problemGroups;
	}

	public void setProblemGroups(Groups problemGroups) {
		this.problemGroups = problemGroups;
	}

	public PresentationRulesAdapter getPresentationRulesAdapter() {
		return presentationRulesAdapter;
	}

	public void setPresentationRulesModule(
			PresentationRulesAdapter presentationRulesAdapter) {
		this.presentationRulesAdapter = presentationRulesAdapter;
	}
	
	public int getNumSubgroupEnabledItems(int group, int subgroup){
		int cnt = 0;
		Subgroup sg = problemGroups.getGroupedProblems().get(group).getSubgroups().get(subgroup);
		for (AnnotationItem ai : sg.getItems()){
			cnt += presentationRulesAdapter.getActivated(ai.getCategory(), ai.getIndex())?1:0;
		}
		return cnt;
	}
	
	public boolean allEnabled(int group, int subgroup){
		Subgroup sg = problemGroups.getGroupedProblems().get(group).getSubgroups().get(subgroup);
		for (AnnotationItem ai : sg.getItems()){
			if(!presentationRulesAdapter.getActivated(ai.getCategory(), ai.getIndex()))
				return false;
		}
		return true;
	}
	
	public void enableAll(int group, int subgroup){
		Subgroup sg = problemGroups.getGroupedProblems().get(group).getSubgroups().get(subgroup);
		for (AnnotationItem ai : sg.getItems()){
			presentationRulesAdapter.setActivated(ai.getCategory(), ai.getIndex(), true);
		}
	}
	
	public void disableAll(int group, int subgroup){
		Subgroup sg = problemGroups.getGroupedProblems().get(group).getSubgroups().get(subgroup);
		for (AnnotationItem ai : sg.getItems()){
			presentationRulesAdapter.setActivated(ai.getCategory(), ai.getIndex(), false);
		}
	}
	
	public void setColour(int group, int subgroup, int colour){
		Subgroup sg = problemGroups.getGroupedProblems().get(group).getSubgroups().get(subgroup);
		for (AnnotationItem ai : sg.getItems()){
			presentationRulesAdapter.setHighlightingColor(ai.getCategory(), ai.getIndex(), colour);
		}
	}
	
	public void setPresentationStyle(int group, int subgroup, int style){
		Subgroup sg = problemGroups.getGroupedProblems().get(group).getSubgroups().get(subgroup);
		for (AnnotationItem ai : sg.getItems()){
			presentationRulesAdapter.setPresentationStyle(ai.getCategory(), ai.getIndex(), style);
		}
	}
	
	public int getPresentationStyle(int group, int subgroup){
		Subgroup sg = problemGroups.getGroupedProblems().get(group).getSubgroups().get(subgroup);
		int ps = -1;
		for (AnnotationItem ai : sg.getItems()){
			boolean activated = presentationRulesAdapter.getActivated(ai.getCategory(), ai.getIndex()); 
			if (activated && ps == -1)
				ps = presentationRulesAdapter.getPresentationStyle(ai.getCategory(), ai.getIndex());
			else if (activated && presentationRulesAdapter.getPresentationStyle(ai.getCategory(), ai.getIndex()) != ps)
				return 1;
		}
		return ps == -1 ? 1 : ps;
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
			Group g =  problemGroups.getGroupedProblems().get(i);
			for (int j=0; j<g.getSubgroups().size(); j++){
				Subgroup s = g.getSubgroups().get(j);
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
			cnt += getNumSubgroupEnabledItems(group, i);
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
	
	public int getTotalNumberOfActiveColours(){
		Rule rules[][] = presentationRulesAdapter.getPresentationRules().getRulesTable();
		ArrayList<Integer> colours = new ArrayList<Integer>();
		for (int i=0; i<rules.length; i++){
			for (int j=0;j<rules[i].length; j++){
				if (rules[i][j].getActivated() && !colours.contains(rules[i][j].getHighlightingColor()))
					colours.add(rules[i][j].getHighlightingColor());
			}
		}
		return colours.size();
	}

	public ArrayList<Group> getGroupedProblems() {
		return problemGroups.getGroupedProblems();
	}
	
}
