package com.ilearnrw.reader.types;

public class ExplorerItem {

	private String item;
	private String path;
	private FileType type;
	private long size;
	
	public static enum FileType{
		Unknown("Unknown", 0),
		Directory("Directory", 1),
		File("File", 2);
		
		private String name;
		private int value;		
		private FileType(String name, int value){
			this.name 	= name;
			this.value 	= value;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public int getValue(){
			return value;
		}
		
		public String getName(){
			return name;
		}
	}
	
	public ExplorerItem(){
		setItem("");
		setPath("");
		setType(FileType.Unknown);
		setSize(0);
	}
	
	public ExplorerItem(String item, String path, long size, FileType type){
		setItem(item);
		setPath(path);
		setType(type);
		setSize(size);
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

	public FileType getType() {
		return type;
	}

	public void setType(FileType type) {
		this.type = type;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
	
	
}
