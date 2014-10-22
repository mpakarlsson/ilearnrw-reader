package com.example.reader;

import ilearnrw.user.profile.UserProfile;
import ilearnrw.utils.LanguageCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TooManyListenersException;

import com.example.reader.interfaces.ColorPickerListener;
import com.example.reader.types.ColorPickerDialog;
import com.example.reader.utils.AppLocales;
import com.example.reader.utils.groups.AnnotationItem;
import com.example.reader.utils.groups.GroupedRulesFacade;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SubgroupDetails extends ListActivity implements OnClickListener {
	private UserProfile profile;
	private SharedPreferences sp;

	private ImageView colorBox;
	private Gson gson;
	private SubgroupProblemsAdapter listAdapter;
	private ArrayList<AnnotationItem> subgroupProblems;
	private GroupedRulesFacade groupedRules;
	private int groupId, subgroupId;
	private int currentCategoryPos, currentProblemPos, defaultColour;
	private RadioGroup rulesGroup;
	private RadioButton rbtnHow1, rbtnHow2, rbtnWhat1, rbtnWhat2;
	private ToggleButton rb;
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
		
		try {
			groupedRules = new GroupedRulesFacade(profile, sp.getInt("id", 0), sp, 
					getAssets().open(profile.getLanguage() == LanguageCode.EN?"uk.json":"gr.json"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		//groups = pg.getGroupedProblems();
        TextView groupSubgroupTitle = (TextView)findViewById(R.id.group_subgroup_title);
        groupSubgroupTitle.setText(groupedRules.getGroupedProblems().get(groupId).getGroupTitle()+
        		" / "+groupedRules.getGroupedProblems().get(groupId).getSubgroups().get(subgroupId).getSubgroupTitle());
        currentCategoryPos = groupedRules.getGroupedProblems().get(groupId).getSubgroups().get(subgroupId).getItems().get(0).getCategory();
        currentProblemPos = groupedRules.getGroupedProblems().get(groupId).getSubgroups().get(subgroupId).getItems().get(0).getIndex();
        String colourString = groupedRules.getGroupedProblems().get(groupId).getSubgroups().get(subgroupId).getItems().get(0).getDefaultColourHEX();
		colorBox = (ImageView) findViewById(R.id.subgroup_apply_color);
		defaultColour = Integer.parseInt(colourString, 16)+0xFF000000;
		colorBox.setBackgroundColor(defaultColour);
		colorBox.setOnClickListener(this);
		
		rb = (ToggleButton)findViewById(R.id.enable_all_radio);
		rb.setOnClickListener(this);
		if (groupedRules.allEnabled(groupId, subgroupId))
			rb.setChecked(true);

		Button ok = (Button)findViewById(R.id.subgroup_btn_ok);
		ok.setOnClickListener(this);
		
		Button cancel = (Button)findViewById(R.id.subgroup_btn_cancel);
		cancel.setOnClickListener(this);

		rulesGroup 	= (RadioGroup) findViewById(R.id.subgroup_rules);
		rbtnHow1 	= (RadioButton) findViewById(R.id.subgroup_option_how_highlight1);
		rbtnHow2 	= (RadioButton) findViewById(R.id.subgroup_option_how_highlight2);
		rbtnWhat1 	= (RadioButton) findViewById(R.id.subgroup_option_what_highlight1);
		rbtnWhat2 	= (RadioButton) findViewById(R.id.subgroup_option_what_highlight2);
		updateRule();
		subgroupProblems = groupedRules.getGroupedProblems().get(groupId).getSubgroups().get(subgroupId).getItems();
		listAdapter = new SubgroupProblemsAdapter(this, R.layout.row_subgroup_details, subgroupProblems);
		setListAdapter(listAdapter);
	}
	
	private int currentRule(){
		if (rbtnHow2.isChecked() && rbtnWhat1.isChecked())
			return 1;
		if (rbtnHow2.isChecked() && rbtnWhat2.isChecked())
			return 2;
		if (rbtnHow1.isChecked() && rbtnWhat1.isChecked())
			return 3;
		return 4;
	}
	
	private void updateRule(){
		int rule = groupedRules.getPresentationStyle(groupId, subgroupId);
		switch(rule){
		case 1:
			rbtnHow2.setChecked(true);
			rbtnWhat1.setChecked(true);
			break;
		case 2:
			rbtnHow2.setChecked(true);
			rbtnWhat2.setChecked(true);
			break;
		case 3:
			rbtnHow1.setChecked(true);
			rbtnWhat1.setChecked(true);
			break;
		case 4:
			rbtnHow1.setChecked(true);
			rbtnWhat2.setChecked(true);
			break;
		}
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		
	case R.id.subgroup_apply_color:
		int color = sp.getInt(sp.getInt("id", 0)+"pm_color_" + currentCategoryPos + "_" + currentProblemPos, defaultColour);
		ColorPickerDialog dialog = new ColorPickerDialog(this, color, new ColorPickerListener() {
			@Override
			public void onOk(ColorPickerDialog dialog, int color) {
				//sp.edit().putInt("pm_color_" + currentCategoryPos + "_" + currentProblemPos, color).commit();
				//updateColor(currentCategoryPos, currentProblemPos);
				defaultColour = color;
				colorBox.setBackgroundColor(defaultColour);
				for (AnnotationItem ai : groupedRules.getGroupedProblems().get(groupId).getSubgroups().get(subgroupId).getItems()){
					ai.setDefaultColour(Integer.toHexString(defaultColour).substring(2));
					groupedRules.setColour(groupId, subgroupId, defaultColour);
				}
				listAdapter.notifyDataSetChanged();
			}
			
			@Override
			public void onCancel(ColorPickerDialog dialog) {}
		});
		dialog.show();
		break;
		
	case R.id.enable_all_radio:
		if (!groupedRules.allEnabled(groupId, subgroupId)){
			groupedRules.enableAll(groupId, subgroupId);
		}
		else 
			groupedRules.disableAll(groupId, subgroupId);
		listAdapter.notifyDataSetChanged();
		break;
		
	case R.id.subgroup_btn_ok:
		if (groupedRules.getTotalNumberOfActiveRules()<6){
			groupedRules.setPresentationStyle(groupId, subgroupId,currentRule());
			groupedRules.getPresentationRulesAdapter().saveModule();
			onBackPressed();
		}
		else {
			Toast.makeText(getApplicationContext(), R.string.error_on_rules_number, Toast.LENGTH_LONG).show();
		}
		break;
		
	case R.id.subgroup_btn_cancel:
		onBackPressed();
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
	            final ToggleButton isEnabled = (ToggleButton) view.findViewById(R.id.enable_radio);

	            if (itemView != null) {
	                itemView.setText(profile.getUserProblems().getProblemDescription(item.getCategory(), item.getIndex()).getHumanReadableDescription());
	                if (groupedRules.getPresentationRulesAdapter().getActivated(item.getCategory(), item.getIndex())){
	                	activeColorView.setBackgroundColor(groupedRules.getPresentationRulesAdapter().getHighlightingColor(
	                			item.getCategory(), item.getIndex()));
	                }
	                else{
	                	groupedRules.getPresentationRulesAdapter().setHighlightingColor(item.getCategory(), item.getIndex(), 
	                			Integer.parseInt(item.getDefaultColourHEX(), 16)+0xFF000000);
	                	activeColorView.setBackgroundColor(Integer.parseInt(item.getDefaultColourHEX(), 16)+0xFF000000);
	                }
	                isEnabled.setChecked(groupedRules.getPresentationRulesAdapter().getActivated(item.getCategory(), item.getIndex()));
	            }
	            activeColorView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						ColorPickerDialog dialog = new ColorPickerDialog(context, (Integer.parseInt(item.getDefaultColourHEX(), 16)+0xFF000000), new ColorPickerListener() {
							@Override
							public void onOk(ColorPickerDialog dialog, int color) {
								item.setDefaultColour(Integer.toHexString(color).substring(2));
								groupedRules.getPresentationRulesAdapter().
									setHighlightingColor(item.getCategory(), item.getIndex(), color);
								listAdapter.notifyDataSetChanged();
							}							
							@Override
							public void onCancel(ColorPickerDialog dialog) {}
						});
						dialog.show();
						
					}
				});
	            isEnabled.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						groupedRules.getPresentationRulesAdapter().setHighlightingColor(item.getCategory(), item.getIndex(), 
								Integer.parseInt(item.getDefaultColourHEX(), 16)+0xFF000000);
						groupedRules.getPresentationRulesAdapter().setActivated(item.getCategory(), item.getIndex(), 
								isEnabled.isChecked());		
						if (groupedRules.allEnabled(groupId, subgroupId))
							rb.setChecked(true);
						else 
							rb.setChecked(false);
					}
				});
	         }

	        return view;
	    }
	}
}
