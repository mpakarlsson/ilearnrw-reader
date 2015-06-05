package com.ilearnrw.reader.utils.groups;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
import java.util.ArrayList;



public class Group {
	private ArrayList<Subgroup> subgroups;
	private String groupTitle;

	public Group(ArrayList<Subgroup> subgroups, String groupTitle) {
		this.subgroups = subgroups;
		this.groupTitle = groupTitle;
	}
	
	public Group(String groupTitle) {
		this.subgroups = new ArrayList<Subgroup>();
		this.groupTitle = groupTitle;
	}

	public ArrayList<Subgroup> getSubgroups() {
		return subgroups;
	}

	public void setSubgroups(ArrayList<Subgroup> subgroups) {
		this.subgroups = subgroups;
	}

	public String getGroupTitle() {
		return groupTitle;
	}

	public void setGroupTitle(String groupTitle) {
		this.groupTitle = groupTitle;
	}
	
}
