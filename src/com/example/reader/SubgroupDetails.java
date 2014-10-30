package com.example.reader;

import ilearnrw.user.profile.UserProfile;
import ilearnrw.utils.LanguageCode;

import java.io.IOException;
import java.util.ArrayList;

import com.example.reader.interfaces.ColorPickerListener;
import com.example.reader.types.ColorPickerDialog;
import com.example.reader.types.singleton.ProfileUser;
import com.example.reader.utils.AppLocales;
import com.example.reader.utils.groups.AnnotationItem;
import com.example.reader.utils.groups.GroupedRulesFacade;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SubgroupDetails extends ListActivity implements OnClickListener {
	private UserProfile profile;
	private SharedPreferences sp;

	private ImageView colorBox;
	private SubgroupProblemsAdapter listAdapter;
	private ArrayList<AnnotationItem> subgroupProblems;
	private GroupedRulesFacade groupedRules;
	private int groupId, subgroupId;
	private int currentCategoryPos, currentProblemPos, defaultColour;
	private RadioButton rbtnHow1, rbtnHow2, rbtnWhat1, rbtnWhat2;
	private ToggleButton rb;
	
	private ColorListPopupWindow listPopupWindow;
	ArrayList<String> colours = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_subgroup_details);
		Bundle groupExtras = getIntent().getExtras();
		
		colours.add("3CB371"); colours.add("ADD8E6");
		colours.add("C0C0C0"); colours.add("CCFB5D");
		colours.add("FFF380"); colours.add("F9966B");
		colours.add("E9CFEC"); colours.add("FFD700");

		groupId = groupExtras.getInt("groupId");
		subgroupId = groupExtras.getInt("subgroupId");
		
		
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		AppLocales.setLocales(getApplicationContext(), sp.getString("language", "en"));

		int id = sp.getInt("id",-1);
		String token = sp.getString("authToken", "");		
		
		if(id==-1 || token.isEmpty()) {
			throw new IllegalArgumentException("Missing id or token");
		}
				
		String jsonProfile = sp.getString("json_profile", "");
		
		if(!jsonProfile.isEmpty()){
			profile = ProfileUser.getInstance(this.getApplicationContext()).getProfile();
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
		
		//colorBox.setOnClickListener(this);
		
		rb = (ToggleButton)findViewById(R.id.enable_all_radio);
		rb.setOnClickListener(this);
		if (groupedRules.allEnabled(groupId, subgroupId))
			rb.setChecked(true);

		Button ok = (Button)findViewById(R.id.subgroup_btn_ok);
		ok.setOnClickListener(this);
		
		Button cancel = (Button)findViewById(R.id.subgroup_btn_cancel);
		cancel.setOnClickListener(this);

		rbtnHow1 	= (RadioButton) findViewById(R.id.subgroup_option_how_highlight1);
		rbtnHow2 	= (RadioButton) findViewById(R.id.subgroup_option_how_highlight2);
		rbtnWhat1 	= (RadioButton) findViewById(R.id.subgroup_option_what_highlight1);
		rbtnWhat2 	= (RadioButton) findViewById(R.id.subgroup_option_what_highlight2);
		updateRule();
		subgroupProblems = groupedRules.getGroupedProblems().get(groupId).getSubgroups().get(subgroupId).getItems();
		listAdapter = new SubgroupProblemsAdapter(this, R.layout.row_subgroup_details, subgroupProblems);
		setListAdapter(listAdapter);
		
		listPopupWindow = new ColorListPopupWindow(SubgroupDetails.this);

		listPopupWindow.setAdapter(new ColorListAdapter(this,
				R.layout.color_presents_row, colours));
	    listPopupWindow.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				listPopupWindow.dismiss();
				if (listPopupWindow.getParentListPosition()<0){
					defaultColour = Integer.parseInt(colours.get(arg2), 16)+0xFF000000;
					listPopupWindow.getAnchorView().setBackgroundColor(defaultColour);
					for (AnnotationItem ai : groupedRules.getGroupedProblems().get(groupId).getSubgroups().get(subgroupId).getItems()){
						ai.setDefaultColour(Integer.toHexString(defaultColour).substring(2));
						groupedRules.setColour(groupId, subgroupId, defaultColour);
					}
				}
				else{
					AnnotationItem ai = groupedRules.getGroupedProblems().get(groupId).getSubgroups().get(subgroupId).
							getItems().get(listPopupWindow.getParentListPosition());
					ai.setDefaultColour(colours.get(arg2));
					groupedRules.getPresentationRulesAdapter().setHighlightingColor(ai.getCategory(), ai.getIndex(), Integer.parseInt(colours.get(arg2), 16)+0xFF000000);
				}
				listAdapter.notifyDataSetChanged();
			}
		});
	    		
		colorBox.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
	        	listPopupWindow.setParentListPosition(-1);
	        	listPopupWindow.setAnchorView(colorBox);
	        	listPopupWindow.setModal(true);
	        	listPopupWindow.setWidth(70);
	            //listPopupWindow.setHorizontalOffset(colorBox.getWidth());
	            listPopupWindow.show();
			}
		});
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
		if (groupedRules.getTotalNumberOfActiveRules()<90){
			if (groupedRules.getTotalNumberOfActiveColours() > 5)
				Toast.makeText(getApplicationContext(), R.string.warning_on_colours_number, Toast.LENGTH_LONG).show();
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
	            final int listPosition = position;
	            final ImageView activeColorView = (ImageView) view.findViewById(R.id.subgrgoup_active_color);
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
			        	listPopupWindow.setParentListPosition(listPosition);
			        	listPopupWindow.setAnchorView(activeColorView);
			        	listPopupWindow.setModal(true);
			        	listPopupWindow.setWidth(70);
			            //listPopupWindow.setHorizontalOffset(colorBox.getWidth());
			            listPopupWindow.show();
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
	
	class ColorListPopupWindow extends ListPopupWindow{
		private int parentListPosition;
		public ColorListPopupWindow(Context context) {
			super(context);
			this.parentListPosition = -1;
		}
		public int getParentListPosition() {
			return parentListPosition;
		}
		public void setParentListPosition(int parentListPosition) {
			this.parentListPosition = parentListPosition;
		}
	}
	
	public class ColorListAdapter extends ArrayAdapter<String> {
	    private Context context;

	    public ColorListAdapter(Context context, int textViewResourceId, ArrayList<String> items) {
	        super(context, textViewResourceId, items);
	        this.context = context;
	    }

	    public View getView(int position, View convertView, ViewGroup parent) {
	        View view = convertView;
	        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        view = inflater.inflate(R.layout.color_presents_row, null);

	        final String item = getItem(position);

			defaultColour = Integer.parseInt(item, 16)+0xFF000000;
			view.setBackgroundColor(defaultColour);
	        return view;
	    }
	}
	
}
