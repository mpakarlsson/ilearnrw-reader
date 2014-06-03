package com.example.reader.types;

public class ExplorerItem {

	private String item;
	private String path;
	
	public ExplorerItem(){
		setItem("");
		setPath("");
	}
	
	public ExplorerItem(String item, String path){
		setItem(item);
		setPath(path);
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
}
