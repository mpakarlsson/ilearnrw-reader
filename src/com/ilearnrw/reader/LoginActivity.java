package com.ilearnrw.reader;

import com.ilearnrw.reader.R;
import com.ilearnrw.reader.tasks.LoginTask;
import com.ilearnrw.reader.types.singleton.ProfileUser;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {

	private final String TAG = getClass().getName();

	public Button btnLogin, btnOffline;
	public EditText etUsername, etPassword;
	public CheckBox chkRM;
	private SharedPreferences preferences;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        
        btnLogin 		= (Button) findViewById(R.id.login_button);
        btnOffline		= (Button) findViewById(R.id.login_offline);
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
        btnOffline.setOnClickListener(this);
        
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
					editor.apply();
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

			editor.apply();
			
			new LoginTask(this, TAG).run(username, password);
			break;
			
		case R.id.login_offline:
			ProfileUser p = ProfileUser.getInstance(getApplicationContext());
			
			if(p==null){
				Toast.makeText(this, getString(R.string.login_offline_requires_profile), Toast.LENGTH_LONG).show();
				break;
			}
			
			Intent i = new Intent(this, LibraryActivity.class);
			i.putExtra("offline_mode", true);
			startActivity(i);
			break;
		default:
			break;
		}
	}
}
