package com.ilearnrw.reader.utils.groups;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
import java.util.ArrayList;


public class Subgroup {
	private ArrayList<AnnotationItem> items;
	private String subgroupTitle;

	public Subgroup() {
	}

	public Subgroup(ArrayList<AnnotationItem> items, String subgroupTitle) {
		this.items = items;
		this.subgroupTitle = subgroupTitle;
	}
	
	public Subgroup(String subgroupTitle) {
		this.items = new ArrayList<AnnotationItem>();
		this.subgroupTitle = subgroupTitle;
	}

	public ArrayList<AnnotationItem> getItems() {
		return items;
	}

	public void setItems(ArrayList<AnnotationItem> items) {
		this.items = items;
	}

	public String getSubgroupTitle() {
		return subgroupTitle;
	}

	public void setSubgroupTitle(String subgroupTitle) {
		this.subgroupTitle = subgroupTitle;
	}
	
}
