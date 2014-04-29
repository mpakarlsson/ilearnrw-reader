package com.example.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;

public class LibraryActivity extends Activity implements OnClickListener , OnItemClickListener, OnItemLongClickListener{

	public static ImageButton btn_add;
	public static ListView library;
	public static String lib_location = "Library";
	public static ArrayList<File> library_files;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_library);
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		btn_add = (ImageButton) findViewById(R.id.ibtn_add_library);
		btn_add.setOnClickListener(this);
		
		File dir = getDir(lib_location, MODE_PRIVATE);
		
		library_files = (ArrayList<File>) getListFiles(dir);
		
		// Copies the files from 'res/raw' into the 'Library' folder, used for testing
		if(library_files.isEmpty()){
			Field[] fields=R.raw.class.getFields();
		    for(int count=0; count < fields.length; count++){
		        Log.i("Raw Asset: ", fields[count].getName());
		    
		        try {
					int resourceID=fields[count].getInt(fields[count]);
					InputStream is = getResources().openRawResource(resourceID);
					WriteToFile(is, fields[count].getName() + ".html", dir);
		        
		        } catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
		    }
		    
		    library_files = (ArrayList<File>) getListFiles(dir);
		}
		
		ArrayList<LibraryItem> values = new ArrayList<LibraryItem>();
		for(File f : library_files){
			values.add(new LibraryItem(f.getName(), f));
		}
		
		LibraryAdapter adapter = new LibraryAdapter(this, R.layout.library_row, values);

		library = (ListView) findViewById(R.id.library_list);
		library.setAdapter(adapter);
		library.setOnItemClickListener(this);
		library.setOnItemLongClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.library, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.ibtn_add_library:
			// TODO: create popup window that lets you choose how to add new items to "Library" folder, choices "Device" & "Online Resource bank"
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			LayoutInflater inflater = getLayoutInflater();
			View convertView = (View) inflater.inflate(R.layout.dialog_add_to_device, null);
			builder.setView(convertView);
			builder.setTitle(getString(R.string.dialog_title_add_to_device));
			final ListView lv = (ListView) convertView.findViewById(R.id.lv_add_to_device);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.add_to_device_array));
			lv.setAdapter(adapter);
			
			lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int pos, long id) {
				
					Toast.makeText(getBaseContext(), "Id : " + id + " pos " + pos, Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(getBaseContext(), AddToLibrary.class);
					intent.putExtra("option", lv.getItemAtPosition(pos).toString());
					startActivity(intent);
				}
			});
			
			
			builder.show();
			break;

		default:
			break;
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(this, PresentationModule.class);
		File f = library_files.get(position);
		intent.putExtra("file", f);
		intent.putExtra("title", f.getName());
		this.startActivity(intent);		
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO: Add a context menu for the item clicked, that contains relevant options ("Delete" etc.);
		Toast.makeText(this, "Long Clicked: " + position, Toast.LENGTH_SHORT).show();
		return true;
	}

	
	private List<File> getListFiles(File parent){
		List<File> dirFiles = new ArrayList<File>();
		File[] files = parent.listFiles();
		
		for( File file : files){
			if(file.isDirectory()){
				dirFiles = getListFiles(file);
			} else {
				if(file.getName().endsWith(".html")){
					dirFiles.add(file);
				}
			}
		}
		return dirFiles; 
	}
	
	private void WriteToFile(InputStream is, String fileName, File directory){
		
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	

	
}
