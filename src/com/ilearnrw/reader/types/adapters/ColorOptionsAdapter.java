package com.ilearnrw.reader.types.adapters;

import java.util.ArrayList;

import com.ilearnrw.reader.R;
import com.ilearnrw.reader.interfaces.ColorPickerListener;
import com.ilearnrw.reader.interfaces.OnSettingUpdated;
import com.ilearnrw.reader.types.ColorListPopupWindow;
import com.ilearnrw.reader.types.ColorOption;
import com.ilearnrw.reader.types.ColorPickerDialog;
import com.ilearnrw.reader.utils.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.TextView;

public class ColorOptionsAdapter extends ArrayAdapter<ColorOption>{
	private Context context;
	private final SharedPreferences sp;
	private OnSettingUpdated setting;
	private ArrayList<ArrayList<String>> colors;
	
	public ColorOptionsAdapter(Context context, int resource, ArrayList<ColorOption> items, OnSettingUpdated settingUpdated) {
		super(context, resource, items);
		this.context = context;
		sp = PreferenceManager.getDefaultSharedPreferences(context);
		this.setting = settingUpdated;
		
		colors = new ArrayList<ArrayList<String>>();
		colors.add(new ArrayList<String>());
		colors.add(new ArrayList<String>());
		colors.add(new ArrayList<String>());
		
		colors.get(0).add("000000");
		colors.get(0).add("F9966B"); colors.get(0).add("FFF380");
		colors.get(0).add("CCFB5D"); colors.get(0).add("E9CFEC");
		colors.get(0).add("ADD8E6"); colors.get(0).add("FFFFFF");
		
		colors.get(1).add("000000"); colors.get(1).add("FFFFCC");
		colors.get(1).add("7FE817"); colors.get(1).add("E0FFFF");
		colors.get(1).add("F4F4F4"); colors.get(1).add("FFFFFF");
		
		colors.get(2).add("3CB371"); colors.get(2).add("ADD8E6");
		colors.get(2).add("C0C0C0"); colors.get(2).add("CCFB5D");
		colors.get(2).add("FFFFAA"); colors.get(2).add("F9966B");
		colors.get(2).add("E9CFEC"); colors.get(2).add("FFD700");
	}

	@Override
	public View getView(final int position, final View convertView, ViewGroup parent) {
		View v = convertView;
		Holder holder = new Holder();
		final ColorOption item = getItem(position);
		int color = -1;
		String temp = "";
		switch (position) {
		case 0:
			temp = context.getString(R.string.pref_text_color_title);
			color = sp.getInt(temp, Color.argb(255, 0, 0, 0));
			break;
		case 1:
			temp = context.getString(R.string.pref_background_color_title);
			color = sp.getInt(temp, Color.argb(255, 255, 255, 204));
			break;
		case 2: 
			temp = context.getString(R.string.pref_highlight_color_title);
			color = sp.getInt(temp, Color.argb(255, 255, 255, 0));
			break;
		}
		
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.row_color_options, parent, false);
			
			final TextView title 	= (TextView) v.findViewById(R.id.tv_option_title);
			final ImageView colorV = (ImageView) v.findViewById(R.id.iv_option_color);
			final Button advanced = (Button) v.findViewById(R.id.btn_option_advanced);
			holder.tv = title;
			holder.iv = colorV;
			holder.btn = advanced;

			final String prefStr = temp;
			
			holder.iv.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					final ColorListPopupWindow popup = new ColorListPopupWindow(context);
					popup.setParentListPosition(-1);
					popup.setAnchorView(colorV);
					popup.setModal(true);
					popup.setWidth(70);
					popup.setPromptPosition(ListPopupWindow.POSITION_PROMPT_BELOW);
					popup.setAdapter(new ColorListAdapter(context, R.layout.color_presents_row, colors.get(position)));
					
					
					popup.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int pos, long id) {
							popup.dismiss();
							
							String color = colors.get(position).get(pos);
							int c = Helper.hexToColor(Helper.fixHex(color));

							sp.edit().putInt(prefStr, c).apply();
							colorV.setBackgroundColor(c);
							setting.onSettingUpdated(position, c);
						}
					});
					
					popup.show();
				}
			});
			
			holder.btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int color = sp.getInt(prefStr, item.getColor());
					new ColorPickerDialog(getContext(), color, false, new ColorPickerListener() {
						@Override
						public void onOk(ColorPickerDialog dialog, int color) {
							sp.edit().putInt(prefStr, color).apply();
							colorV.setBackgroundColor(color);
							setting.onSettingUpdated(position, color);
						}
						
						@Override
						public void onCancel(ColorPickerDialog dialog) {}
						
					}).show();
				}
			});
			
			v.setTag(holder);			
		} else 
			holder = (Holder) v.getTag();
		
		holder.tv.setText(item.getName());
		holder.iv.setBackgroundColor(color);
		return v;
	}
	
	private static class Holder{
		TextView tv;
		ImageView iv;
		Button btn;
	}
}
