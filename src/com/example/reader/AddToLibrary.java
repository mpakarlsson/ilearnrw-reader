package com.example.reader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class AddToLibrary extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String option = getIntent().getExtras().getString("option");
		
	}

	
	
}
