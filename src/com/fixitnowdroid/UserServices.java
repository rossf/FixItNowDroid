package com.fixitnowdroid;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public abstract class UserServices extends BaseActivity{

	private static final String TAG = "UserServices";
	
	protected HashMap<String, String> signIn(String userName, String password) {
		HttpPost post = new HttpPost(SERVER + "tokens");
		return postUserTo(post, userName, password);
	}
	
	protected HashMap<String, String> signUp(String userName, String password) {
		HttpPost post = new HttpPost(SERVER + "users");
		return postUserTo(post, userName, password);
	}
	
	private HashMap<String, String> postUserTo(HttpPost post, String email, String password) {
		HashMap<String, String> sessionTokens = null;

		JSONObject holder = new JSONObject();
		JSONObject userObj = new JSONObject();

		try {
			userObj.put("password", password);
			userObj.put("email", email);
			holder.put("user", userObj);
			StringEntity se = new StringEntity(holder.toString());
			post.setEntity(se);
			post.setHeader("Accept", "application/json");
			post.setHeader("Content-Type", "application/json");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (JSONException js) {
			js.printStackTrace();
		}

		String response = null;
		try {
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			response = client.execute(post, responseHandler);
			Log.d(TAG, response);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {

			/*
			 * if (response == null) { System.out.println("response is null " +
			 * response); Exception e = new Exception(); throw e; }
			 */
			sessionTokens = parseToken(response);

			// now = Long.valueOf(System.currentTimeMillis());
			// mSignInDbHelper.createSession(mEmailField.getText().toString(),mAuthToken,now);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sessionTokens;
	}
	
	private HashMap<String, String> parseToken(String jsonResponse) throws JSONException {
		HashMap<String, String> sessionTokens = new HashMap<String, String>();
		if (jsonResponse != null) {
			JSONObject jObject = new JSONObject(jsonResponse);
			JSONObject sessionObject = jObject.getJSONObject("session");
			String attributeError = sessionObject.getString("error");
			String attributeToken = sessionObject.getString("auth_token");
			sessionTokens.put("error", attributeError);
			sessionTokens.put("auth_token", attributeToken);
		} else {
			sessionTokens.put("error", "Error");
		}
		return sessionTokens;
	}
}
