package com.example.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;



import com.example.reader.interfaces.OnHttpListener;
import com.example.reader.tasks.AddToLibraryTask;
import com.example.reader.types.BasicListAdapter;
import com.example.reader.types.ExplorerItem;
import com.example.reader.utils.FileHelper;
import com.example.reader.utils.HttpHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AddToLibraryExplorerActivity 
	extends 
		Activity 
	implements 
		OnHttpListener {
	
	private List<ExplorerItem> items = null;
	private String root = Environment.getExternalStorageDirectory().getPath();
	private String currentDir, currentParent;
	private TextView tvPath;
	private ListView lvList;
	private String[] FILE_ENDINGS = { "html", "txt" };
	private String TAG;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_to_library_explorer);
		
		TAG = getClass().getName();
		
		tvPath = (TextView) findViewById(R.id.tv_atl_path);
		lvList = (ListView) findViewById(R.id.lv_atl_list);
		getDirectory(root);
		
		lvList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int pos,
					long id) {
				final File file = new File(items.get(pos).getPath());
				
				if(file.isDirectory()){
					if(file.canRead()){
						getDirectory(items.get(pos).getPath());
					} else {
						new AlertDialog.Builder(view.getContext())
							.setTitle("[" + file.getName() + "] " + getString(R.string.folder_unreadable))
							.setPositiveButton(getString(android.R.string.ok), null).show();
					}
				} else {
					new AlertDialog.Builder(view.getContext())
						.setTitle(getString(R.string.copy_confirm_start) + file.getName() + getString(R.string.copy_confirm_end))
						.setNegativeButton(getString(android.R.string.no), null)
						.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								copyFileToLibrary(file);
							}
						}).show();
				}
			}
			
		});
	}

	@Override
	public void onBackPressed() {
		if(currentDir.equals(root)){
			super.onBackPressed();
		}
		else
			getDirectory(currentParent);
	}

	private void getDirectory(String dir){
		currentDir = dir;
		tvPath.setText(dir);
		
		items = new ArrayList<ExplorerItem>();
		
		File f = new File(dir);
		File[] files = f.listFiles();
		
		ArrayList<ExplorerItem> objects = new ArrayList<ExplorerItem>();
		
		for(int i=0; i<files.length;i++){
			File file = files[i];
			if(file.isDirectory() && file.canRead()){
				items.add(new ExplorerItem(file.getName()+"/", file.getPath()));
			} else {
				if(hasValidFileEnding(file.getName()))
					objects.add(new ExplorerItem(file.getName(), file.getPath()));
			}
		}
		
		Collections.sort(items, new Comparator<ExplorerItem>() {
			@Override
			public int compare(ExplorerItem lhs, ExplorerItem rhs) {
				return lhs.getItem().compareToIgnoreCase(rhs.getItem());
			}
		});
		
		Collections.sort(objects, new Comparator<ExplorerItem>() {
			@Override
			public int compare(ExplorerItem lhs, ExplorerItem rhs) {
				return lhs.getItem().compareToIgnoreCase(rhs.getItem());
			}
		});
		
		items.addAll(objects);
		
		if(!dir.equals(root)){
			items.add(0, new ExplorerItem("/", root));
			items.add(1, new ExplorerItem("../", f.getParent()));
			currentParent = f.getParent();
		} else {
			currentParent = "";
		}
		
		String[] listItems = new String[items.size()];
		for(int i=0; i<items.size(); i++){
			listItems[i] = items.get(i).getItem();
		}
		
		ArrayAdapter<String> adapter = new BasicListAdapter(this, R.layout.textview_item_multiline, listItems, 20, true);
		lvList.setAdapter(adapter);
	}

	
	private boolean hasValidFileEnding(String name){
		int index = name.lastIndexOf(".");
		
		if(index!=-1){
			String ending = name.substring(index+1);
			ending = ending.toLowerCase(Locale.getDefault()).trim();
			
			if(ending.isEmpty())
				return false;
			
			for (int i = 0; i < FILE_ENDINGS.length; i++) {
				if(ending.equals(FILE_ENDINGS[i]))
					return true;
			}
		}
		return false;
	}
	
	public void copyFileToLibrary(final File file){
		if(file!=null){
			try {
				final FileInputStream fis = new FileInputStream(file);
				final File localDir = getDir(getString(R.string.library_location), MODE_PRIVATE);
				boolean hiddenFiles = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("showAll", false);
				ArrayList<File> files = (ArrayList<File>) FileHelper.getFileList(localDir, hiddenFiles);
				
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
				final String lang 	= preferences.getString("language", "");
				final int id 		= preferences.getInt("id", -1);
				final String token 	= preferences.getString("authToken", "");
				
				for(File f : files){
					if(f.getName().equals(file.getName())){
						String name = file.getName();
						Calendar c = Calendar.getInstance();
						SimpleDateFormat df = new SimpleDateFormat("_yyyy_MM_dd-HH_mm_ss", Locale.getDefault());
						StringBuilder builder = new StringBuilder(name);
						
						builder.insert(name.lastIndexOf("."), df.format(c.getTime()));			
						String data = FileHelper.inputStreamToString(fis);
						fis.close();
						
						new AddToLibraryTask(this, this, this).run(data, Integer.toString(id), lang, token, builder.toString());
						return;
					}
				}
				
				String data = FileHelper.inputStreamToString(fis);
				fis.close();
				
				new AddToLibraryTask(this, this, this).run(data, Integer.toString(id), lang, token, file.getName());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onTokenExpired(final String... params) {
		if(HttpHelper.refreshTokens(this)){
			final String newToken = PreferenceManager.getDefaultSharedPreferences(this).getString("authToken", "");
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					new AddToLibraryTask(AddToLibraryExplorerActivity.this, AddToLibraryExplorerActivity.this, AddToLibraryExplorerActivity.this).run(params[0], params[1], params[2], newToken, params[4]);
					Log.d(TAG, getString(R.string.token_error_retry));
					Toast.makeText(AddToLibraryExplorerActivity.this, getString(R.string.token_error_retry), Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
	
}
