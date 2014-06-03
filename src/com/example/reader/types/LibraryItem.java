package com.example.reader.types;

import java.io.File;

public class LibraryItem {

	private String name;
	private File file;
	
	public LibraryItem(){
	}
	
	public LibraryItem(String name, File file){
		setName(name);
		setFile(file);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}	
}
