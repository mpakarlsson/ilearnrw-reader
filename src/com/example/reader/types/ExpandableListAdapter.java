package com.example.reader.types;

import com.example.reader.R;
import com.example.reader.utils.groups.Group;
import com.example.reader.utils.groups.GroupedRulesFacade;
import com.example.reader.utils.groups.Subgroup;

import android.content.Context;
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
                .findViewById(R.id.subgroup_label);        
        int active = groupedRules.getSubgroupEnabledItems(groupPosition, childPosition);
        if (active>0)
        	txtListChild.setText(childText+" *("+active+")");
        else
        	txtListChild.setText(childText);

        TextView sublabel = (TextView) convertView
                .findViewById(R.id.subgroub_sublabel);
        if (active == 0)
        	sublabel.setText(context.getResources().getString(R.string.no_rules_activated));
        else if (active == 1)
           	sublabel.setText(active+" "+context.getResources().getString(R.string.activated_rule));
        else 
           	sublabel.setText(active+" "+context.getResources().getString(R.string.activated_rules));

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
                .findViewById(R.id.group_label);
        if (groupedRules.getGroupEnabledItems(groupPosition)>0)
        	lblListHeader.setText(headerTitle+" *("+groupedRules.getGroupEnabledItems(groupPosition)+")");
        else 
        	lblListHeader.setText(headerTitle);
 
        TextView sublabel = (TextView) convertView
                .findViewById(R.id.groub_sublabel);
        String str = groupedRules.getGroupedProblems().get(groupPosition).getSubgroups().size()+" ";
        sublabel.setText(str+context.getResources().getString(R.string.sublists));
 
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
