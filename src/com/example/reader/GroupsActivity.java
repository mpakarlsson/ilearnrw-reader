package com.example.reader;

import ilearnrw.user.profile.UserProfile;
import ilearnrw.utils.LanguageCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.reader.types.ExpandableListAdapter;
import com.example.reader.utils.AppLocales;
import com.example.reader.utils.groups.Group;
import com.example.reader.utils.groups.ProblemGroups;
import com.example.reader.utils.groups.ProblemGroupsFactory;
import com.example.reader.utils.groups.Subgroup;
import com.google.gson.Gson;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

public class GroupsActivity extends Activity {
	private SharedPreferences sp;
	
	private Gson gson;
	private ArrayList<Group> groups;
	
	private UserProfile profile;
	////
	ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<Group> listDataHeader;
    HashMap<Group, List<Subgroup>> listDataChild;
	////
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.groups_view);
		
        sp = PreferenceManager.getDefaultSharedPreferences(this);
		AppLocales.setLocales(getApplicationContext(), sp.getString("language", "en"));
				
		gson = new Gson();

		int id = sp.getInt("id",-1);
		String token = sp.getString("authToken", "");		
		
		if(id==-1 || token.isEmpty()) {
			//finished(); // If you don't have an id something is terribly wrong
			throw new IllegalArgumentException("Missing id or token");
		}
				
		String jsonProfile = sp.getString("json_profile", "");
		
		if(!jsonProfile.isEmpty()){
			initProfile(jsonProfile);
		}
		
        Button add = (Button) this.findViewById(R.id.show_advanced);
        add.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(GroupsActivity.this, ActiveRules.class);
				intent.putExtra("showGUI", true);
				startActivity(intent);
			}
		});        
        
		ProblemGroups pg = null;
		try {
			pg = new ProblemGroupsFactory().getLanguageGroups(profile.getLanguage(), 
					getAssets().open(profile.getLanguage() == LanguageCode.EN?"uk.json":"gr.json"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		groups = pg.getGroupedProblems();
		//listAdapter = new GroupListAdapter(this, R.layout.row_groups, groups);
		//setListAdapter(listAdapter);
		
        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.lvExp);
 
        // preparing list data
        prepareListData();
 
        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
 
        // setting list adapter
        expListView.setAdapter(listAdapter);
        expListView.setOnChildClickListener(new OnChildClickListener() {			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				Intent intent = new Intent(GroupsActivity.this, SubgroupDetails.class);
				intent.putExtra("groupId", groupPosition);
				intent.putExtra("subgroupId", childPosition);
				intent.putExtra("showGUI", true);
				startActivity(intent);
				return false;
			}
		});
	}
	
	public void onResume(){
		super.onResume();
	}
	
	private void initProfile(String jsonProfile){
		profile = gson.fromJson(jsonProfile, UserProfile.class);				
	}
	
	public class GroupListAdapter extends ArrayAdapter<Group> {

	    private Context context;

	    public GroupListAdapter(Context context, int textViewResourceId, ArrayList<Group> items) {
	        super(context, textViewResourceId, items);
	        this.context = context;
	    }

	    public View getView(int position, View convertView, ViewGroup parent) {
	        View view = convertView;
	        if (view == null) {
	            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            view = inflater.inflate(R.layout.row_groups, null);
	        }

	        final Group item = getItem(position);
	        if (item!= null) {				
	            TextView itemView = (TextView) view.findViewById(R.id.group_label);
	            if (itemView != null) {
	                itemView.setText(item.getGroupTitle());
	            }
	         }

	        return view;
	    }
	}
    private void prepareListData() {
        listDataHeader = new ArrayList<Group>();
        listDataChild = new HashMap<Group, List<Subgroup>>();
 
        for (Group g : groups){
            listDataHeader.add(g);
            listDataChild.put(g, g.getSubgroups());
        }
    }

}
