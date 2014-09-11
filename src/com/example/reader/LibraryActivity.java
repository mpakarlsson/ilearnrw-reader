package com.example.reader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.example.reader.popups.RenameActivity;
import com.example.reader.types.LibraryAdapter;
import com.example.reader.types.LibraryItem;
import com.example.reader.types.Pair;
import com.example.reader.types.SideSelector;
import com.example.reader.utils.FileHelper;
import com.example.reader.utils.Helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
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
	
	private File libDir;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_library);
		
		libDir = getDir(getString(R.string.library_location), MODE_PRIVATE);
		
		boolean hiddenFiles = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("showAll", false);
		libraryFiles = (ArrayList<File>) FileHelper.getFileList(libDir, hiddenFiles);
		
		// Copies the files from 'res/raw' into the 'Library' folder, used for testing, remove when we got valid text files
		if(libraryFiles.isEmpty()){
			Field[] fields=R.raw.class.getFields();
		    for(int count=0; count < fields.length; count++){
		        Log.i("Raw Asset: ", fields[count].getName());
		    
		        try {
					int resourceID=fields[count].getInt(fields[count]);
					InputStream is = getResources().openRawResource(resourceID);
					String name = fields[count].getName();
					
					if(name.startsWith("_j_"))
						name = name.substring(3) + ".json";
					else if(name.startsWith("_t_"))
						name = name.substring(3) + ".txt";
					
					File f = new File(libDir, name);
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
		    
		    libraryFiles = (ArrayList<File>) FileHelper.getFileList(libDir, hiddenFiles);
		}
		
		files = new ArrayList<LibraryItem>();
		for(File f : libraryFiles){
			files.add(new LibraryItem(f.getName(), f));
		}
		sortValues();
		
		adapter = new LibraryAdapter(this, R.layout.row_library, files);
		
		library = (ListView) findViewById(R.id.library_list);
		library.setAdapter(adapter);
		library.setOnItemClickListener(this);
		registerForContextMenu(library);
		
		btnAdd = (ImageButton) findViewById(R.id.ibtn_add_library);
		btnAdd.setOnClickListener(this);
		
		sideSelector = (SideSelector) findViewById(R.id.library_side_selector);
		sideSelector.setListView(library);
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
		
		
		if(menuItemName.equals("Edit")){
			if(listItemName.endsWith(".json")){
				Toast.makeText(this, "You can't change name of a JSON file", Toast.LENGTH_SHORT).show();
				return true;
			}
			
			Intent i = new Intent(this, RenameActivity.class);
			i.putExtra("name", listItemName);
			startActivityForResult(i, FLAG_UPDATE_FILE_NAME);
			
		} else if(menuItemName.equals("Delete")){
			new AlertDialog.Builder(this)
				.setTitle(getString(R.string.dialog_remove_item_confirmation) + listItemName)
				.setNegativeButton(getString(android.R.string.no), null)
				.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {					
						Pair<String> fileName = Helper.splitFileName(clickItem.getName());
					
						String name = "current_" + fileName.first()+ "_" + fileName.second().substring(1);
						SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
						sp.edit().remove(name).commit();
						
						if(sp.getBoolean("showAll", false)){
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
		} else if(menuItemName.equals("Copy")){			
			new AlertDialog.Builder(this)
			.setTitle(getString(R.string.dialog_copy_item_confirmation))
			.setNegativeButton(getString(android.R.string.no), null)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					boolean result = FileHelper.copyFileToExternalStorage(clickItem.getFile(), clickItem.getName(), getString(R.string.external_storage_folder_name));
					
					if(result)
						Toast.makeText(getBaseContext(), getString(R.string.copy_file_external_succeeded), Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(getBaseContext(), getString(R.string.copy_file_external_failed), Toast.LENGTH_SHORT).show();
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
				boolean a = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("showAll", false);
				if(json!=null && a){
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
				libraryFiles = (ArrayList<File>) FileHelper.getFileList(libDir, PreferenceManager.getDefaultSharedPreferences(this).getBoolean("showAll", false));
				files.clear();
				for(File file : libraryFiles){
					files.add(new LibraryItem(file.getName(), file));
				}
				
				updateListView();
				sortValues();
				
				break;

			case FLAG_SETTINGS:
				Bundle b2 = data.getExtras();
				boolean showAll = b2.getBoolean("showAll");
				
				libraryFiles = (ArrayList<File>) FileHelper.getFileList(libDir, showAll);
				files = new ArrayList<LibraryItem>();
				for(File file : libraryFiles){
					files.add(new LibraryItem(file.getName(), file));
				}
				sortValues();
				adapter = new LibraryAdapter(this, R.layout.row_library, files);
				library.setAdapter(adapter);

				
				if(b2.getBoolean("format")){
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
		super.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibtn_add_library:
			Intent intent = new Intent(getBaseContext(), AddToLibraryExplorerActivity.class);
			startActivityForResult(intent, FLAG_ADD_TO_DEVICE);
			
			/*AlertDialog.Builder builder = new AlertDialog.Builder(this);
			LayoutInflater inflater = getLayoutInflater();
			View convertView = (View) inflater.inflate(R.layout.dialog_add_to_device, null);
			builder.setView(convertView);
			builder.setTitle(getString(R.string.dialog_title_add_to_device));
			final ListView lv = (ListView) convertView.findViewById(R.id.lv_add_to_device);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.add_to_device_array));
			lv.setAdapter(adapter);

			lv.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
					String option = lv.getItemAtPosition(pos).toString();
					option = option.toLowerCase(Locale.getDefault()).toString();
					
					Intent intent;
					if(option.contains("device"))
						intent = new Intent(getBaseContext(), AddToLibraryExplorerActivity.class);
					else 
						intent = new Intent(getBaseContext(), AddToLibraryORBActivity.class);
					
					startActivityForResult(intent, FLAG_ADD_TO_DEVICE);
					dialog.cancel();
				}
			});
			dialog = builder.create();
			dialog.show();
			*/
			break;

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		LibraryItem item = files.get(position);
		Pair<File> libItems = FileHelper.getFilesFromLibrary(this, item.getName());
		
		if(item.getName().endsWith(".txt") || item.getName().endsWith(".html")){
			Intent intent = new Intent(this, PresentationModule.class);
			intent.putExtra("file", libItems.first());
			intent.putExtra("json", libItems.second());
			intent.putExtra("title", libItems.first().getName());
			intent.putExtra("loadFiles", true);
			intent.putExtra("showGUI", false);
			this.startActivity(intent);
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
