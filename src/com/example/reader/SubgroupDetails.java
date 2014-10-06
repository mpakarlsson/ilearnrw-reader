package com.example.reader;

import ilearnrw.user.profile.UserProfile;
import ilearnrw.utils.LanguageCode;

import java.io.IOException;
import java.util.ArrayList;

import com.example.reader.interfaces.ColorPickerListener;
import com.example.reader.types.ColorPickerDialog;
import com.example.reader.types.PresentationRulesAdapter;
import com.example.reader.utils.AppLocales;
import com.example.reader.utils.groups.AnnotationItem;
import com.example.reader.utils.groups.Group;
import com.example.reader.utils.groups.ProblemGroups;
import com.example.reader.utils.groups.ProblemGroupsFactory;
import com.google.gson.Gson;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SubgroupDetails extends ListActivity implements OnClickListener {
	private UserProfile profile;
	private SharedPreferences sp;

	private ImageView colorBox;
	private LinearLayout colorLayout;
	private SubgroupProblemsAdapter listAdapter;
	private ArrayList<AnnotationItem> subgroupProblems;
	private Gson gson;
	private ArrayList<Group> groups;
	private int groupId, subgroupId;
	private int currentCategoryPos, currentProblemPos, defaultColour;
	private PresentationRulesAdapter presentationRules;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_subgroup_details);
		Bundle groupExtras = getIntent().getExtras();

		groupId = groupExtras.getInt("groupId");
		subgroupId = groupExtras.getInt("subgroupId");
		
		
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		AppLocales.setLocales(getApplicationContext(), sp.getString("language", "en"));
				
		gson = new Gson();

		int id = sp.getInt("id",-1);
		String token = sp.getString("authToken", "");		
		
		if(id==-1 || token.isEmpty()) {
			throw new IllegalArgumentException("Missing id or token");
		}
				
		String jsonProfile = sp.getString("json_profile", "");
		
		if(!jsonProfile.isEmpty()){
			profile = gson.fromJson(jsonProfile, UserProfile.class);
		}
		
		ProblemGroups pg = null;
		try {
			pg = new ProblemGroupsFactory().getLanguageGroups(profile.getLanguage(), 
					getAssets().open(profile.getLanguage() == LanguageCode.EN?"uk.json":"gr.json"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		groups = pg.getGroupedProblems();
        TextView itemView = (TextView)findViewById(R.id.group_label);
        itemView.setText(groups.get(groupId).getGroupTitle());
        TextView itemView2 = (TextView)findViewById(R.id.subgroup_label);
        itemView2.setText(groups.get(groupId).getSubgroups().get(subgroupId).getSubgroupTitle());
        currentCategoryPos = groups.get(groupId).getSubgroups().get(subgroupId).getItems().get(0).getCategory();
        currentProblemPos = groups.get(groupId).getSubgroups().get(subgroupId).getItems().get(0).getIndex();
        String colourString = groups.get(groupId).getSubgroups().get(subgroupId).getItems().get(0).getDefaultColourHEX();
		colorBox = (ImageView) findViewById(R.id.subgroup_color);
		defaultColour = Integer.parseInt(colourString, 16)+0xFF000000;
		colorBox.setBackgroundColor(defaultColour);
		
		Button t = (Button)findViewById(R.id.subgroup_btn_ok);
		t.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				presentationRules.saveModule();
				Toast.makeText(getApplicationContext(), "text", Toast.LENGTH_LONG).show();
				
			}
		});
		
		colorLayout = (LinearLayout) findViewById(R.id.subgroup_color_layout);
		
		colorLayout.setOnClickListener(this);
		
		presentationRules = new PresentationRulesAdapter(profile, sp.getInt("id", 0), sp);
		subgroupProblems = groups.get(groupId).getSubgroups().get(subgroupId).getItems();
		listAdapter = new SubgroupProblemsAdapter(this, R.layout.row_subgroup_details, subgroupProblems);
		setListAdapter(listAdapter);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
			
		case R.id.subgroup_color_layout:
			int color = sp.getInt(sp.getInt("id", 0)+"pm_color_" + currentCategoryPos + "_" + currentProblemPos, defaultColour);
			ColorPickerDialog dialog = new ColorPickerDialog(this, color, new ColorPickerListener() {
				@Override
				public void onOk(ColorPickerDialog dialog, int color) {
					//sp.edit().putInt("pm_color_" + currentCategoryPos + "_" + currentProblemPos, color).commit();
					//updateColor(currentCategoryPos, currentProblemPos);
					defaultColour = color;
					colorBox.setBackgroundColor(defaultColour);
					for (AnnotationItem ai : groups.get(groupId).getSubgroups().get(subgroupId).getItems()){
						ai.setDefaultColour(Integer.toHexString(defaultColour).substring(2));
					}
					listAdapter.notifyDataSetChanged();
				}
				
				@Override
				public void onCancel(ColorPickerDialog dialog) {}
			});
			dialog.show();
			break;
		
		}
	};
	
	
	public class SubgroupProblemsAdapter extends ArrayAdapter<AnnotationItem> {

	    private Context context;

	    public SubgroupProblemsAdapter(Context context, int textViewResourceId, ArrayList<AnnotationItem> items) {
	        super(context, textViewResourceId, items);
	        this.context = context;
	    }

	    public View getView(int position, View convertView, ViewGroup parent) {
	        View view = convertView;
	        if (view == null) {
	            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            view = inflater.inflate(R.layout.row_subgroup_details, null);
	        }

	        final AnnotationItem item = getItem(position);
	        if (item!= null) {
	            TextView itemView = (TextView) view.findViewById(R.id.label_problem_description);
	            ImageView activeColorView = (ImageView) view.findViewById(R.id.subgrgoup_active_color);

	            if (itemView != null) {
	                itemView.setText(profile.getUserProblems().getProblemDescription(item.getCategory(), item.getIndex()).getHumanReadableDescription());
	                if (presentationRules.getActivated(item.getCategory(), item.getIndex())){
	                	activeColorView.setBackgroundColor(presentationRules.getHighlightingColor(
	                			item.getCategory(), item.getIndex()));
	                }
	                else
	                	activeColorView.setBackgroundColor(Integer.parseInt(item.getDefaultColourHEX(), 16)+0xFF000000);
	            }
	            activeColorView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						ColorPickerDialog dialog = new ColorPickerDialog(context, (Integer.parseInt(item.getDefaultColourHEX(), 16)+0xFF000000), new ColorPickerListener() {
							@Override
							public void onOk(ColorPickerDialog dialog, int color) {
								item.setDefaultColour(Integer.toHexString(color).substring(2));
								presentationRules.setHighlightingColor(item.getCategory(), item.getIndex(), color);
								presentationRules.setActivated(item.getCategory(), item.getIndex(), true);
								listAdapter.notifyDataSetChanged();
							}							
							@Override
							public void onCancel(ColorPickerDialog dialog) {}
						});
						dialog.show();
						
					}
				});
	         }

	        return view;
	    }
	}
}
