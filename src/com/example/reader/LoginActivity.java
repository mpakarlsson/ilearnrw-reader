package com.example.reader;

import com.example.reader.types.handlers.ExtendedLoginHandler;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;


public class LoginActivity extends Activity implements OnClickListener {

	public Button btnLogin, btnLoginSkip;
	public EditText etUsername, etPassword;
	public CheckBox chkRM;
	private SharedPreferences preferences;
	private Handler handlerLogin;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = (Button) findViewById(R.id.login_button);
        btnLoginSkip = (Button) findViewById(R.id.login_button_skip);
        etUsername = (EditText) findViewById(R.id.login_username);
        etPassword = (EditText) findViewById(R.id.login_password);
        
        chkRM = (CheckBox) findViewById(R.id.chk_remember_me);
        
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final boolean isRemember = preferences.getBoolean("rememberMe", false);
        String username = preferences.getString("username", "");
        String password = preferences.getString("password", "");
        
        
        chkRM.setChecked(isRemember);
        etUsername.setText(username);
        etPassword.setText(password);
        etUsername.setSelection(username.length());
        etPassword.setSelection(password.length());
        
        btnLogin.setOnClickListener(this);
        btnLoginSkip.setOnClickListener(this);
        
        chkRM.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isRemember == true && !isChecked){
					etUsername.setText("");
					etPassword.setText("");
				}	
			}
		});    
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
			final String username = etUsername.getText().toString();
			final String password = etPassword.getText().toString();
			
			final SharedPreferences.Editor editor = preferences.edit();			
			if(chkRM.isChecked()){
				editor.putBoolean("rememberMe", true);
				editor.putString("username", username);
				editor.putString("password", password);
			} else {
				editor.putBoolean("rememberMe", false);
				editor.putString("username", "");
				editor.putString("password", "");
			}
			editor.commit();
			
			handlerLogin = new ExtendedLoginHandler(this, getBaseContext(), username);
			
	        new HttpConnection(handlerLogin).get("http://api.ilearnrw.eu/ilearnrw/user/auth?username="+username+"&pass="+password);
			
			break;

		case R.id.login_button_skip:
			Intent i2 = new Intent(this, LibraryActivity.class);
			startActivity(i2);
			break;
		default:
			break;
		}
	}	
}
