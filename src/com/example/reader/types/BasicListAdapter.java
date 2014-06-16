package com.example.reader.types;

import java.util.Arrays;
import java.util.List;

import com.example.reader.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BasicListAdapter extends ArrayAdapter<String>{
	private List<String> objects;
	private LayoutInflater inflater;
	
	private ViewHolder viewHolder;
	
	private int colorEven 	= 0xffe7e3d1;
	private int colorOdd	= 0xfff8f6e9;
	
	private boolean colorRows	= false;
	
	private float textSize;
	
	static class ViewHolder{
		public TextView item;
	};
	
	public BasicListAdapter(Context context, int resource, List<String> objects) {
		super(context, resource, objects);
		init(context, objects, -1, colorEven, colorOdd, colorRows);
	}
	
	public BasicListAdapter(Context context, int resource, List<String> objects, int textSize) {
		super(context, resource, objects);
		init(context, objects, textSize, colorEven, colorOdd, colorRows);
	}
	
	public BasicListAdapter(Context context, int resource, List<String> objects, boolean isColoring) {
		super(context, resource, objects);
		init(context, objects, -1, colorEven, colorOdd, isColoring);
	}
	
	public BasicListAdapter(Context context, int resource, List<String> objects, int textSize, boolean isColoring) {
		super(context, resource, objects);
		init(context, objects, textSize,  colorEven, colorOdd, isColoring);
	}
	
	public BasicListAdapter(Context context, int resource, List<String> objects, int color1, int color2) {
		super(context, resource, objects);
		init(context, objects, -1, color1, color2, colorRows);
	}
	
	public BasicListAdapter(Context context, int resource, List<String> objects, int textSize, int color1, int color2) {
		super(context, resource, objects);
		init(context, objects, textSize, color1, color2, colorRows);
	}
	
	public BasicListAdapter(Context context, int resource, List<String> objects, int color1, int color2, boolean isColoring) {
		super(context, resource, objects);
		init(context, objects, -1, color1, color2, isColoring);
	}

	public BasicListAdapter(Context context, int resource, List<String> objects, int textSize, int color1, int color2, boolean isColoring) {
		super(context, resource, objects);
		init(context, objects, textSize, color1, color2, isColoring);
	}
	
	public BasicListAdapter(Context context, int resource, String[] objects){
		super(context, resource, objects);
		init(context, objects, -1, colorEven, colorOdd, colorRows);
	}
	
	public BasicListAdapter(Context context, int resource, String[] objects, int textSize){
		super(context, resource, objects);
		init(context, objects, textSize, colorEven, colorOdd, colorRows);
	}
	
	public BasicListAdapter(Context context, int resource, String[] objects, boolean isColoring){
		super(context, resource, objects);
		init(context, objects, -1, colorEven, colorOdd, isColoring);
	}
	
	public BasicListAdapter(Context context, int resource, String[] objects, int textSize, boolean isColoring){
		super(context, resource, objects);
		init(context, objects, textSize, colorEven, colorOdd, isColoring);
	}
	
	public BasicListAdapter(Context context, int resource, String[] objects, int color1, int color2){
		super(context, resource, objects);
		init(context, objects, -1, color1, color2, colorRows);
	}
	
	public BasicListAdapter(Context context, int resource, String[] objects, int textSize, int color1, int color2){
		super(context, resource, objects);
		init(context, objects, textSize, color1, color2, colorRows);
	}
	
	public BasicListAdapter(Context context, int resource, String[] objects, int color1, int color2, boolean isColoring){
		super(context, resource, objects);
		init(context, objects, -1, color1, color2, isColoring);
	}
	
	public BasicListAdapter(Context context, int resource, String[] objects, int textSize, int color1, int color2, boolean isColoring){
		super(context, resource, objects);
		init(context, objects, textSize, color1, color2, isColoring);
	}
	
	private void init(Context context, List<String> objects, int textSize, int color1, int color2, boolean isColoring){
		this.objects 	= objects;
		this.inflater	= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		colorEven		= color1;
		colorOdd		= color2;
		colorRows		= isColoring;
		
		if(textSize == -1)
			this.textSize = context.getResources().getDimension(R.dimen.font_size_default);
		else
			this.textSize = textSize;
	}
	
	private void init(Context context, String[] objects, int textSize, int color1, int color2, boolean isColoring){
		this.objects 	= Arrays.asList(objects);;
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
		return setupView(position, convertView, parent, true);
	}


	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return setupView(position, convertView, parent, true);
	}

	
	private View setupView(int position, View convertView, ViewGroup parent, boolean isDropDown){
		View view = convertView;
		
		if(view==null){
			view 			= inflater.inflate(R.layout.textview_item_multiline, null);
			viewHolder 		= new ViewHolder();
			viewHolder.item = (TextView) view.findViewById(R.id.tv_spinner_item);
			viewHolder.item.setTextSize(textSize);
			view.setTag(viewHolder);
		}
		
		if(isDropDown)
			view.setBackgroundColor(position%2==0 ? colorEven : colorOdd);
		
		viewHolder 	= (ViewHolder) view.getTag();
		String item = objects.get(position);
		viewHolder.item.setText(item);
		return view;
	}
}
