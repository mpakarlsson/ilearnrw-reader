package com.example.reader;

import com.example.reader.tasks.LoginTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
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

	private final String TAG = getClass().getName();

	public Button btnLogin, btnLogout;
	public EditText etUsername, etPassword;
	public CheckBox chkRM;
	private SharedPreferences preferences;
	private boolean isLoggedIn;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        btnLogin 		= (Button) findViewById(R.id.login_button);
        btnLogout 		= (Button) findViewById(R.id.login_logout);
        etUsername 		= (EditText) findViewById(R.id.login_username);
        etPassword 		= (EditText) findViewById(R.id.login_password);
        
        chkRM = (CheckBox) findViewById(R.id.chk_remember_me);
        
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean isRemember = preferences.getBoolean("rememberMe", false);
        String username = preferences.getString("username", "");
        String password = preferences.getString("password", "");       
        
        chkRM.setChecked(isRemember);
        etUsername.setText(username);
        etPassword.setText(password);
        etUsername.setSelection(username.length());
        etPassword.setSelection(password.length());
        
        btnLogin.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        
        chkRM.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isRemember == true && !isChecked){
					etUsername.setText("");
					etPassword.setText("");
					SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
					editor.remove("username");
					editor.remove("password");
					editor.remove("rememberMe");
					editor.remove("authToken");
					editor.remove("refreshToken");
					editor.commit();
				}	
			}
		});
    }
    
    @Override
	protected void onResume() {
    	super.onResume();
		
    	boolean isRemember = preferences.getBoolean("rememberMe", false);
        String username = preferences.getString("username", "");
        String password = preferences.getString("password", "");
        isLoggedIn = preferences.getBoolean("isLoggedIn", false);
        updateButtons();
        
        
        chkRM.setChecked(isRemember);
        etUsername.setText(username);
        etPassword.setText(password);
        etUsername.requestFocus();
    	
	}
    
    /*
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
    
    */

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

			updateButtons();
			isLoggedIn = true;
			editor.putBoolean("isLoggedIn", isLoggedIn);
			
			editor.commit();

			new LoginTask(this, TAG).run(username, password);
			break;
		
		case R.id.login_logout:
			new AlertDialog.Builder(this)
			.setTitle(getString(R.string.dialog_logout_title))
			.setMessage(getString(R.string.dialog_logout_message))
			.setNegativeButton(android.R.string.no, null)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SharedPreferences.Editor edit= preferences.edit();
					edit.remove("username");
					edit.remove("password");
					edit.remove("rememberMe");
					edit.remove("authToken");
					edit.remove("refreshToken");
					
					isLoggedIn = false;
					updateButtons();
					edit.putBoolean("isLoggedIn", isLoggedIn);
					
					edit.commit();
					
					etUsername.getText().clear();
					etPassword.getText().clear();
					chkRM.setChecked(false);
				}
			}).show();
			
			break;
		default:
			break;
		}
	}
	
	private void updateButtons(){		
		if(isLoggedIn)
        	btnLogout.setVisibility(View.VISIBLE);
        else
        	btnLogout.setVisibility(View.GONE);
	}
}
