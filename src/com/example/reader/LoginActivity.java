package com.example.reader;

import com.example.reader.serveritems.LoginResult;
import com.example.reader.serveritems.UserDetailResult;
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
	private Handler handlerLogin, handlerUserInfo;
	
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
			
			
			handlerLogin = new Handler() {
	            public void handleMessage(Message message) {
	            	switch (message.what) {
		            	case HttpConnection.CONNECTION_START: {
		            		Log.d("Login", "Starting connection...");
		            		break;
		            	}
		            	case HttpConnection.CONNECTION_SUCCEED: {
		            		String response = (String) message.obj;
		            		Log.d("Login", response);
		            		
		            		LoginResult lr = new Gson().fromJson(response, LoginResult.class);
		            		editor.putString("authToken", lr.authToken);
		            		editor.putString("refreshToken", lr.refreshToken);
		            		editor.commit();
		            		
		            		new HttpConnection(handlerUserInfo).get("http://api.ilearnrw.eu/ilearnrw/user/details/"+ username +"?token=" + lr.authToken);
		            		
		            		break;
		            	}
		            	case HttpConnection.CONNECTION_ERROR: { 
		            		Exception e = (Exception) message.obj;
		            		e.printStackTrace();
		            		Log.e("Login", "Connection failed.");
		            		Toast.makeText(getBaseContext(), "Connection failed due to error.", Toast.LENGTH_SHORT).show();
		            		break;
		            	}
		            	case HttpConnection.CONNECTION_RESPONSE_ERROR: {
		            		String s = (String) message.obj;
		            		Log.e("Login", s);
		            		Toast.makeText(getBaseContext(), "Connection failed due to wrong status code.", Toast.LENGTH_SHORT).show();
		            		break;
		            	}
	            	}
	            }
			};
			
			handlerUserInfo = new Handler(){

				@Override
				public void handleMessage(Message msg) {
					switch(msg.what){
					case HttpConnection.CONNECTION_START:
						Log.d("UserDetails", "Fetching user info");
						break;
						
					case HttpConnection.CONNECTION_SUCCEED:
						Log.d("UserDetails", "Fetching user info succeeded");
						
						String response = (String) msg.obj;
						UserDetailResult userDetails = new Gson().fromJson(response, UserDetailResult.class);
						
						editor.putInt("id", userDetails.id);
						editor.putString("language", userDetails.language);
						editor.commit();
						
	            		Intent i2 = new Intent(getBaseContext(), LibraryActivity.class);
	            		startActivity(i2);
	            		
						break;
						
					case HttpConnection.CONNECTION_ERROR:
						Log.e("UserDetails", "Getting user info failed");
						break;
						
					case HttpConnection.CONNECTION_RESPONSE_ERROR:
						Log.e("UserDetails", "Getting user info status fail");
						break;
					
					}
				}
			};
			
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
