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

	public static Button btnLogin;
	public static EditText etUsername, etPassword;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = (Button) findViewById(R.id.login_button);
        etUsername = (EditText) findViewById(R.id.login_username);
        etPassword = (EditText) findViewById(R.id.login_password);
        
        btnLogin.setOnClickListener(this);
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
