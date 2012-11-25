package com.fixitnowdroid;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

public class BaseActivity extends Activity {

	public static final String SERVER = "http://ec2-46-137-225-83.ap-southeast-1.compute.amazonaws.com:3000/";

	SharedPreferences mPreferences = null;
	static DefaultHttpClient client = new DefaultHttpClient();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (mPreferences == null) {
			mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		ProgressDialog mProgressDialog = new ProgressDialog(this);
		switch (id) {
		case 0:
			mProgressDialog.setMessage("Signing in...");
			break;
		case 2:
			mProgressDialog.setMessage("Joining...");
			break;
		case 3:
			mProgressDialog.setMessage("Posting...");
			break;
		case 9:
			mProgressDialog.setMessage("Singing out...");
			break;
		default:
			mProgressDialog.setMessage("Loading ...");
			break;
		}

		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setCancelable(true);
		return mProgressDialog;
	}

	/**
	 * 
	 */
	protected void backgroundUpdate() {
		Thread t = new Thread() {
			public void run() {
				try {
					process();
				} catch (HttpResponseException hre) {
					if (hre.getStatusCode() == 401) {
						login();
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};
		t.start();
	}

	private void login() {
		Intent intent = new Intent(this, LoginPageActivity.class);
		startActivity(intent);
	}

	public void logout() {
		new LogoutActivity().execute();
		Intent signIn = new Intent(this, LoginPageActivity.class);
		startActivity(signIn);
	}

	protected void process() throws ClientProtocolException, IOException {

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.menu_logout:
	            logout();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private class LogoutActivity extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			HttpGet get = new HttpGet(SERVER + "users/sign_out");
			get.setHeader("Accept", "application/json");
			get.setHeader("Content-Type", "application/json");
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response = null;
			try {
				response = client.execute(get, responseHandler);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(response!=null) {
				Log.d("logout", response);
			}
			return response;
		}
		
		@Override
		protected void onPreExecute() {
			showDialog(9);
		}
		
		@Override
		protected void onPostExecute(String response) {
			Editor editor = mPreferences.edit();
			editor.remove("auth_token");
			editor.commit();
			showDialog(9);
		}
	}
}
