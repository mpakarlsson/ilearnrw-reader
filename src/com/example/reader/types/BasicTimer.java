package com.example.reader.types;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import android.os.Environment;

public class BasicTimer {

	long start, end, elapsedTime;
	boolean isStarted;
	
	
	String root, filename;
	File timerDir, file;
	public BasicTimer(String filename){
		
		isStarted = false;
		
		//long start = System.nanoTime(); 
		// method()
		//long end = System.nanoTime(); 
		//long elapsedTime = end - start;
		//TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
		
		root = Environment.getExternalStorageDirectory().toString();
		timerDir = new File(root + "/ilearn_timer");
		timerDir.mkdirs();
	
		this.filename = filename;
	}
	
	public void start(){
		if(isStarted){
			stop();
		}
		
		isStarted = true;
		start = System.nanoTime();
	}
	
	public void stop(){
		end = System.nanoTime();
		isStarted = false;
		
		elapsedTime = end - start;
		
		ArrayList<Long> out = new ArrayList<Long>(Arrays.asList(start, end, elapsedTime));
		saveFile(out, true);
	}
	
	public void saveFile(ArrayList<Long> out, boolean isAppending){
		file = new File(timerDir, filename);
		
		if(!isAppending)
			if(file.exists())
				file.delete();
		
		BufferedWriter writer =  null;
		try {
			writer= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, isAppending), "UTF-8"));
			for(int i=0; i<out.size(); i++){
				Long value = out.get(i);

				if(value%3==0)
					writer.write(TimeUnit.SECONDS.convert(value, TimeUnit.NANOSECONDS)+"\n");
				else
					writer.write(value+"\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e2) {}
		}
	}
}
