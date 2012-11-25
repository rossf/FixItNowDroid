package com.fixitnowdroid;

import java.io.IOException;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class LoginPageActivity extends UserServices {
	
	private static final String TAG = "LoginPageActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_page);
		if(mPreferences.contains("UserName")) {
			EditText email = (EditText) findViewById(R.id.email_field);
			email.setText(mPreferences.getString("UserName", null));
		}
		if(mPreferences.contains("Password")) {
			EditText password = (EditText) findViewById(R.id.password_field);
			password.setText(mPreferences.getString("Password", null));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_login_page, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void logIn(View view) {
		showDialog(0);
		Editor editor = mPreferences.edit();
		EditText email = (EditText) findViewById(R.id.email_field);
		editor.putString("UserName", email.getText().toString());
		EditText password = (EditText) findViewById(R.id.password_field);
		editor.putString("Password", password.getText().toString());
		editor.commit();
		backgroundUpdate();
	}
	
	public void signUp(View view) {
		Intent intent = new Intent(this, SignUpActivity.class);
		startActivity(intent);
	}

	@Override
	protected void process() throws ClientProtocolException, IOException {
		try {
			EditText mEmailField = (EditText) findViewById(R.id.email_field);
			EditText mPasswordField = (EditText) findViewById(R.id.password_field);

			String email = mEmailField.getText().toString();
			String password = mPasswordField.getText().toString();
			HashMap<String, String> sessionTokens = signIn(email, password);
			Editor editor = mPreferences.edit();
			editor.putString("auth_token", sessionTokens.get("auth_token"));
			editor.commit();
			removeDialog(0);
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();

		} catch (Exception e) {
			Intent intent = new Intent(this, LoginPageActivity.class);
			intent.putExtra("LoginMessage", "Unable to login");
			startActivity(intent);
			removeDialog(0);
		}
	}
}
