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

import org.apache.http.HttpResponse;

import com.example.reader.results.TextAnnotationResult;
import com.example.reader.types.ExplorerItem;
import com.example.reader.utils.FileHelper;
import com.example.reader.utils.HttpHelper;
import com.google.gson.Gson;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AddToLibraryExplorerActivity extends Activity {
	
	private List<ExplorerItem> items = null;
	private String root = Environment.getExternalStorageDirectory().getPath();
	private TextView tvPath;
	private ListView lvList;
	private String[] FILE_ENDINGS = { "html", "txt" };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_to_library_explorer);
		
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

	private void getDirectory(String dir){
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
		}
		
		String[] listItems = new String[items.size()];
		for(int i=0; i<items.size(); i++){
			listItems[i] = items.get(i).getItem();
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
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
						
						new AddToLibraryTask().execute(data, Integer.toString(id), lang, token, builder.toString());
						return;
					}
				}
				
				String data = FileHelper.inputStreamToString(fis);
				fis.close();
				
				new AddToLibraryTask().execute(data, Integer.toString(id), lang, token, file.getName());
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class AddToLibraryTask extends AsyncTask<String, Void, TextAnnotationResult>{
		private ProgressDialog dialog;
		private String filename;
		
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(AddToLibraryExplorerActivity.this);
			dialog.setTitle(getString(R.string.dialog_annotation_title));
			dialog.setMessage(getString(R.string.dialog_annotation_message));
			dialog.setCancelable(true);
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Toast.makeText(getBaseContext(), getString(R.string.annotation_aborted), Toast.LENGTH_SHORT).show();
					dialog.dismiss();
					cancel(true);
				}
			});
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					Toast.makeText(getBaseContext(), getString(R.string.annotation_aborted), Toast.LENGTH_SHORT).show();
					dialog.dismiss();
					cancel(true);
				}
			});
			dialog.show();
			super.onPreExecute();
		}

		@Override
		protected TextAnnotationResult doInBackground(String... params) {
			filename = params[4];
			HttpResponse response = HttpHelper.post("http://api.ilearnrw.eu/ilearnrw/text/annotate?userId=" + params[1] + "&lc=" + params[2]+ "&token=" + params[3], params[0]);

			ArrayList<String> data = HttpHelper.handleResponse(response);
			
			if(data.size()==1){
				return null;
			} else {
				System.out.println(data.get(1));
				TextAnnotationResult result = null;
				try {
					String json = data.get(1);
					result = new Gson().fromJson(json, TextAnnotationResult.class);
				} catch (Exception e) {
					e.printStackTrace();
					result = null;
				}
				
				return result;
			}
		}
		
		
		@Override
		protected void onPostExecute(TextAnnotationResult result) {
			if(dialog.isShowing())
				dialog.dismiss();
			
			if(result != null){
				Toast.makeText(AddToLibraryExplorerActivity.this, getString(R.string.annotation_succeeded), Toast.LENGTH_SHORT).show();
				
				Gson gson =  new Gson();
				int index = filename.lastIndexOf(".");
				String name = filename.substring(0, index);
				File dir = getDir(getString(R.string.library_location), Context.MODE_PRIVATE);
				
				File newFile = new File(dir, filename);
				FileHelper.saveFile(result.html, newFile);
				String wordSet = gson.toJson(result.wordSet);
				File jsonFile = new File(dir, name+".json");
				FileHelper.saveFile(wordSet, jsonFile);

				Intent intent=new Intent();
			    intent.putExtra("file", newFile);
			    intent.putExtra("json", jsonFile);
				intent.putExtra("name", filename);
				setResult(Activity.RESULT_OK, intent);
				finish();
			} else 
				Toast.makeText(AddToLibraryExplorerActivity.this, getString(R.string.annotation_failed), Toast.LENGTH_SHORT).show();
			
		}
		
	};
	
}
