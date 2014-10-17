package com.example.reader;

import ilearnrw.user.profile.UserProfile;
import ilearnrw.utils.LanguageCode;

import java.io.IOException;
import java.util.ArrayList;

import com.example.reader.types.ExpandableListAdapter;
import com.example.reader.utils.AppLocales;
import com.example.reader.utils.groups.Group;
import com.example.reader.utils.groups.GroupedRulesFacade;
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
	private GroupedRulesFacade groupedRules;
	
	private UserProfile profile;
	////
	private ExpandableListAdapter listAdapter;
	private ExpandableListView expListView;
	
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
        
        initModules();
        
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
	
	private void initModules(){
        try {
			groupedRules = new GroupedRulesFacade(profile, sp.getInt("id", 0), sp, 
					getAssets().open(profile.getLanguage() == LanguageCode.EN?"uk.json":"gr.json"));
			// get the listview
	        expListView = (ExpandableListView) findViewById(R.id.lvExp);
	  
	        listAdapter = new ExpandableListAdapter(this, groupedRules);
	 
	        // setting list adapter
	        expListView.setAdapter(listAdapter);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void onResume(){
		super.onResume();
		initModules();
		listAdapter.notifyDataSetChanged();
		
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

}
