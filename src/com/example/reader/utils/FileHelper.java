package com.example.reader.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;

public class FileHelper {
	public static boolean WriteFileToDirectory(InputStream is, String fileName, File directory){
		OutputStream os;
		try {
			os = new FileOutputStream(new File(directory, fileName));
			
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
	
	public static void copy(File src, File dst) throws IOException {
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);

	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	}
	
	public static String InputStreamToString(InputStream is){
		try {
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			
			return new String(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static boolean copyFileToExternalStorage(File file, String fileName, String folderName){
		if(!isExternalStorageWritable())
			return false;
		
		File folder = getOrCreateExternalDirectory(folderName);
		File resultFile = new File(folder, fileName);
		
		try {
			copy(file, resultFile);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	
	public static boolean isExternalStorageWritable(){
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) ||
		        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
			return true;
		
		return false;
	}

	
	public static File getOrCreateExternalDirectory(String folderName) {
	    File folder = Environment.getExternalStorageDirectory();
	    if( folder == null || !folder.isDirectory() )
	        return null;
	    File dataDir = new File(folder, folderName);
	    if( !confirmDir(dataDir) )
	        return null;
	    return dataDir;
	}   

	private static boolean confirmDir(File dir) {
	    if (dir.isDirectory()) return true;
	    if (dir.exists()) return false;
	    return dir.mkdirs();
	}   
	
	public static String fromStream(InputStream in) throws IOException
	{
	    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	    StringBuilder out = new StringBuilder();
	    String newLine = System.getProperty("line.separator");
	    String line;
	    while ((line = reader.readLine()) != null) {
	        out.append(line);
	        out.append(newLine);
	    }
	    return out.toString();
	}
	
}
