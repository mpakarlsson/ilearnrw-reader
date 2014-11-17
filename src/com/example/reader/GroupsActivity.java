package com.example.reader;

import ilearnrw.user.profile.UserProfile;
import ilearnrw.utils.LanguageCode;

import java.io.IOException;

import com.example.reader.types.ExpandableLayout;
import com.example.reader.types.ExpandableListAdapter;
import com.example.reader.types.ExpandableLayout.OnExpandListener;
import com.example.reader.types.singleton.ProfileUser;
import com.example.reader.utils.AppLocales;
import com.example.reader.utils.groups.GroupedRulesFacade;

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
	////
	private ExpandableListAdapter listAdapter;
	private ExpandableListView expListView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.groups_view);
		
        sp = PreferenceManager.getDefaultSharedPreferences(this);
		AppLocales.setLocales(getApplicationContext(), sp.getString(getString(R.string.sp_user_language), "en"));

		int id = sp.getInt(getString(R.string.sp_user_id),-1);
		String token = sp.getString(getString(R.string.sp_authToken), "");		
		
		if(id==-1 || token.isEmpty()) {
			//finished(); // If you don't have an id something is terribly wrong
			throw new IllegalArgumentException("Missing id or token");
		}
				
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
				startActivity(intent);
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
		listAdapter.notifyDataSetChanged();
		
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
