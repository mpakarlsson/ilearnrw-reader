package com.ilearnrw.reader.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.ilearnrw.reader.types.Pair;

import android.content.Context;
import android.os.Environment;

public class FileHelper {
	/*public static boolean WriteFileToDirectory(InputStream is, String fileName, File directory){
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
	}*/
	
	public static void saveFile(String data, File file){
		try {
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file));
			osw.write(data);
			osw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveFile(InputStream is, File file){
		try {
			OutputStream os = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int read;
			
			while((read = is.read(buffer)) != -1)
				os.write(buffer, 0, read);
			
			os.flush();
			os.close();
			
		} catch (IOException e) {
		}
		
	}
	
	public static String readFromFile(File file){
		if(file==null)
			return null;
		
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			String line = "";
			StringBuilder sb = new StringBuilder();
			
			while((line = br.readLine()) != null){
				sb.append(line);
			}
			br.close();
			isr.close();
			return sb.toString();
			
		} catch (IOException e) {}
		
		return null;
	}
	
	public static List<File> getFileList(File parent, boolean hiddenFiles){
		List<File> dirFiles = new ArrayList<File>();
		File[] files = parent.listFiles();
		
		for( File file : files){
			if(file.isDirectory()){
				dirFiles = getFileList(file, hiddenFiles);
			} else {
				if(hiddenFiles)
					dirFiles.add(file);
				else{
					if(file.getName().endsWith(".html") || file.getName().endsWith(".txt")){
						dirFiles.add(file);
					}
				}
			}
		}
		return dirFiles; 
	}
	
	public static File getFile(String name, File parent){
		File[] files = parent.listFiles();
		
		for(File f : files){
			if(!f.isDirectory()){
				String fName = f.getName();
				if(fName.equals(name) && (fName.endsWith(".html") || fName.endsWith(".txt")))
					return f;
			}
		}
		return null;
	}
	
	public static void removeFiles(File parent){
		File[] files = parent.listFiles();
		
		for( File file : files){
			if(file.isDirectory())
				removeFiles(file);
			else {
				file.delete();
			}
		}
		
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
	
	public static String inputStreamToString(InputStream is){
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder(is.available());
			String line;
			
			while((line=br.readLine()) != null){
				sb.append(line +"\n");
			}
			
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private static final long kB = 1024;
	//private static final long MB = kB * kB;
	private static final long amount = 5;
	public static final long LIMIT_BYTES = kB * amount;
	public static String inputStreamBufferRead(FileInputStream fis){
		String result = "";
		StringBuilder builder = new StringBuilder("");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			char[] buffer = new char[50];
			int numChars = -1;
			int size = 0;
			long limit = LIMIT_BYTES;
			long hardLimit = 500 + limit;
			while((numChars = br.read(buffer)) != -1){
				char[] buff = new char[numChars];
				
				for(int i=0; i<numChars; i++)
					buff[i] = buffer[i];
				
				byte[] b = new String(buff).getBytes("UTF-8");
				size += b.length;
				
				builder.append(buff);
				if(size>limit){
					int offsetFromEnd = lastIndexOfEOLChar(buff);
					if(offsetFromEnd != -1){
						builder = builder.delete(builder.length() - offsetFromEnd, builder.length());
						result = builder.toString();
						break;
					}
					
					if(size>hardLimit){
						result = builder.substring(0, builder.lastIndexOf(" "));
						break;
					}
				}
				buffer = new char[50];
			}
			
		} catch (IOException e ){
			e.printStackTrace();
		} finally {
			if(result.isEmpty())
				result = builder.toString();
			if(fis!=null){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return result;
	}
	
	private static int lastIndexOfEOLChar(char[] buff){
		for(int i = buff.length-1; i>0; i--){
			if(Helper.endOfLineCharacter(buff[i])){
				return buff.length - i;
			}
		}
		
		return -1;
	}
	
	public static String getFileLimit(){
		return getReadableFileSize(LIMIT_BYTES);
	}
	
	public static String getReadableFileSize(long size){
		return getReadableFileSize(size, "#,##0.#");		
	}
	
	public static String getReadableFileSize(long size, String pattern){
		if(size<=0)
			return "0";
		
		final String[] units = new String[]{ "B", "kB", "MB", "GB", "TB"};
		int digitGroups = (int)(Math.log10(size)/Math.log10(1024));
		
		return new DecimalFormat(pattern).format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
	
	public static long getFileSize(File file){
		if(!file.exists() || !file.isFile())
			return -1;
		
		return file.length();
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
	

	public static Pair<File> getFilesFromLocation(Context context, String filename, String location){

		File dir = context.getDir(location, Context.MODE_PRIVATE);
		ArrayList<File> fileList = (ArrayList<File>)FileHelper.getFileList(dir, true);
		
		Pair<String> origName = Helper.splitFileName(filename);
		
		File html = null, json = null;
		for(File file : fileList){
			Pair<String> name = Helper.splitFileName(file.getName());
			if(name.first().equals(origName.first())){
				
				if(name.second().equals(".json"))
					json = file;
				else
					html = file;
				
			}
		}
		
		return new Pair<File>(html, json);
	}
}
