package com.example.reader.utils.groups;
import java.util.ArrayList;



public class UKGroups implements ProblemGroups {
	private ArrayList<Group> groups;
	
	public UKGroups(ArrayList<Group> groups) {
		this.groups = groups;
	}
	public ArrayList<Group> getGroups() {
		return groups;
	}
	public void setGroups(ArrayList<Group> groups) {
		this.groups = groups;
	}

	@Override
	public ArrayList<Group> getGroupedProblems() {
		return groups;
	}
}
