package com.ilearnrw.reader.types;

import com.ilearnrw.reader.utils.Helper;

public class ColorOption {

	private String name, hex, hexDefault;
	private int color, colorDefault;

	public ColorOption(String name, String hexColor, String hexDefaultColor){
		this.name = name;

		this.hex = Helper.fixHex(hexColor);
		this.hexDefault = Helper.fixHex(hexDefaultColor);
		
		color = Helper.hexToColor(hex);
		colorDefault = Helper.hexToColor(hexDefaultColor);
	}
	
	public ColorOption(String name, int color, int colorDefault){
		this.name = name;
		this.color = color;
		this.colorDefault = colorDefault;
		
		hex = Helper.colorToHex(color);
		hexDefault = Helper.colorToHex(colorDefault);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHex(boolean withAlpha) {
		if(withAlpha)
			return hex;
		else {
			
			
			return hex;
		}
	}

	public void setHex(String hex) {
		this.hex = hex;
	}

	public String getHexDefault() {
		return hexDefault;
	}

	public void setHexDefault(String hexDefault) {
		this.hexDefault = hexDefault;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getColorDefault() {
		return colorDefault;
	}

	public void setColorDefault(int colorDefault) {
		this.colorDefault = colorDefault;
	}
	
}
