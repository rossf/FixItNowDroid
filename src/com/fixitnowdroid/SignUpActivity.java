package com.fixitnowdroid;

import java.util.HashMap;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class SignUpActivity extends UserServices {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_sign_up, menu);
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
    
    public void signUp(View view) {
    	showDialog(2);
    	Editor editor = mPreferences.edit();
		EditText email = (EditText) findViewById(R.id.sign_up_email_field);
		editor.putString("UserName", email.getText().toString());
		EditText password = (EditText) findViewById(R.id.sign_up_password_field);
		editor.putString("Password", password.getText().toString());
		editor.commit();
    	backgroundUpdate();
    }
    
    @Override
    protected void process() {
    	EditText email = (EditText) findViewById(R.id.sign_up_email_field);
    	EditText password = (EditText) findViewById(R.id.sign_up_password_field);
    	HashMap<String, String> sessionTokens = signUp(email.getText().toString(), password.getText().toString());
    	Editor editor = mPreferences.edit();
		editor.putString("auth_token", sessionTokens.get("auth_token"));
		editor.commit();
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		removeDialog(2);
		finish();
    }

}
