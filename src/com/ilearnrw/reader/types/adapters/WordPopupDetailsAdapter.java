package com.ilearnrw.reader.types.adapters;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
import java.util.ArrayList;
import java.util.TreeSet;

import com.ilearnrw.reader.R;

import android.content.Context;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class WordPopupDetailsAdapter extends BaseAdapter{

	private static final int TYPE_ITEM = 0;
	private static final int TYPE_HEADER = 1;
	
	private ArrayList<Spannable> data = new ArrayList<Spannable>();
	private ArrayList<String> descriptions = new ArrayList<String>();
	private TreeSet<Integer> sectionHeader = new TreeSet<Integer>();
	
	private LayoutInflater inflater;
	
	public WordPopupDetailsAdapter(Context context){
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void addItem(final Spannable item){
		data.add(item);
		descriptions.add("");
		notifyDataSetChanged();		
	}
	
	public void addItem(final Spannable item, final String description){
		data.add(item);
		descriptions.add(description);
		notifyDataSetChanged();
	}
	
	public void addSectionHeader(final Spannable item){
		data.add(item);
		descriptions.add("");
		sectionHeader.add(data.size()-1);
		notifyDataSetChanged();
	}
	
	@Override
	public int getItemViewType(int position) {
		return sectionHeader.contains(position) ? TYPE_HEADER : TYPE_ITEM;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		int rowType = getItemViewType(position);
		
		if(convertView == null){
			holder = new ViewHolder();
			switch (rowType) {
			case TYPE_ITEM:
				convertView = inflater.inflate(R.layout.row_word_popup_item, parent, false);
				holder.title = (TextView) convertView.findViewById(R.id.tv_wp_item_title);
				holder.info = (TextView) convertView.findViewById(R.id.tv_wp_item_info);
				break;

			case TYPE_HEADER:
				convertView = inflater.inflate(R.layout.row_word_popup_heading, parent, false);
				holder.title = (TextView) convertView.findViewById(R.id.tv_wp_heading);
				holder.info = null;
				break;
			}
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		Spannable info 		= data.get(position);
		String description 	= descriptions.get(position);
		
		if(holder.info==null)
			holder.title.setText(info);
		else {			
			holder.title.setText(info);
			holder.info.setText(description);
		}
		
		return convertView;
	}
	
	public static class ViewHolder {
		public TextView title;
		public TextView info;
	}

}
