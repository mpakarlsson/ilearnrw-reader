package com.example.reader.types;

public class Pair<T> {

	private final T mFirst;
	private final T mSecond;
	
	public Pair(T first, T second){
		mFirst 	= first;
		mSecond = second;
	}
	
	public T first(){
		return mFirst;
	}
	
	public T second(){
		return mSecond;
	}
}
