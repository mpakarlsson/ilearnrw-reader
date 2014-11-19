package com.ilearnrw.reader.types;

import java.util.HashSet;
import java.util.Set;

import com.ilearnrw.reader.R;
import com.ilearnrw.reader.utils.groups.Group;
import com.ilearnrw.reader.utils.groups.GroupedRulesFacade;
import com.ilearnrw.reader.utils.groups.Subgroup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
public class ExpandableListAdapter extends BaseExpandableListAdapter{
 
    private Context context;
	private GroupedRulesFacade groupedRules;
	private Set<Integer> expandedGroupIds;
 
    public ExpandableListAdapter(Context context, GroupedRulesFacade groupedRules) {
        this.context 			= context;
        this.groupedRules 		= groupedRules;
        this.expandedGroupIds 	= new HashSet<Integer>();
        
        
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
            convertView = infalInflater.inflate(R.layout.subgroup_child, parent, false);
        }

        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.subgroup_label);        
        int count = groupedRules.getNumSubgroupEnabledItems(groupPosition, childPosition);
        txtListChild.setText(childText);
        
        ImageView iv = (ImageView) convertView.findViewById(R.id.subgroup_image);
        
        if(count>0)
        	iv.setImageResource(R.drawable.rules_active);
        else
        	iv.setImageResource(R.drawable.rules_inactive);
        

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
    
    public Set<Integer> getExpandedGroupIds(){
    	return expandedGroupIds;
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
        int count = groupedRules.getGroupEnabledItems(groupPosition);
        lblListHeader.setText(headerTitle);
        
        ImageView iv = (ImageView) convertView.findViewById(R.id.group_image);
        
        if(count>0)
        	iv.setImageResource(R.drawable.rules_active);
        else
        	iv.setImageResource(R.drawable.rules_inactive);
        
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

    
    
	@Override
	public void onGroupCollapsed(int groupPosition) {
		expandedGroupIds.remove(groupPosition);
		super.onGroupCollapsed(groupPosition);
	}

	@Override
	public void onGroupExpanded(int groupPosition) {
		expandedGroupIds.add(groupPosition);
		super.onGroupExpanded(groupPosition);
	}

}
