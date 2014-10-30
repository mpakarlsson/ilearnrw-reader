package com.example.reader.types;

import java.util.List;

import com.example.reader.R;

import android.content.Context;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class WordPopupAdapter extends ArrayAdapter<String>{
	private List<String> objects;
	private List<Spannable> items;
	private LayoutInflater inflater;
	
	private ViewHolder viewHolder;
	
	private int colorEven 	= 0xffe7e3d1;
	private int colorOdd	= 0xfff8f6e9;
	
	private boolean colorRows	= false;
	
	private float textSize;
	
	static class ViewHolder{
		public TextView title;
		public TextView data;
	};
	
	public WordPopupAdapter(Context context, int resource, List<String> objects, List<Spannable> items) {
		super(context, resource, objects);
		init(context, objects, items, -1, colorEven, colorOdd, colorRows);
	}
	
	public WordPopupAdapter(Context context, int resource, List<String> objects, List<Spannable> items, int textSize) {
		super(context, resource, objects);
		init(context, objects, items, textSize, colorEven, colorOdd, colorRows);
	}
	
	public WordPopupAdapter(Context context, int resource, List<String> objects, List<Spannable> items, boolean isColoring) {
		super(context, resource, objects);
		init(context, objects, items, -1, colorEven, colorOdd, isColoring);
	}
	
	public WordPopupAdapter(Context context, int resource, List<String> objects, List<Spannable> items, int textSize, boolean isColoring) {
		super(context, resource, objects);
		init(context, objects, items, textSize,  colorEven, colorOdd, isColoring);
	}
	
	public WordPopupAdapter(Context context, int resource, List<String> objects, List<Spannable> items, int color1, int color2) {
		super(context, resource, objects);
		init(context, objects, items, -1, color1, color2, colorRows);
	}
	
	public WordPopupAdapter(Context context, int resource, List<String> objects, List<Spannable> items, int textSize, int color1, int color2) {
		super(context, resource, objects);
		init(context, objects, items, textSize, color1, color2, colorRows);
	}
	
	public WordPopupAdapter(Context context, int resource, List<String> objects, List<Spannable> items, int color1, int color2, boolean isColoring) {
		super(context, resource, objects);
		init(context, objects, items, -1, color1, color2, isColoring);
	}

	public WordPopupAdapter(Context context, int resource, List<String> objects, List<Spannable> items, int textSize, int color1, int color2, boolean isColoring) {
		super(context, resource, objects);
		init(context, objects, items, textSize, color1, color2, isColoring);
	}
	
	private void init(Context context, List<String> objects, List<Spannable> items, int textSize, int color1, int color2, boolean isColoring){
		this.objects 	= objects;
		this.items		= items;
		this.inflater	= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		colorEven		= color1;
		colorOdd		= color2;
		colorRows		= isColoring;
		
		if(textSize == -1)
			this.textSize = context.getResources().getDimension(R.dimen.font_size_default);
		else
			this.textSize = textSize;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return setupView(position, convertView, parent, colorRows);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return setupView(position, convertView, parent, colorRows);
	}

	private View setupView(int position, View convertView, ViewGroup parent, boolean isColoring){
		View view = convertView;
		
		if(view==null){
			view 				= inflater.inflate(R.layout.row_word_popup, null);
			viewHolder 			= new ViewHolder();
			viewHolder.title 	= (TextView) view.findViewById(R.id.tv_item_title);
			viewHolder.data		= (TextView) view.findViewById(R.id.tv_item_info);
			viewHolder.title.setTextSize(textSize);
			view.setTag(viewHolder);
		}
		
		if(isColoring)
			view.setBackgroundColor(position%2==0 ? colorEven : colorOdd);
		
		viewHolder 	= (ViewHolder) view.getTag();
		viewHolder.title.setText(objects.get(position));
		
		viewHolder.data.setText(items.get(position));
		
		return view;
	}
}
