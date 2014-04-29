package com.example.reader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class PresentationModule extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		Bundle bundle = getIntent().getExtras();
		Intent intent = new Intent(PresentationModule.this, ReaderActivity.class);
		intent.putExtra("LibraryBundle", bundle);
		startActivity(intent);
	}

	
	
}
