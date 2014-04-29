package com.example.reader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity implements OnClickListener {

	public static Button btn_login;
	public static EditText et_username, et_password;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_login = (Button) findViewById(R.id.login_button);
        et_username = (EditText) findViewById(R.id.login_username);
        et_password = (EditText) findViewById(R.id.login_password);
        
        btn_login.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.login_button:
			// TODO: Send information to API and process response
			
			Intent intent = new Intent(this, LibraryActivity.class);
			
			startActivity(intent);
			break;

		default:
			break;
		}
		
	}
}
