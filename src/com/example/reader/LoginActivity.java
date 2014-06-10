package com.example.reader;

import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.HttpResponse;

import com.example.reader.interfaces.ColorPickerListener;
import com.example.reader.results.LoginResult;
import com.example.reader.results.UserDetailResult;
import com.example.reader.types.ColorPickerDialog;
import com.example.reader.utils.HttpHelper;
import com.google.gson.Gson;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import android.widget.Toast;


public class LoginActivity extends Activity implements OnClickListener {

	public Button btnLogin, btnLoginSkip;
	public EditText etUsername, etPassword;
	public CheckBox chkRM;
	private SharedPreferences preferences;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
ColorPickerDialog d =  new ColorPickerDialog(this, 0xff000000, true, new ColorPickerListener() {
	
	@Override
	public void onOk(ColorPickerDialog dialog, int color) {
		// TODO Auto-generated method stub
		Toast.makeText(getBaseContext(), "OK", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onCancel(ColorPickerDialog dialog) {
		// TODO Auto-generated method stub
		Toast.makeText(getBaseContext(), "CANCEL", Toast.LENGTH_SHORT).show();
	}
});
d.show();
        btnLogin = (Button) findViewById(R.id.login_button);
        btnLoginSkip = (Button) findViewById(R.id.login_button_skip);
        etUsername = (EditText) findViewById(R.id.login_username);
        etPassword = (EditText) findViewById(R.id.login_password);
        
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
        btnLoginSkip.setOnClickListener(this);
        
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
        
        chkRM.setChecked(isRemember);
        etUsername.setText(username);
        etPassword.setText(password);
        etUsername.requestFocus();
    	
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
			
			new LoginTask().execute(username, password);
			break;

		case R.id.login_button_skip:
			Intent i2 = new Intent(this, LibraryActivity.class);
			startActivity(i2);
			break;
			
		default:
			break;
		}
	}
	
	private class LoginTask extends AsyncTask<String, Void, LoginResult>{
		private ProgressDialog dialog;
		private String username;
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(LoginActivity.this);
			dialog.setTitle(getString(R.string.dialog_login_title));
			dialog.setMessage(getString(R.string.dialog_login_message));
			dialog.setCancelable(true);
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					cancel(true);
				}
			});
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					dialog.dismiss();
					cancel(true);
				}
			});
			dialog.show();
			super.onPreExecute();
		}

		@Override
		protected LoginResult doInBackground(String... params) {
			username = params[0];
			HttpResponse response = HttpHelper.get("http://api.ilearnrw.eu/ilearnrw/user/auth?username="+username+"&pass="+params[1]);
			
			ArrayList<String> data = HttpHelper.handleResponse(response);
			
			if(data.size()==1){
				return null;
			} else {
				LoginResult lr = new Gson().fromJson(data.get(1), LoginResult.class);
	    		return lr;
			}
		}

		@Override
		protected void onPostExecute(LoginResult result) {
			if(dialog.isShowing()) {
				dialog.dismiss();
			}
			
			if(result != null){
				SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit();
	    		editor.putString("authToken", result.authToken);
	    		editor.putString("refreshToken", result.refreshToken);
	    		editor.commit();
				
	    		new UserDetailsTask().execute(username, result.authToken);
			} else
				Toast.makeText(LoginActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
		}
	};
	
	private class UserDetailsTask extends AsyncTask<String, Void, UserDetailResult>{
		private ProgressDialog dialog;
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(LoginActivity.this);
			dialog.setTitle(getString(R.string.dialog_fetch_user_title));
			dialog.setMessage(getString(R.string.dialog_fetch_user_message));
			dialog.setCancelable(true);
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					cancel(true);
				}
			});
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					dialog.dismiss();
					cancel(true);
				}
			});
			dialog.show();
			super.onPreExecute();
		}
		
		@Override
		protected UserDetailResult doInBackground(String... params) {
			HttpResponse response = HttpHelper.get("http://api.ilearnrw.eu/ilearnrw/user/details/"+ params[0] +"?token=" + params[1]);
		
			ArrayList<String> data = HttpHelper.handleResponse(response);
			
			if(data.size()==1){
				return null;
			} else {
				UserDetailResult userDetails = new Gson().fromJson(data.get(1), UserDetailResult.class);
				return userDetails;
			}
		}
		
		@Override
		protected void onPostExecute(UserDetailResult result) {
			if(dialog.isShowing()) {
				dialog.dismiss();
			}
			
			if(result != null){
				Toast.makeText(LoginActivity.this, getString(R.string.login_succeeded), Toast.LENGTH_SHORT).show();
				
				SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit();
				editor.putInt("id", result.id);
				editor.putString("language", result.language);
				editor.commit();
				
				if(result.language.equals("EN"))
					Locale.setDefault(new Locale("en"));	
				else if(result.language.equals("GR"))
					Locale.setDefault(new Locale("el"));
				else
					Locale.setDefault(new Locale("en"));
					
        		Intent i2 = new Intent(LoginActivity.this, LibraryActivity.class);
        		startActivity(i2);
			} else 
				Toast.makeText(LoginActivity.this, getString(R.string.login_failed_fetching), Toast.LENGTH_SHORT).show();
		}
	};
}
