package com.ilearnrw.reader;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
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

import com.ilearnrw.reader.R;
import com.ilearnrw.reader.interfaces.OnHttpListener;
import com.ilearnrw.reader.tasks.AddToLibraryTask;
import com.ilearnrw.reader.types.ExplorerItem;
import com.ilearnrw.reader.types.ExplorerItem.FileType;
import com.ilearnrw.reader.types.adapters.ExplorerListAdapter;
import com.ilearnrw.reader.utils.AppLocales;
import com.ilearnrw.reader.utils.FileHelper;
import com.ilearnrw.reader.utils.HttpHelper;

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

	private SharedPreferences sp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_add_to_library_explorer);
		
        sp = PreferenceManager.getDefaultSharedPreferences(this);
		AppLocales.setLocales(getApplicationContext(), sp.getString(getString(R.string.sp_user_language), "en"));
		
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
					AlertDialog.Builder b = new AlertDialog.Builder(view.getContext())
						.setTitle(getString(R.string.copy_confirm_start) + file.getName() + getString(R.string.copy_confirm_end))
						.setNegativeButton(getString(android.R.string.no), null)
						.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								copyFileToLibrary(file);
							}
						});
					
					if(file.length() > FileHelper.LIMIT_BYTES){
						StringBuilder sb = new StringBuilder(getString(R.string.file_to_large));
						b.setMessage(sb.append(" ").append(FileHelper.getFileLimit()).toString());
					}
					b.show();
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
				items.add(new ExplorerItem(file.getName()+"/", file.getPath(), 0, FileType.Directory ));
			} else {
				if(hasValidFileEnding(file.getName()))
					objects.add(new ExplorerItem(file.getName(), file.getPath(), file.length(), FileType.File));
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
			items.add(0, new ExplorerItem("/", root, 0, FileType.Directory));
			items.add(1, new ExplorerItem("../", f.getParent(), 0, FileType.Directory));
			currentParent = f.getParent();
		} else {
			currentParent = "";
		}
		
		ArrayAdapter<ExplorerItem> adapter = new ExplorerListAdapter(this, R.layout.row_explorer_item, items, 20, true);
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
				boolean hiddenFiles = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.sp_show_all), false);
				ArrayList<File> files = (ArrayList<File>) FileHelper.getFileList(localDir, hiddenFiles);
				
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
				final String lang 	= preferences.getString(getString(R.string.sp_user_language), "");
				final int id 		= preferences.getInt(getString(R.string.sp_user_id), -1);
				final String token 	= preferences.getString(getString(R.string.sp_authToken), "");
				
				String name = file.getName();
				StringBuilder builder = new StringBuilder(name);
				
				for(File f : files){
					if(f.getName().equals(file.getName())){
						SimpleDateFormat df = new SimpleDateFormat("_yyyy_MM_dd-HH_mm_ss", Locale.getDefault());
						builder.insert(name.lastIndexOf("."), df.format(Calendar.getInstance().getTime()));
						break;
					}
				}
				
				//String data = FileHelper.inputStreamToString(fis);
				//String data = FileHelper.inputStreamReadBytes(fis, fSize);
				String data = FileHelper.inputStreamBufferRead(fis);
				fis.close();
				
				new AddToLibraryTask(this, this, this).run(data, Integer.toString(id), lang, token, builder.toString());
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
			final String newToken = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.sp_authToken), "");
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
