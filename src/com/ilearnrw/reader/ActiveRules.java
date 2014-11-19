package com.ilearnrw.reader;

import ilearnrw.user.problems.ProblemDefinition;
import ilearnrw.user.problems.ProblemDefinitionIndex;
import ilearnrw.user.problems.ProblemDescription;
import ilearnrw.user.profile.UserProfile;

import java.util.ArrayList;

import com.ilearnrw.reader.R;
import com.ilearnrw.reader.types.singleton.ProfileUser;
import com.ilearnrw.reader.utils.AppLocales;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ActiveRules extends ListActivity {
	private TextView selection;
	private SharedPreferences sp;
	
	private ArrayList<Problem> activeProblems;
	
	private ProblemDefinition[] definitions;
	private ProblemDescription[][] descriptions;
	private UserProfile profile;
	
	private ProblemDescriptionAdapter listAdapter;

	private final int DEFAULT_COLOR = 0xffff0000;
	private final int DEFAULT_RULE	= 3;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.active_rules_list);
		
        sp = PreferenceManager.getDefaultSharedPreferences(this);
		AppLocales.setLocales(getApplicationContext(), sp.getString(getString(R.string.sp_user_language), "en"));
		
		activeProblems 	= new ArrayList<Problem>();
		listAdapter = new ProblemDescriptionAdapter(this, R.layout.row_active_rules, activeProblems);
		setListAdapter(listAdapter);

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
		updateData();
		
        Button add = (Button) this.findViewById(R.id.add_active_rule);
        add.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ActiveRules.this, PresentationModule.class);
				intent.putExtra("showGUI", true);
				startActivity(intent);
			}
		});        
        
		selection=(TextView)findViewById(R.id.selection);
		selection.setText(R.string.active_rules);
	}
	
	public void onResume(){
		super.onResume();
		updateData();
	}
	
	public void onListItemClick(ListView parent, View v,
					                    int position, long id) {
	}
	
	private void initProfile(String jsonProfile){
		profile = ProfileUser.getInstance(this.getApplicationContext()).getProfile();
		
		ProblemDefinitionIndex index = profile.getUserProblems().getProblems();
		
		definitions 	= index.getProblemsIndex();
		descriptions 	= index.getProblems();
				
	}
	
	public class ProblemDescriptionAdapter extends ArrayAdapter<Problem> {

	    private Context context;

	    public ProblemDescriptionAdapter(Context context, int textViewResourceId, ArrayList<Problem> items) {
	        super(context, textViewResourceId, items);
	        this.context = context;
	    }

	    public View getView(int position, View convertView, ViewGroup parent) {
	        View view = convertView;
	        if (view == null) {
	            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            view = inflater.inflate(R.layout.row_active_rules, parent, false);
	        }

	        final Problem item = getItem(position);
	        if (item!= null) {
	            TextView itemView = (TextView) view.findViewById(R.id.label_test);
	            TextView activeRuleView = (TextView) view.findViewById(R.id.active_rule);
	            ImageView activeColorView = (ImageView) view.findViewById(R.id.active_color);
	            Button edit = (Button) view.findViewById(R.id.edit_active_rule);
	            edit.setText(R.string.edit_btn_text);
	            edit.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						System.err.println(item.category);
						Intent intent = new Intent(context, PresentationModule.class);
						intent.putExtra("category", item.category);
						intent.putExtra("index", item.index);
						intent.putExtra("showGUI", true);
						context.startActivity(intent);
					}
				});
	            Button delete = (Button) view.findViewById(R.id.delete_active_rule);
	            delete.setText(R.string.delete_btn_text);
	            delete.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						AlertDialog.Builder alert = new AlertDialog.Builder(context);
			            alert.setTitle(getResources().getString(R.string.deactivate_rule_alert_title));
			            alert.setMessage(getResources().getString(R.string.deactivate_rule));
			            alert.setPositiveButton(R.string.confirm_btn_text, new DialogInterface.OnClickListener() {
			            	public void onClick(DialogInterface dialog, int id) {
			            		sp.edit().putBoolean(sp.getInt(getString(R.string.sp_user_id), 0)+"pm_enabled_" + item.category + "_" + item.index, false).apply();
			            		updateData();
			       			}
			            });
			            alert.setNegativeButton(R.string.cancel_btn_text, new DialogInterface.OnClickListener() {
			                   public void onClick(DialogInterface dialog, int id) {
			                       // User cancelled the dialog
			                   }
			            });

			            alert.show();
						
					}
				});
	            if (itemView != null) {
	                itemView.setText(String.format(getResources().getString(R.string.active_problem_description)+" %s", item.description));
	                String rule = getResources().getString(R.string.problem_1);
	                switch (sp.getInt(sp.getInt(getString(R.string.sp_user_id), 0)+"pm_rule_"+item.category+"_"+item.index, DEFAULT_RULE)){
	                case 1:
	                	rule = getResources().getString(R.string.problem_1);
	                	break;
	                case 2:
	                	rule = getResources().getString(R.string.problem_2);
	                	break;
	                case 3:
	                	rule = getResources().getString(R.string.problem_3);
	                	break;
	                case 4:
	                	rule = getResources().getString(R.string.problem_4);
	                	break;
	                	
	                }
	                activeRuleView.setText(getResources().getString(R.string.active_problem_rule)+" "+rule);
	                activeColorView.setBackgroundColor(sp.getInt(sp.getInt(getString(R.string.sp_user_id), 0)+"pm_color_"+item.category+"_"+item.index, DEFAULT_COLOR));
	            }
	         }

	        return view;
	    }
	}
	
	private void updateData() {
		activeProblems.clear();
		for (int i = 0; i < definitions.length; i++){
			for (int j = 0; j < descriptions[i].length; j++){
				String id = getString(R.string.sp_user_id);
				//int color 			= sp.getInt(sp.getInt(id, 0)+"pm_color_"+i+"_"+j, DEFAULT_COLOR);
				//int rule 			= sp.getInt(sp.getInt(id, 0)+"pm_rule_"+i+"_"+j, DEFAULT_RULE); 
				boolean isChecked 	= sp.getBoolean(sp.getInt(id, 0)+"pm_enabled_"+i+"_"+j, false);
				if (isChecked){
					String text = "";
					for (int k=0; k<descriptions[i][j].getDescriptions().length; k++)
						if (k < descriptions[i][j].getDescriptions().length-1)
							text = text+descriptions[i][j].getDescriptions()[k]+", ";
						else
							text = text+descriptions[i][j].getDescriptions()[k];
							
					ActiveRules.Problem prob = new Problem();
					prob.category = i;
					prob.index = j;
					prob.description = "{"+text+"}";
					activeProblems.add(prob);
				}
			}
		}
	    listAdapter.notifyDataSetChanged();
	}
	
	private class Problem{
		int category, index;
		String description;
	}
}