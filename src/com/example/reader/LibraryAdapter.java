package com.example.reader;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LibraryAdapter extends ArrayAdapter<LibraryItem> {
	
	private ArrayList<LibraryItem> objects;
	
	public LibraryAdapter(Context context, int textViewResourceId, ArrayList<LibraryItem> objects){
		super(context, textViewResourceId, objects);
		this.objects = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	
		View v = convertView;
		if(v == null){
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.library_row, null);
		}
		
		final LibraryItem item = objects.get(position);
		if(item != null){
			TextView tv_item = (TextView) v.findViewById(R.id.library_item);

			tv_item.setFocusable(false);
			tv_item.setClickable(false);
			
			if(tv_item != null){
				tv_item.setText(item.getName());
			}
		}
		return v;
	}
}
