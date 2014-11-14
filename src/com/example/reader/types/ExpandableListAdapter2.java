package com.example.reader.types;

import java.util.List;
import java.util.Map;

import com.example.reader.R;
import com.example.reader.utils.groups.Group;
import com.example.reader.utils.groups.GroupedRulesFacade;
import com.example.reader.utils.groups.Subgroup;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class 
		ExpandableListAdapter2 
	extends 
		BaseExpandableListAdapter {

	private Activity context;
	private Map<String, List<String>> problemCollections;
	private List<String> problems;
	private GroupedRulesFacade facade;
	
	public ExpandableListAdapter2(Activity context, GroupedRulesFacade facade){
		this.context 	= context;
		this.facade 	= facade;
	}

	@Override
	public int getGroupCount() {
		return facade.getGroupedProblems().size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return facade.getGroupedProblems().get(groupPosition).getSubgroups().size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return facade.getGroupedProblems().get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return facade.getGroupedProblems().get(groupPosition).getSubgroups().get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		String headerTitle = ((Group) getGroup(groupPosition)).getGroupTitle();
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_subgroup, parent, false);
        }
 
        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.group_label);
        int count = facade.getGroupEnabledItems(groupPosition);
        lblListHeader.setText(headerTitle);
        
        
        ImageView iv = (ImageView) convertView.findViewById(R.id.group_image);
        
        if(count>0)
        	iv.setImageResource(R.drawable.rules_active);
        else
        	iv.setImageResource(R.drawable.rules_inactive);
        
        return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		final String childText = ((Subgroup) getChild(groupPosition, childPosition)).getSubgroupTitle();
		 
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.subgroup_child, parent, false);
        }

        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.subgroup_label);        
        int count = facade.getSubgroupEnabledItems(groupPosition, childPosition);
        txtListChild.setText(childText);
        
        if(count>0)
        	txtListChild.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.rules_active, 0);
        
        ImageView iv = (ImageView) convertView.findViewById(R.id.subgroup_image);
        
        if(count>0)
        	iv.setImageResource(R.drawable.rules_active);
        else
        	iv.setImageResource(R.drawable.rules_inactive);
        

        return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	
	
}
