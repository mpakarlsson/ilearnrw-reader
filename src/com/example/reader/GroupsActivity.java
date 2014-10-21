package com.example.reader;

import ilearnrw.user.profile.UserProfile;
import ilearnrw.utils.LanguageCode;

import java.io.IOException;

import com.example.reader.types.ExpandableListAdapter;
import com.example.reader.utils.AppLocales;
import com.example.reader.utils.groups.GroupedRulesFacade;
import com.google.gson.Gson;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

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

		Button suggestion1 = (Button)findViewById(R.id.personal_recomendations_one);
		Button suggestion2 = (Button)findViewById(R.id.personal_recomendations_two);
		initSuggestionButtons(suggestion1, suggestion2);
        
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
	
	private void initSuggestionButtons(Button x, Button y){
		final int p[][] = groupedRules.getSuggestedSubgroups();
		if (p != null && p.length >0){
			x.setText(groupedRules.getProblemGroups().getGroupedProblems().get(p[0][0]).getGroupTitle()+
					" / "+groupedRules.getProblemGroups().getGroupedProblems().get(p[0][0]).
					getSubgroups().get(p[0][1]).getSubgroupTitle());
			x.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(GroupsActivity.this, SubgroupDetails.class);
					intent.putExtra("groupId", p[0][0]);
					intent.putExtra("subgroupId", p[0][1]);
					intent.putExtra("showGUI", true);
					startActivity(intent);
				}
			});
		}
		if (p != null && p.length >1){
			y.setText(groupedRules.getProblemGroups().getGroupedProblems().get(p[1][0]).getGroupTitle()+
					" / "+groupedRules.getProblemGroups().getGroupedProblems().get(p[1][0]).
					getSubgroups().get(p[1][1]).getSubgroupTitle());
			y.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(GroupsActivity.this, SubgroupDetails.class);
					intent.putExtra("groupId", p[1][0]);
					intent.putExtra("subgroupId", p[1][1]);
					intent.putExtra("showGUI", true);
					startActivity(intent);
				}
			});
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
}
