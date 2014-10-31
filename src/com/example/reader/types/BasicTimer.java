package com.example.reader.types;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.os.Environment;

public class BasicTimer {

	long start, end, elapsedTime;
	
	String root, filename, sectionName, timestamp;
	File timerDir, file;
	public BasicTimer(String filename){
		root = Environment.getExternalStorageDirectory().toString();
		timerDir = new File(root + "/ilearn_timer");
		timerDir.mkdirs();
	
		String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()); 
		String[] parts = filename.split("\\.");
		
		this.filename = parts[0] + "_" + currentDate + "." + parts[1];
	}
	
	public void start(String sectionName){	
		Date ts = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss", Locale.getDefault());
		timestamp = sdf.format(ts);		
		this.sectionName = sectionName;
		start = System.nanoTime();
	}
	
	public void stop(boolean manualSave){
		end = System.nanoTime();		
		elapsedTime = end - start;
		
		if(!manualSave)
			saveFile(new ArrayList<Long>(Arrays.asList(start, end, elapsedTime)), true);
		
	}
	
	public void save(boolean append){
		saveFile(new ArrayList<Long>(Arrays.asList(start, end, elapsedTime)), append);
	}
	
	private void saveFile(ArrayList<Long> out, boolean isAppending){
		file = new File(timerDir, filename);
		
		if(!isAppending)
			if(file.exists())
				file.delete();
		
		BufferedWriter writer =  null;
		try {
			writer= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, isAppending), "UTF-8"));
			writer.write("--- Start:"+sectionName+"(" + timestamp + ") ---\n");
			for(int i=0; i<out.size(); i++){
				Long value = out.get(i);

				if((i+1)%3==0)
					writer.write(TimeUnit.MILLISECONDS.convert(value, TimeUnit.NANOSECONDS)+"ms\n");
				else
					writer.write(value+"\n");
			}
			writer.write("---\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e2) {}
		}
	}
}
