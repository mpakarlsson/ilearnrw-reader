package com.example.reader.types;

import com.example.reader.R;
import com.example.reader.utils.groups.Group;
import com.example.reader.utils.groups.GroupedRulesFacade;
import com.example.reader.utils.groups.Subgroup;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
 
public class ExpandableListAdapter extends BaseExpandableListAdapter {
 
    private Context context;
	private GroupedRulesFacade groupedRules;
 
    public ExpandableListAdapter(Context context, GroupedRulesFacade groupedRules) {
        this.context = context;
        this.groupedRules = groupedRules;
    }
 
    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.groupedRules.getGroupedProblems().get(groupPosition).
        		getSubgroups().get(childPosititon);
    }
 
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
 
    @Override
    public View getChildView(int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
 
        final String childText = ((Subgroup) getChild(groupPosition, childPosition)).getSubgroupTitle();
 
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.subgroup_child, null);
        }
 
        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.lblListItem);
        
        String str = " ["+groupedRules.getSubgroupEnabledItems(groupPosition, childPosition)+"]";
 
        txtListChild.setText(childText+str);
        return convertView;
    }
 
    @Override
    public int getChildrenCount(int groupPosition) {
        return this.groupedRules.getGroupedProblems().get(groupPosition).getSubgroups().size();
    }
 
    @Override
    public Object getGroup(int groupPosition) {
        return this.groupedRules.getGroupedProblems().get(groupPosition);
    }
 
    @Override
    public int getGroupCount() {
    	return this.groupedRules.getGroupedProblems().size();
    }
 
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        String headerTitle = ((Group) getGroup(groupPosition)).getGroupTitle();
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_subgroup, null);
        }
 
        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        String str = " ("+groupedRules.getGroupEnabledItems(groupPosition)+")";
        if (groupedRules.getGroupEnabledItems(groupPosition)>0){
        	lblListHeader.setTypeface(null, Typeface.BOLD);
        }
        lblListHeader.setText(headerTitle+str);
 
        return convertView;
    }
 
    @Override
    public boolean hasStableIds() {
        return false;
    }
 
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    
}
