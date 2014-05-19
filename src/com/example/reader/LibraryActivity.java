package com.example.reader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import com.example.reader.popups.RenameActivity;
import com.example.reader.types.Pair;
import com.example.reader.types.SideSelector;
import com.example.reader.utils.FileHelper;
import com.example.reader.utils.Helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class LibraryActivity extends Activity implements OnClickListener , OnItemClickListener{

	private static ImageButton btnAdd;
	private static ListView library;
	private SideSelector sideSelector;
	private static ArrayList<File> libraryFiles;
	private ArrayList<LibraryItem> files;
	
	private AlertDialog dialog;
	
	public static final int FLAG_ADD_TO_DEVICE = 10000;
	public static final int FLAG_UPDATE_FILE_NAME = 10001;
	public static final int FLAG_SETTINGS = 10002;
	
	
	private LibraryAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_library);

		btnAdd = (ImageButton) findViewById(R.id.ibtn_add_library);
		btnAdd.setOnClickListener(this);
		
		File dir = getDir(getString(R.string.library_location), MODE_PRIVATE);
		
		boolean hiddenFiles = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("showAll", false);
		
		libraryFiles = (ArrayList<File>) FileHelper.getFileList(dir, hiddenFiles);
		
		// Copies the files from 'res/raw' into the 'Library' folder, used for testing
		if(libraryFiles.isEmpty()){
			Field[] fields=R.raw.class.getFields();
		    for(int count=0; count < fields.length; count++){
		        Log.i("Raw Asset: ", fields[count].getName());
		    
		        try {
					int resourceID=fields[count].getInt(fields[count]);
					InputStream is = getResources().openRawResource(resourceID);
					File f = new File(dir, fields[count].getName()+".txt");
					FileHelper.saveFile(is, f);
					is.close();
		        } catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		    
		    libraryFiles = (ArrayList<File>) FileHelper.getFileList(dir, hiddenFiles);
		}
		
		files = new ArrayList<LibraryItem>();
		for(File f : libraryFiles){
			files.add(new LibraryItem(f.getName(), f));
		}
		sortValues();
		
		adapter = new LibraryAdapter(this, R.layout.library_row, files);
		
		library = (ListView) findViewById(R.id.library_list);
		library.setAdapter(adapter);
		library.setOnItemClickListener(this);
		registerForContextMenu(library);
		
		sideSelector = (SideSelector) findViewById(R.id.library_side_selector);
		sideSelector.setListView(library);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.library, menu);
		return true;
	}
	
	

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		switch (v.getId()) {
		case R.id.library_list:
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
			menu.setHeaderTitle(files.get(info.position).getName());
			String[] menuItems = getResources().getStringArray(R.array.library_context_menu);
			
			for(int i=0; i<menuItems.length; i++){
				menu.add(Menu.NONE, i, i, menuItems[i]);
			}
			break;

		default:
			break;
		}
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent i = new Intent(this, SettingsActivity.class);
			i.putExtra("setting", "library");
			startActivityForResult(i, FLAG_SETTINGS);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		int menuItemIndex = item.getItemId();
		String[] menuItems = getResources().getStringArray(R.array.library_context_menu);
		String menuItemName = menuItems[menuItemIndex];
		String listItemName = files.get(info.position).getName();
		
		if(menuItemName.equals("Edit")){			
			Intent i = new Intent(this, RenameActivity.class);
			i.putExtra("file", files.get(info.position).getFile());
			i.putExtra("name", listItemName);
			i.putExtra("pos", info.position);
			startActivityForResult(i, FLAG_UPDATE_FILE_NAME);
			
		} else if(menuItemName.equals("Delete")){
			final int pos = info.position;
			
			new AlertDialog.Builder(this)
				.setTitle(getString(R.string.library_remove_item_confirmation) + listItemName)
				.setNegativeButton(getString(android.R.string.no), null)
				.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						LibraryItem item = files.get(pos);						
						Pair<String> fileName = Helper.splitFileName(item.getName());
					
						String name = "current_" + fileName.first()+ "_" + fileName.second().substring(1);
						SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
						sp.edit().remove(name).commit();
						
						if(sp.getBoolean("showAll", false)){
							for(int i=files.size()-1; i>0; i--){
								LibraryItem libItem = files.get(i);
								Pair<String> fName = Helper.splitFileName(libItem.getName());

								if(fileName.first().equals(fName.first())){
									libItem.getFile().delete();
									files.remove(i);
								}
							}
						} else {
							File dir = getDir(getString(R.string.library_location), MODE_PRIVATE);
							ArrayList<File> fileList = (ArrayList<File>)FileHelper.getFileList(dir, true);
							
							item.getFile().delete();
							files.remove(pos);
							
							for(File file : fileList){
								Pair<String> fName = Helper.splitFileName(file.getName());
								if(fName.first().equals(fileName.first()) && fName.second().equals(".json")){
									file.delete();
									break;
								}
							}	
						}						
						
						updateListView();
					}
				}).show();			
		} else if(menuItemName.equals("Copy")){
			final int pos = info.position;
			
			new AlertDialog.Builder(this)
			.setTitle("Copy this file to external storage?")
			.setNegativeButton(getString(android.R.string.no), null)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					boolean result = FileHelper.copyFileToExternalStorage(files.get(pos).getFile(), files.get(pos).getName(), "Debug_IlearnRW");
					Toast.makeText(getBaseContext(), "Copying file to external storage was " + result, Toast.LENGTH_SHORT).show();
				}
			}).show();
		}
		
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK){
			switch (requestCode) {
			case FLAG_ADD_TO_DEVICE:
				if(dialog != null)
					dialog.cancel();
				
				File f = (File) data.getExtras().get("file");
				File json = (File) data.getExtras().get("json");
				String name = data.getExtras().getString("name");
				boolean exists = false;
				
				if(f!=null && name!= null){
					files.add(new LibraryItem(name, f));
					exists = true;
				}
				if(json!=null && PreferenceManager.getDefaultSharedPreferences(this).getBoolean("showAll", false)){
					files.add(new LibraryItem(json.getName(), json));
					exists = true;
				}
				
				if(exists){
					updateListView();
					sortValues();
				}
				break;
				
			case FLAG_UPDATE_FILE_NAME:
				Bundle b = data.getExtras();
				int pos = b.getInt("pos");
				String updatedName = b.getString("name");
				File origFile = (File) b.get("file");
				File updatedFile =  new File(getDir(getString(R.string.library_location), MODE_PRIVATE), updatedName);
				
				try {
					FileHelper.copy(origFile, updatedFile);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				files.get(pos).getFile().delete();
				files.remove(pos);
				files.add(new LibraryItem(updatedName, updatedFile));
				
				updateListView();
				sortValues();
				
				break;

			case FLAG_SETTINGS:
				File dir = getDir(getString(R.string.library_location), Context.MODE_PRIVATE);
				Bundle b2 = data.getExtras();
				boolean showAll = b2.getBoolean("showAll");
				PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("showAll", showAll).commit();
				
				libraryFiles = (ArrayList<File>) FileHelper.getFileList(dir, showAll);
				files = new ArrayList<LibraryItem>();
				for(File file : libraryFiles){
					files.add(new LibraryItem(file.getName(), file));
				}
				sortValues();
				adapter = new LibraryAdapter(this, R.layout.library_row, files);
				library.setAdapter(adapter);

				
				if(b2.getBoolean("format")){
					for(int i=0; i<files.size(); i++){
						LibraryItem item = files.get(i);
						item.getFile().delete();
					}
					
					files.clear();
					FileHelper.removeFiles(dir);
					
					updateListView();
				}
				break;
			default:
				break;
			}				
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onBackPressed() {

		new AlertDialog.Builder(this)
			.setTitle("Logout")
			.setMessage("Do you wish to log out?")
			.setNegativeButton(android.R.string.no, null)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
					editor.remove("username");
					editor.remove("password");
					editor.remove("rememberMe");
					editor.remove("authToken");
					editor.remove("refreshToken");
					editor.commit();
					
					LibraryActivity.super.onBackPressed();
				}
			}).show();
		
	}

	
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.ibtn_add_library:
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
					
					String option = lv.getItemAtPosition(pos).toString();
					option = option.toLowerCase(Locale.getDefault()).toString();
					
					Intent intent;
					
					if(option.contains("device"))
						intent = new Intent(getBaseContext(), AddToLibraryExplorerActivity.class);
					else 
						intent = new Intent(getBaseContext(), AddToLibraryORBActivity.class);
					
					startActivityForResult(intent, FLAG_ADD_TO_DEVICE);
				}
			});
			dialog = builder.create();
			
			dialog.show();
			break;

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		File f = files.get(position).getFile();
		
		File dir = getDir(getString(R.string.library_location), MODE_PRIVATE);
		ArrayList<File> fileList = (ArrayList<File>)FileHelper.getFileList(dir, true);
		
		String fileName = f.getName();
		fileName = fileName.substring(0, fileName.lastIndexOf("."));
		
		File json = null;
		for(File file : fileList){
			Pair<String> name = Helper.splitFileName(file.getName());
			if(name.first().equals(fileName) && name.second().equals(".json")){
				json = file;
				break;
			}
		}
		
		String name = f.getName();
		if(name.endsWith(".txt") || name.endsWith(".html")){
			Intent intent = new Intent(this, PresentationModule.class);
			intent.putExtra("file", f);
			intent.putExtra("json", json);
			intent.putExtra("title", f.getName());
			this.startActivity(intent);
		} else if(name.endsWith(".json")){
			new AlertDialog.Builder(this)
			.setTitle("JSON file")
			.setMessage("This file contains information about a .txt or .html with the same name.")
			.setPositiveButton(android.R.string.ok, null).show();
		}
		else {
			
			new AlertDialog.Builder(this)
			.setTitle("Invalid file type")
			.setMessage("Can not open file, " + f.getName() + ", please select another one")
			.setPositiveButton(android.R.string.ok, null).show();
			
			
		}

	}
	
	private void updateListView(){
		adapter.notifyDataSetChanged();
		sideSelector.setListView(library);

		if(sideSelector.getNumSections() == 0)
			sideSelector.setVisibility(View.GONE);
		else
			sideSelector.setVisibility(View.VISIBLE);
	}
	
	private void sortValues(){
		Collections.sort(files, new Comparator<LibraryItem>() {
			@Override
			public int compare(LibraryItem lhs, LibraryItem rhs) {
				return lhs.getName().compareToIgnoreCase(rhs.getName());
			}
		});
	}
}
