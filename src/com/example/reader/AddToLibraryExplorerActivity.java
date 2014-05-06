package com.example.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.example.reader.serveritems.TextAnnotationResult;
import com.example.reader.serveritems.UserDetailResult;
import com.example.reader.utils.FileHelper;
import com.google.gson.Gson;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
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
	private Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_to_library_explorer);
		
		tvPath = (TextView) findViewById(R.id.tv_atl_path);
		lvList = (ListView) findViewById(R.id.lv_atl_list);
		getDirectory(root);
		
		
		setupHandlers();
		
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
							.setTitle("[" + file.getName() + "] folder can't be read!")
							.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							}).show();
					}
				} else {
					new AlertDialog.Builder(view.getContext())
						.setTitle(getString(R.string.add_to_device_explorer_copy_confirm_start) + file.getName() + getString(R.string.add_to_device_explorer_copy_confirm_end))
						.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								copyFileToLibrary(file);
							}
						})
						.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Toast.makeText(view.getContext(), "File was not copied", Toast.LENGTH_SHORT).show();
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

			ending = ending.toLowerCase().trim();
			
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
				ArrayList<File> files = (ArrayList<File>) FileHelper.getFileList(localDir);
				
				for(File f : files){
					if(f.getName().equals(file.getName())){
						String name = file.getName();
						Calendar c = Calendar.getInstance();
						SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
						StringBuilder builder = new StringBuilder(name);
						
						builder.insert(name.lastIndexOf("."), df.format(c.getTime()));
						
						
						SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
						final String lang 	= preferences.getString("language", "");
						final int id 			= preferences.getInt("id", -1);
						final String token 	= preferences.getString("authToken", "");
						
						String data = "This is a test string. Testing out text annotation.";
						String url = "http://api.ilearnrw.eu/ilearnrw/text/annotate?userId=" + id + "&lc=" + lang + "&token=" + token;
						//String url = "http://httpbin.org/post";
						
						new HttpConnection(handler).post(url, data);
						//Accept=application/json
						
						/*Toast.makeText(this, "File already exists,  changed name to " + name, Toast.LENGTH_SHORT).show();
						
						FileHelper.WriteToFile(fis, builder.toString(), localDir);
						fis.close();
						
						Intent intent=new Intent();
					    intent.putExtra("file", f);
						intent.putExtra("name", builder.toString());
						setResult(Activity.RESULT_OK, intent);
						finish();
						return;*/
					}
				}
				
				FileHelper.WriteToFile(fis, file.getName(), localDir);
				fis.close();
				
				Intent intent=new Intent();
			    intent.putExtra("file", file);
				intent.putExtra("name", file.getName());
				setResult(RESULT_OK, intent);
				finish();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}	

	
	private void setupHandlers(){
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				
				switch(msg.what){
				case HttpConnection.CONNECTION_START:
					Log.d("TextAnnotation", "Start");
					break;
				
				case HttpConnection.CONNECTION_SUCCEED:
					Log.d("TextAnnotation", "Succeeded");
					String response = (String) msg.obj;
					TextAnnotationResult text = new Gson().fromJson(response, TextAnnotationResult.class);
					
					int a =0;
					break;
					
				case HttpConnection.CONNECTION_RESPONSE_ERROR:
					Log.e("TextAnnotation", "Failed due to response error");
					break;
					
				case HttpConnection.CONNECTION_ERROR:
					Log.e("TextAnnotation", "Failed due to error");
					break;
					
				}
			}
		};		
		
	}
	
}
