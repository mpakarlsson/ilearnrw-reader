package com.ilearnrw.reader.utils.groups;


public class AnnotationItem {
	private int category, index;
	private String defaultColourHEX;

	public AnnotationItem(int category, int index, String defaultColourHEX) {
		this.category = category;
		this.index = index;
		this.defaultColourHEX = defaultColourHEX;
	}

	public AnnotationItem() {
		this.category = -1;
		this.index = -1;
		this.defaultColourHEX = "6666FF";
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getDefaultColourHEX() {
		return defaultColourHEX;
	}

	public void setDefaultColour(String defaultColourHEX) {
		this.defaultColourHEX = defaultColourHEX;
	}
	
}
