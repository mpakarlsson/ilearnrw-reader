package com.example.reader.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {

	
	public static boolean WriteToFile(InputStream is, String fileName, File directory){
		
		OutputStream os;
		try {
			os = new FileOutputStream(new File(directory +"/" + fileName));
			
			int read = 0;
			byte[] bytes = new byte[1024];
			while((read = is.read(bytes)) != -1)
				os.write(bytes, 0 ,read);
			
			os.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static List<File> getFileList(File parent){
		List<File> dirFiles = new ArrayList<File>();
		File[] files = parent.listFiles();
		
		for( File file : files){
			if(file.isDirectory()){
				dirFiles = getFileList(file);
			} else {
				if(file.getName().endsWith(".html") || file.getName().endsWith(".txt")){
					dirFiles.add(file);
				}
			}
		}
		return dirFiles; 
	}
}
