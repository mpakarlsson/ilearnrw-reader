package com.ilearnrw.reader.types;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
import java.util.ArrayList;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class LogBasicExclusionStrategy implements ExclusionStrategy {
	 private Class<?> c;
     private ArrayList<String> fieldNames;
     public LogBasicExclusionStrategy(String... skipVariables) throws SecurityException, NoSuchFieldException, ClassNotFoundException
     {
    	 this.c = LogEntry.class;
    	 /*
    	 if(skipVariables[0].contains("."))
    		 this.c = Class.forName(skipVariables[0].substring(0, skipVariables[0].lastIndexOf(".")));
    	 else
    		 this.c = Class.forName(skipVariables[0]);
    	 */
         fieldNames = new ArrayList<String>();
         
         for(int i=0; i<skipVariables.length; i++){
        	 if(skipVariables[i].contains("."))
        		 fieldNames.add(skipVariables[i].substring(skipVariables[i].lastIndexOf(".")+1));
        	 else
        		 fieldNames.add(skipVariables[i]);
         }
     }
     
     @Override
     public boolean shouldSkipClass(Class<?> arg0) {
         return false;
     }

     @Override
     public boolean shouldSkipField(FieldAttributes f) {
    	 if(f.getDeclaringClass() == c){
    		 for(int i=0; i<fieldNames.size(); i++){
    			 if(f.getName().equals(fieldNames.get(i)))
    				 return true;
    		 }
    	 }
    	 return false;
     }
}
