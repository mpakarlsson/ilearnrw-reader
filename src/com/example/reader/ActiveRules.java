package com.example.reader;

import android.os.Bundle;
import android.app.ListActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ActiveRules extends ListActivity {
	TextView selection;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.active_rules_list);
		setListAdapter(new ArrayAdapter<String>(this, R.layout.row_active_rules, 
				                          R.id.label, SmartPhones));
		selection=(TextView)findViewById(R.id.selection);
	}
	
	public void onListItemClick(ListView parent, View v,
					                    int position, long id) {
	 	selection.setText(SmartPhones[position]);
	}

	static final String[] SmartPhones = new String[] {
		"First active rule",  "Second active rule", 
		"Third active rule", "Fourth active rule", 
		"Fifth active rule", "Sixth active rule", 
		"Seventh active rule", "Eighth active rule"
	};
}