package com.example.reader.types;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import com.example.reader.ActiveRules;
import com.example.reader.PresentationModule;
import com.example.reader.R;
import com.example.reader.utils.FileHelper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class LibraryAdapter extends ArrayAdapter<LibraryItem> implements SectionIndexer{
	
	public static final String TAG = LibraryAdapter.class.getCanonicalName();
	
	private ArrayList<LibraryItem> objects;
	private HashMap<String, Integer> alphaIndexer;
	String[] sections;
	
	public LibraryAdapter(Context context, int textViewResourceId, ArrayList<LibraryItem> objects){
		super(context, textViewResourceId, objects);
		this.objects = objects;
		
		alphaIndexer = new HashMap<String, Integer>();
		
		for(int i=0; i<objects.size(); i++){
			LibraryItem item = objects.get(i);
			
			String ch = item.getName().substring(0,1);
			ch = ch.toUpperCase(Locale.getDefault());
			
			if(!alphaIndexer.containsKey(ch))
				alphaIndexer.put(ch, i);
		}
		
		Set<String> sectionLetters = alphaIndexer.keySet();
		
		ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);
		Collections.sort(sectionList);
		sections = new String[sectionList.size()];
		sectionList.toArray(sections);/**/
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if(v == null){
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.row_library, null);
		}
		
		final LibraryItem item = objects.get(position);
		if(item != null){
			
			final Pair<File> libItems = FileHelper.getFilesFromLibrary(getContext(), item.getName());
			
			TextView tv_item = (TextView) v.findViewById(R.id.library_item);
			
			Button rules = (Button) v.findViewById(R.id.library_item_rules);
			tv_item.setFocusable(false);
			tv_item.setClickable(false);
			
			rules.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(item.getName().endsWith(".txt") || item.getName().endsWith(".html")){
						Intent intent = new Intent(getContext(), ActiveRules.class);
						intent.putExtra("file", libItems.first());
						intent.putExtra("json", libItems.second());
						intent.putExtra("title", libItems.first().getName());
						intent.putExtra("loadFiles", true);
						intent.putExtra("showGUI", true);
						getContext().startActivity(intent);
					} else if(item.getName().endsWith(".json")){
						jsonClicked();
					}
					else {
						invalidClicked(item.getName());
					}
				}
			});
			
			if(tv_item != null){
				String name = item.getName();
				
				if(name.endsWith(".txt"))
					name = name.substring(0,name.length()-4);
				tv_item.setText(name);
			}
		}
		return v;
	}

	private void jsonClicked(){
		new AlertDialog.Builder(getContext())
		.setTitle(getContext().getString(R.string.dialog_json_title))
		.setMessage(getContext().getString(R.string.dialog_json_message))
		.setPositiveButton(android.R.string.ok, null).show();
	}
	
	private void invalidClicked(String name){
		new AlertDialog.Builder(getContext())
		.setTitle(getContext().getString(R.string.folder_invalid))
		.setMessage(getContext().getString(R.string.folder_open_failed) + name)
		.setPositiveButton(android.R.string.ok, null).show();
	}
	
	@Override
	public void notifyDataSetChanged() {
		alphaIndexer.clear();
		
		for(int i=0; i<objects.size(); i++){
			LibraryItem item = objects.get(i);
			
			String ch = item.getName().substring(0,1);
			ch = ch.toUpperCase(Locale.getDefault());
			
			if(!alphaIndexer.containsKey(ch))
				alphaIndexer.put(ch, i);
		}
		
		Set<String> sectionLetters = alphaIndexer.keySet();
		
		ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);
		Collections.sort(sectionList);
		sections = new String[sectionList.size()];
		sectionList.toArray(sections);
		
		super.notifyDataSetChanged();
	}

	@Override
	public int getPositionForSection(int sectionIndex) {
		if(sectionIndex>=sections.length)
			sectionIndex =  sections.length-1;
		else if(sectionIndex<0)
			sectionIndex=0;
		
		String section = sections[sectionIndex];
		Integer index = alphaIndexer.get(section);
		return index;
	}

	@Override
	public int getSectionForPosition(int position) {
		return 1;
	}

	@Override
	public Object[] getSections() {
		return sections;
	}
}
