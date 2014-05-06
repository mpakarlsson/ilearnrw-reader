package com.example.reader;

import com.google.gson.Gson;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;


public class LoginActivity extends Activity implements OnClickListener {

	public Button btnLogin, btnLoginSkip;
	public EditText etUsername, etPassword;
	public CheckBox chkRM;
	private SharedPreferences preferences;
	private static Handler handler;
	
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
			String username = etUsername.getText().toString();
			String password = etPassword.getText().toString();
			
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
			
			
			handler = new Handler() {
	            public void handleMessage(Message message) {
	            	switch (message.what) {
		            	case HttpConnection.CONNECTION_START: {
		            		Log.d("connectionStart", "Starting connection...");
		            		break;
		            	}
		            	case HttpConnection.CONNECTION_SUCCEED: {
		            		String response = (String) message.obj;
		            		Log.d("connectionSucceed", response);
		            		
		            		LoginResult lr = new Gson().fromJson(response, LoginResult.class);
		            		editor.putString("authToken", lr.authToken);
		            		editor.putString("refreshToken", lr.refreshToken);
		            		editor.commit();
		            		
		            		Intent i2 = new Intent(getBaseContext(), LibraryActivity.class);
		        			startActivity(i2);
		            		
		            		break;
		            	}
		            	case HttpConnection.CONNECTION_ERROR: { 
		            		Exception e = (Exception) message.obj;
		            		e.printStackTrace();
		            		Log.e("connectionError", "Connection failed.");
		            		Toast.makeText(getBaseContext(), "Connection failed due to error.", Toast.LENGTH_SHORT).show();
		            		break;
		            	}
		            	case HttpConnection.CONNECTION_RESPONSE_ERROR: {
		            		String s = (String) message.obj;
		            		Log.e("connectionResponseError", s);
		            		Toast.makeText(getBaseContext(), "Connection failed due to wrong status code.", Toast.LENGTH_SHORT).show();
		            		break;
		            	}
	            	}
	            }
			};
				        
	        new HttpConnection(handler).get("http://api.ilearnrw.eu/ilearnrw/user/auth?username="+username+"&pass="+password);
			
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
