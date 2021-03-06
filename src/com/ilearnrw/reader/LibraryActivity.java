package com.ilearnrw.reader;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import com.ilearnrw.reader.R;
import com.ilearnrw.reader.popups.RenameActivity;
import com.ilearnrw.reader.tasks.OpenBookTask;
import com.ilearnrw.reader.texttospeech.TextToSpeechReader;
import com.ilearnrw.reader.types.LibraryItem;
import com.ilearnrw.reader.types.Pair;
import com.ilearnrw.reader.types.SystemTags;
import com.ilearnrw.reader.types.adapters.LibraryAdapter;
import com.ilearnrw.reader.types.singleton.ProfileUser;
import com.ilearnrw.reader.utils.AppLocales;
import com.ilearnrw.reader.utils.FileHelper;
import com.ilearnrw.reader.utils.Helper;
import com.ilearnrw.reader.utils.HttpHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class LibraryActivity extends Activity implements OnClickListener , OnItemClickListener{

	private static ImageButton btnAdd, btnSettings, btnRules, btnLogout;
	//private static ListView library;
	private static GridView library;
	//private SideSelector sideSelector;
	private static ArrayList<File> libraryFiles;
	private ArrayList<LibraryItem> files;
	
	private AlertDialog dialog;
	private SharedPreferences preferences;
	
	public static final int FLAG_ADD_TO_DEVICE = 10000;
	public static final int FLAG_UPDATE_FILE_NAME = 10001;
	public static final int FLAG_SETTINGS = 10002;
	
	private LibraryAdapter adapter;
	
	private File libDir;
	
	private boolean activateRules;
	
	private boolean offlineMode = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_library);
		
		TextToSpeechReader.getInstance(getApplicationContext()).initializeTextToSpeech(Locale.UK);		
		
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
		AppLocales.setLocales(getApplicationContext(), preferences.getString(getString(R.string.sp_user_language), "en"));
		
		String jsonProfile = preferences.getString(getString(R.string.sp_user_profile_json), "");
		
		if(!jsonProfile.isEmpty()){
			initProfile(jsonProfile);
		}
		
		libDir = getDir(getString(R.string.library_location), MODE_PRIVATE);
		
		boolean hiddenFiles = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.sp_show_all), false);
		libraryFiles = (ArrayList<File>) FileHelper.getFileList(libDir, hiddenFiles);
		
		String ulang = preferences.getString(getString(R.string.sp_user_language), "en");
		ulang = ulang.toLowerCase(Locale.getDefault());
		
		String versionName = preferences.getString("versionName", "0.00");
		String currVersionName = Helper.getVersionName(this);
		if(!versionName.equals(currVersionName)){
			FileHelper.copyBundledFiles(this, ulang);
			libraryFiles = (ArrayList<File>) FileHelper.getFileList(libDir, hiddenFiles);
			preferences.edit().putString("versionName", currVersionName).apply();
		}
		
		if(libraryFiles.isEmpty()){	
			FileHelper.copyBundledFiles(this, ulang);
			libraryFiles = (ArrayList<File>) FileHelper.getFileList(libDir, hiddenFiles);
		}
		
		files = new ArrayList<LibraryItem>();
		for(File f : libraryFiles){
			files.add(new LibraryItem(f.getName(), f));
		}
		sortValues();
		
		adapter = new LibraryAdapter(this, R.layout.row_library, files);
		
		library = (GridView) findViewById(R.id.library_grid);
		library.setAdapter(adapter);
		library.setOnItemClickListener(this);
		registerForContextMenu(library);
		

		btnAdd = (ImageButton) findViewById(R.id.ibtn_add_library);
		btnAdd.setOnClickListener(this);
		
		btnSettings = (ImageButton) findViewById(R.id.presentation_settings);
		btnSettings.setOnClickListener(this);
		
		btnRules = (ImageButton) findViewById(R.id.ibtn_rules);
		btnRules.setOnClickListener(this);
		
		btnLogout = (ImageButton) findViewById(R.id.ibtn_logout);
		btnLogout.setOnClickListener(this);
		
		
		activateRules = preferences.getBoolean("activateRules", false);
		if(activateRules)
			btnRules.setImageResource(R.drawable.rules_active);
		else
			btnRules.setImageResource(R.drawable.rules_inactive);
		
		offlineMode = false;
		Intent intent =  getIntent();
		
		Bundle b = intent.getExtras();
		
		if(b!=null)
			if(b.containsKey("offline_mode"))
				offlineMode = b.getBoolean("offline_mode");
		
		if(offlineMode){
			btnAdd.setVisibility(View.GONE);
		}
		//sideSelector = (SideSelector) findViewById(R.id.library_side_selector);
		//sideSelector.setListView(library);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		switch (v.getId()) {
		case R.id.library_grid:
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
	
	/*@Override
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
			Intent i = new Intent(this, SettingsActivity.class);
			i.putExtra("setting", "library");
			startActivityForResult(i, FLAG_SETTINGS);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	*/
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		final int pos = info.position;
		
		int menuItemIndex = item.getItemId();
		String[] menuItems = getResources().getStringArray(R.array.library_context_menu);
		String menuItemName = menuItems[menuItemIndex];
		
		final LibraryItem clickItem = files.get(pos);
		String listItemName = clickItem.getName();
		
		
		if(menuItemName.equals("Change book name") || menuItemName.equals("Επεξεργασία")){
			if(listItemName.endsWith(".json")){
				Toast.makeText(this, "You can't change name of a JSON file", Toast.LENGTH_SHORT).show();
				return true;
			}
			
			Intent i = new Intent(this, RenameActivity.class);
			i.putExtra("name", listItemName);
			startActivityForResult(i, FLAG_UPDATE_FILE_NAME);
			
		} else if(menuItemName.equals("Remove book") || menuItemName.equals("Διαγραφή")){
			new AlertDialog.Builder(this)
				.setTitle(getString(R.string.dialog_remove_item_confirmation) + listItemName)
				.setNegativeButton(getString(android.R.string.no), null)
				.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {					
						Pair<String> fileName = Helper.splitFileName(clickItem.getName());
					
						String name = "current_" + fileName.first()+ "_" + fileName.second().substring(1);
						
						preferences.edit().remove(name).apply();
						
						if(preferences.getBoolean(getString(R.string.sp_show_all), false)){
							for(int i=files.size()-1; i>=0; i--){
								LibraryItem libItem = files.get(i);
								Pair<String> fName = Helper.splitFileName(libItem.getName());

								if(fileName.first().equals(fName.first())){
									libItem.getFile().delete();
									files.remove(i);
								}
							}
						} else {							
							clickItem.getFile().delete();
							files.remove(pos);
							
							ArrayList<File> fileList = (ArrayList<File>)FileHelper.getFileList(libDir, true);

							for(File file : fileList){
								Pair<String> fName = Helper.splitFileName(file.getName());
								if(fName.first().equals(fileName.first())){
									file.delete();
									break;
								}
							}	
						}						
						
						updateListView();
					}
				}).show();			
		} else if(menuItemName.equals("Copy book") || menuItemName.equals("Αντιγραφή")){			
			new AlertDialog.Builder(this)
			.setTitle(getString(R.string.dialog_copy_item_confirmation))
			.setNegativeButton(getString(android.R.string.no), null)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ArrayList<File> libFiles = (ArrayList<File>) FileHelper.getFileList(libDir, true);
					String name = Helper.removeSubstring(new StringBuilder(clickItem.getName()), ".txt").toString();
					boolean result = false;
					
					for(File item : libFiles){
						 
						String n = item.getName();
						if(n.endsWith(".json"))
							n = Helper.removeSubstring(new StringBuilder(item.getName()), ".json").toString();
						else if(n.endsWith(".txt"))
							n = Helper.removeSubstring(new StringBuilder(item.getName()), ".txt").toString();
						
						if(n.equals(name)){
							result = FileHelper.copyFileToExternalStorage(item, item.getName(), getString(R.string.external_storage_folder_name));
							
							if(result)
								Toast.makeText(getBaseContext(), getString(R.string.copy_file_external_succeeded) + " " + item.getName(), Toast.LENGTH_SHORT).show();
							else
								Toast.makeText(getBaseContext(), getString(R.string.copy_file_external_failed) + " " + item.getName(), Toast.LENGTH_SHORT).show();
						}
					}
				}
			}).show();
		}
		
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK){
			Bundle b = data.getExtras();
			
			switch (requestCode) {
			case FLAG_ADD_TO_DEVICE:
				if(dialog != null)
					dialog.cancel();
				
				
				String fName = b.getString("file");
				String jName = b.getString("json");
				boolean exists = false;
				
				File f = FileHelper.getFile(fName, libDir);
				File json = FileHelper.getFile(jName, libDir);
				
				if(f!=null){
					files.add(new LibraryItem(fName, f));
					exists = true;
				}
				boolean a = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.sp_show_all), false);
				if(json!=null && a){
					files.add(new LibraryItem(jName, json));
					exists = true;
				}
				
				if(exists){
					updateListView();
					sortValues();
				}
				break;
				
			case FLAG_UPDATE_FILE_NAME:
				Pair<String> updatedName = Helper.splitFileName(b.getString("name"));
				Pair<String> orgFilename = Helper.splitFileName(b.getString("orgName"));
				
				ArrayList<File> libFiles = (ArrayList<File>) FileHelper.getFileList(libDir, true);
				
				boolean fileExists = false;
				ArrayList<File> updateFiles = new ArrayList<File>();
				for(int i=0; i<libFiles.size(); i++){
					File file = libFiles.get(i);
					Pair<String> fileInfo = Helper.splitFileName(file.getName());

					if(fileInfo.first().equals(updatedName.first())){
						fileExists = true;
						break;
					} else if(fileInfo.first().equals(orgFilename.first())){
						updateFiles.add(file);
					}
				}
				
				if(fileExists){
					Toast.makeText(this, "File already exists with this name", Toast.LENGTH_SHORT).show();
					break;
				}
				
				for(int i=0; i<updateFiles.size(); i++){					
					File file = updateFiles.get(i);
					Pair<String> fileInfo = Helper.splitFileName(file.getName());
					
					File updatedFile = new File(libDir, updatedName.first() + fileInfo.second());
					try {
						FileHelper.copy(file, updatedFile);
						file.delete();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				libraryFiles = (ArrayList<File>) FileHelper.getFileList(libDir, PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.sp_show_all), false));
				files.clear();
				for(File file : libraryFiles){
					files.add(new LibraryItem(file.getName(), file));
				}
				
				updateListView();
				sortValues();
				
				break;

			case FLAG_SETTINGS:
				boolean showAll = b.getBoolean("showAll");
				
				libraryFiles = (ArrayList<File>) FileHelper.getFileList(libDir, showAll);
				files = new ArrayList<LibraryItem>();
				for(File file : libraryFiles){
					files.add(new LibraryItem(file.getName(), file));
				}
				sortValues();
				adapter = new LibraryAdapter(this, R.layout.row_library, files);
				library.setAdapter(adapter);

				
				if(b.getBoolean("format")){
					for(int i=0; i<files.size(); i++){
						LibraryItem item = files.get(i);
						item.getFile().delete();
					}
					
					files.clear();
					FileHelper.removeFiles(libDir);
					
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
		btnLogout.callOnClick();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibtn_add_library:
			Intent intent = new Intent(getBaseContext(), AddToLibraryExplorerActivity.class);
			startActivityForResult(intent, FLAG_ADD_TO_DEVICE);
			break;
		case R.id.presentation_settings:
			Intent intent2 = new Intent(this, GroupsActivity.class);
			this.startActivity(intent2);
			break;

		case R.id.ibtn_rules:
			activateRules = !activateRules;
			
			if(activateRules)
				btnRules.setImageResource(R.drawable.rules_active);
			else
				btnRules.setImageResource(R.drawable.rules_inactive);

				preferences.edit().putBoolean("activateRules", activateRules).apply();

			break;
		case R.id.ibtn_logout:
			if(offlineMode){
				HttpHelper.log(LibraryActivity.this, "Logout (Offline).", SystemTags.APP_SESSION_END);
				Intent i = new Intent(LibraryActivity.this, LoginActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(i);
			} else {
				new AlertDialog.Builder(this)
				.setTitle(getString(R.string.dialog_logout_title))
				.setMessage(getString(R.string.dialog_logout_message))
				.setNegativeButton(android.R.string.no, null)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPreferences.Editor edit= preferences.edit();
						edit.remove(getString(R.string.sp_authToken));
						edit.remove(getString(R.string.sp_refreshToken));
						edit.apply();
						
						HttpHelper.log(LibraryActivity.this, "Logout.", SystemTags.APP_SESSION_END);
						
						Intent i = new Intent(LibraryActivity.this, LoginActivity.class);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(i);
					}
				}).show();
			}
			
			break;
		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		LibraryItem item = files.get(position);
		Pair<File> libItems = FileHelper.getFilesFromLocation(this, item.getName(), getString(R.string.library_location));
		 
		if(item.getName().endsWith(".txt") || item.getName().endsWith(".html")){
			new OpenBookTask(this, libItems, activateRules).run();
		} else if(item.getName().endsWith(".json")){
			new AlertDialog.Builder(this)
			.setTitle(getString(R.string.dialog_json_title))
			.setMessage(getString(R.string.dialog_json_message))
			.setPositiveButton(android.R.string.ok, null).show();
		}
		else {
			new AlertDialog.Builder(this)
			.setTitle(getString(R.string.folder_invalid))
			.setMessage(getString(R.string.folder_open_failed) + item.getName())
			.setPositiveButton(android.R.string.ok, null).show();
		}
	}
	
	private void initProfile(String jsonProfile){
		ProfileUser.getInstance(this.getApplicationContext()).getProfile();
	}
	
	private void updateListView(){
		adapter.notifyDataSetChanged();
		//sideSelector.setListView(library);

		//if(sideSelector.getNumSections() == 0)
		//	sideSelector.setVisibility(View.GONE);
		//else
		//	sideSelector.setVisibility(View.VISIBLE);
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
