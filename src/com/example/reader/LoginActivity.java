package com.example.reader;

import com.example.reader.tasks.LoginTask;
import com.example.reader.types.singleton.ProfileUser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        
        btnLogin 		= (Button) findViewById(R.id.login_button);
        btnLogout 		= (Button) findViewById(R.id.login_logout);
        etUsername 		= (EditText) findViewById(R.id.login_username);
        etPassword 		= (EditText) findViewById(R.id.login_password);
        
        chkRM = (CheckBox) findViewById(R.id.chk_remember_me);

        final boolean isRemember = preferences.getBoolean(getString(R.string.sp_user_remember_me), false);
        String username = preferences.getString(getString(R.string.sp_user_name), "");
        String password = preferences.getString(getString(R.string.sp_user_password), "");     
        
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
					editor.remove(getString(R.string.sp_user_name));
					editor.remove(getString(R.string.sp_user_password));
					editor.remove(getString(R.string.sp_user_remember_me));
					editor.remove(getString(R.string.sp_authToken));
					editor.remove(getString(R.string.sp_refreshToken));
					editor.commit();
				}	
			}
		});
    }
    
    @Override
	protected void onResume() {
    	super.onResume();
		
    	boolean isRemember = preferences.getBoolean(getString(R.string.sp_user_remember_me), false);
        String username = preferences.getString(getString(R.string.sp_user_name), "");
        String password = preferences.getString(getString(R.string.sp_user_password), "");
        isLoggedIn = preferences.getBoolean(getString(R.string.sp_user_is_logged_in), false);
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
				editor.putBoolean(getString(R.string.sp_user_remember_me), true);
				editor.putString(getString(R.string.sp_user_name), username);
				editor.putString(getString(R.string.sp_user_password), password);
			} else {
				editor.putBoolean(getString(R.string.sp_user_remember_me), false);
				editor.putString(getString(R.string.sp_user_name), "");
				editor.putString(getString(R.string.sp_user_password), "");
			}

			updateButtons();
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
					edit.remove(getString(R.string.sp_user_name));
					edit.remove(getString(R.string.sp_user_password));
					edit.remove(getString(R.string.sp_user_remember_me));
					edit.remove(getString(R.string.sp_authToken));
					edit.remove(getString(R.string.sp_refreshToken));
					
					isLoggedIn = false;
					updateButtons();
					edit.putBoolean(getString(R.string.sp_user_is_logged_in), isLoggedIn);
					edit.commit();
					
					ProfileUser.getInstance(getApplicationContext()).nullProfile();
					
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
