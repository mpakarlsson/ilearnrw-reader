package com.example.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.example.reader.popups.RenameActivity;
import com.example.reader.utils.FileHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;

public class LibraryActivity extends Activity implements OnClickListener , OnItemClickListener{

	public static ImageButton btn_add;
	public static ListView library;
	public static ArrayList<File> library_files;
	public ArrayList<LibraryItem> values;
	
	public AlertDialog dialog;
	
	public static final int FLAG_ADD_TO_DEVICE = 10000;
	public static final int FLAG_UPDATE_FILE_NAME = 10001;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_library);
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		btn_add = (ImageButton) findViewById(R.id.ibtn_add_library);
		btn_add.setOnClickListener(this);
		
		File dir = getDir(getString(R.string.library_location), MODE_PRIVATE);
		
		library_files = (ArrayList<File>) FileHelper.getFileList(dir);
		
		// Copies the files from 'res/raw' into the 'Library' folder, used for testing
		if(library_files.isEmpty()){
			Field[] fields=R.raw.class.getFields();
		    for(int count=0; count < fields.length; count++){
		        Log.i("Raw Asset: ", fields[count].getName());
		    
		        try {
					int resourceID=fields[count].getInt(fields[count]);
					InputStream is = getResources().openRawResource(resourceID);
					FileHelper.WriteToFile(is, fields[count].getName() + ".html", dir);		        
		        } catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
		    }
		    
		    library_files = (ArrayList<File>) FileHelper.getFileList(dir);
		}
		
		values = new ArrayList<LibraryItem>();
		for(File f : library_files){
			values.add(new LibraryItem(f.getName(), f));
		}
		sortValues();
		
		LibraryAdapter adapter = new LibraryAdapter(this, R.layout.library_row, values);

		library = (ListView) findViewById(R.id.library_list);
		library.setAdapter(adapter);
		library.setOnItemClickListener(this);
		registerForContextMenu(library);
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
			menu.setHeaderTitle(values.get(info.position).getName());
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
		String listItemName = values.get(info.position).getName();
		
		if(menuItemName.equals("Edit")){
			Toast.makeText(this, "EDIT", Toast.LENGTH_SHORT).show();
			
			Intent i = new Intent(this, RenameActivity.class);
			i.putExtra("file", values.get(info.position).getFile());
			i.putExtra("name", listItemName);
			i.putExtra("pos", info.position);
			startActivityForResult(i, FLAG_UPDATE_FILE_NAME);
			
		} else if(menuItemName.equals("Delete")){
			final int pos = info.position;
			
			new AlertDialog.Builder(this)
				.setTitle(getString(R.string.library_remove_item_confirmation) + listItemName)
				.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						values.remove(pos);
						((ArrayAdapter<LibraryItem>)library.getAdapter()).notifyDataSetChanged();
					}
				})
				.setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {}
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
					String name = data.getExtras().getString("name");
					
					if(f!=null && name!= null){
						values.add(new LibraryItem(name, f));
						sortValues();
						((ArrayAdapter)library.getAdapter()).notifyDataSetChanged();
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
					
					values.remove(pos);
					values.add(new LibraryItem(updatedName, updatedFile));
					
					sortValues();
					
					origFile.delete();
					((ArrayAdapter)library.getAdapter()).notifyDataSetChanged();
					
					break;

				default:
					break;
				}				
			}
			
		
		super.onActivityResult(requestCode, resultCode, data);
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
					Intent intent = new Intent(getBaseContext(), AddToLibraryActivity.class);
					String option = lv.getItemAtPosition(pos).toString();
					option = option.toLowerCase().toString();
					
					if(option.contains("device"))
						intent.putExtra("online", false);
					else 
						intent.putExtra("online", true);
					
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
		
		Intent intent = new Intent(this, PresentationModule.class);
		File f = library_files.get(position);
		intent.putExtra("file", f);
		intent.putExtra("title", f.getName());
		this.startActivity(intent);

	}
	
	private void sortValues(){
		Collections.sort(values, new Comparator<LibraryItem>() {
			@Override
			public int compare(LibraryItem lhs, LibraryItem rhs) {
				return lhs.getName().compareToIgnoreCase(rhs.getName());
			}
		});
	}
}
