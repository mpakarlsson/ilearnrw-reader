package com.ilearnrw.reader.types;

public class Preset {
	private String text, textColor, backgroundColor;
	
	public Preset(String text, String textColor, String backgroundColor){
		this.text 				= text;
		this.textColor 			= textColor;
		this.backgroundColor 	= backgroundColor;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTextColor() {
		return textColor;
	}

	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
}
