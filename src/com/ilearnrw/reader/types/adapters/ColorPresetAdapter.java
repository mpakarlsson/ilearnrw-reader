package com.ilearnrw.reader.types.adapters;

import java.util.ArrayList;

import com.ilearnrw.reader.R;
import com.ilearnrw.reader.types.Preset;
import com.ilearnrw.reader.utils.Helper;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ColorPresetAdapter extends ArrayAdapter<Preset> {
	private Context context;

	public ColorPresetAdapter(Context context, int resource, ArrayList<Preset> items) {
		super(context, resource, items);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		final Preset item = getItem(position);
		
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.row_default, parent, false);
			
			TextView tv = (TextView) view.findViewById(R.id.tv_default);
			
			int bgColor = Helper.hexToColor(item.getBackgroundColor());
			int txColor = Helper.hexToColor(item.getTextColor());
			CharSequence seq = item.getText();
			SpannableStringBuilder builder = Helper.colorString(seq, new ForegroundColorSpan(txColor), 0, seq.length());
			builder = Helper.colorString(builder, new BackgroundColorSpan(bgColor), 0, seq.length());
			tv.setText(builder);			
		}
		return view;
	}
}
