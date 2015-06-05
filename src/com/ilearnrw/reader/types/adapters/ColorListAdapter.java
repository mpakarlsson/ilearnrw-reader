package com.ilearnrw.reader.types.adapters;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.ilearnrw.reader.R;

public class ColorListAdapter extends ArrayAdapter<String> {
    private Context context;

    public ColorListAdapter(Context context, int textViewResourceId, ArrayList<String> items) {
        super(context, textViewResourceId, items);
        this.context = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {    	
        final String item = getItem(position);
        
        if(convertView == null){
        	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	convertView = inflater.inflate(R.layout.color_presents_row, parent, false); 
        } 

        convertView.setBackgroundColor(Integer.parseInt(item, 16) + 0xFF000000);
        return convertView;
    }
}


