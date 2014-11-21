package com.ilearnrw.reader.types;

import java.util.List;

import com.ilearnrw.reader.R;
import com.ilearnrw.reader.utils.FileHelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ExplorerListAdapter extends ArrayAdapter<ExplorerItem>{
	private List<ExplorerItem> objects;
	private LayoutInflater inflater;
	
	private ViewHolder viewHolder;
	
	private int colorEven 	= 0xffe7e3d1;
	private int colorOdd	= 0xfff8f6e9;
	
	private boolean colorRows	= false;
	
	private float textSize;
	
	static class ViewHolder{
		public TextView item;
		public TextView size;
		public TextView type;
	};
	
	public ExplorerListAdapter(Context context, int resource, List<ExplorerItem> objects) {
		super(context, resource, objects);
		init(context, objects, -1, colorEven, colorOdd, colorRows);
	}
	
	public ExplorerListAdapter(Context context, int resource, List<ExplorerItem> objects, int textSize) {
		super(context, resource, objects);
		init(context, objects, textSize, colorEven, colorOdd, colorRows);
	}
	
	public ExplorerListAdapter(Context context, int resource, List<ExplorerItem> objects, boolean isColoring) {
		super(context, resource, objects);
		init(context, objects, -1, colorEven, colorOdd, isColoring);
	}
	
	public ExplorerListAdapter(Context context, int resource, List<ExplorerItem> objects, int textSize, boolean isColoring) {
		super(context, resource, objects);
		init(context, objects, textSize,  colorEven, colorOdd, isColoring);
	}
	
	public ExplorerListAdapter(Context context, int resource, List<ExplorerItem> objects, int color1, int color2) {
		super(context, resource, objects);
		init(context, objects, -1, color1, color2, colorRows);
	}
	
	public ExplorerListAdapter(Context context, int resource, List<ExplorerItem> objects, int textSize, int color1, int color2) {
		super(context, resource, objects);
		init(context, objects, textSize, color1, color2, colorRows);
	}
	
	public ExplorerListAdapter(Context context, int resource, List<ExplorerItem> objects, int color1, int color2, boolean isColoring) {
		super(context, resource, objects);
		init(context, objects, -1, color1, color2, isColoring);
	}

	public ExplorerListAdapter(Context context, int resource, List<ExplorerItem> objects, int textSize, int color1, int color2, boolean isColoring) {
		super(context, resource, objects);
		init(context, objects, textSize, color1, color2, isColoring);
	}
	
	private void init(Context context, List<ExplorerItem> objects, int textSize, int color1, int color2, boolean isColoring){
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
			view 			= inflater.inflate(R.layout.row_explorer_item, parent, false);
			viewHolder 		= new ViewHolder();
			viewHolder.item = (TextView) view.findViewById(R.id.tv_item);
			viewHolder.item.setTextSize(textSize);
			viewHolder.size = (TextView) view.findViewById(R.id.tv_size);
			viewHolder.type = (TextView) view.findViewById(R.id.tv_type);
			
			view.setTag(viewHolder);
		}
		
		if(isColoring)
			view.setBackgroundColor(position%2==0 ? colorEven : colorOdd);
		
		viewHolder 	= (ViewHolder) view.getTag();
		ExplorerItem item = objects.get(position);
		viewHolder.item.setText(item.getItem());
		
		String fileSize = item.getSize()==0 ? "Unknown" : FileHelper.getReadableFileSize(item.getSize());
		viewHolder.size.setText(fileSize);
		viewHolder.type.setText(item.getType().getName());
		return view;
	}
}
