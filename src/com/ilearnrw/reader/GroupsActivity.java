package com.ilearnrw.reader;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
import ilearnrw.user.profile.UserProfile;
import ilearnrw.utils.LanguageCode;

import java.io.IOException;
import java.util.Set;

import com.ilearnrw.reader.R;
import com.ilearnrw.reader.types.ExpandableLayout;
import com.ilearnrw.reader.types.ExpandableLayout.OnExpandListener;
import com.ilearnrw.reader.types.adapters.ExpandableListAdapter;
import com.ilearnrw.reader.types.singleton.ProfileUser;
import com.ilearnrw.reader.utils.AppLocales;
import com.ilearnrw.reader.utils.groups.GroupedRulesFacade;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageButton;

public class GroupsActivity 
	extends 
		Activity {
	
	private SharedPreferences sp;
	
	private GroupedRulesFacade groupedRules;
	
	private UserProfile profile;
	
	private ExpandableListAdapter listAdapter;
	private ExpandableListView expListView;
	
	public final int GROUPS_EDIT_RULE = 20000;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.groups_view);
		
        sp = PreferenceManager.getDefaultSharedPreferences(this);
		AppLocales.setLocales(getApplicationContext(), sp.getString(getString(R.string.sp_user_language), "en"));
				
		String jsonProfile = sp.getString(getString(R.string.sp_user_profile_json), "");
		
		if(!jsonProfile.isEmpty()){
			initProfile(jsonProfile);
		}
		
		initModules();
		
        Button showAdvanced = (Button) this.findViewById(R.id.show_advanced);
        showAdvanced.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(GroupsActivity.this, ActiveRules.class);
				intent.putExtra("showGUI", true);
				startActivity(intent);
			}
		});  
		
        Button clearAll = (Button) this.findViewById(R.id.clear_rules);
        clearAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				confirmQuestion();
			}
		});        
        
        

        ExpandableLayout expLayoutRecommendations = (ExpandableLayout) findViewById(R.id.expLayoutRecommendations);
        final ImageButton recommendationsImgBtn = (ImageButton) findViewById(R.id.recommendationArrow);
        expLayoutRecommendations.setOnExpandListener(new OnExpandListener() {
			
			@Override
			public void onExpand(View handle, View content) {
				recommendationsImgBtn.setBackground(getResources().getDrawable(R.drawable.arrow_up));
			}
			
			@Override
			public void onCollapse(View handle, View content) {
				recommendationsImgBtn.setBackground(getResources().getDrawable(R.drawable.arrow_down));
			}
		});
        
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
				startActivityForResult(intent, GROUPS_EDIT_RULE);
				return false;
			}
		});
	}
	
	private void initModules(){
        try {
			groupedRules = new GroupedRulesFacade(this ,profile, sp.getInt(getString(R.string.sp_user_id), 0),
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
					startActivityForResult(intent, GROUPS_EDIT_RULE);
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
					startActivityForResult(intent, GROUPS_EDIT_RULE);
				}
			});
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == GROUPS_EDIT_RULE && resultCode == RESULT_OK){
			Set<Integer> expanded = listAdapter.getExpandedGroupIds();
			boolean updated = data.getExtras().getBoolean("updated");
			if(updated){
				initModules();
				
				for(Integer position : expanded)
					expListView.expandGroup(position);
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void initProfile(String jsonProfile){
		profile = ProfileUser.getInstance(this.getApplicationContext()).getProfile();		
	}
	
	public void confirmQuestion() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE: // Yes button clicked
					for (int i = 0; i<groupedRules.getGroupedProblems().size(); i++){
						for (int j = 0; j<groupedRules.getGroupedProblems().get(i).getSubgroups().size(); j++)
							groupedRules.disableAll(i, j);
					}
					groupedRules.getPresentationRulesAdapter().saveModule();
					listAdapter.notifyDataSetChanged();
					break; 
				} 
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.clear_all_question).setPositiveButton(R.string.yes, dialogClickListener)
			.setNegativeButton(R.string.no, dialogClickListener).show();
	}	
}
