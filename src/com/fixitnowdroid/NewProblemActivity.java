package com.fixitnowdroid;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.layout;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class NewProblemActivity extends BaseActivity {

	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";

	private File mImageFile;
	private Bitmap mImageBitmap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_problem);
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			Button cameraButton = (Button) findViewById(R.id.camera_button);
			cameraButton.setVisibility(View.INVISIBLE);
			cameraButton.setEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_new_problem, menu);
		return true;
	}
	
	public void postProblem(View view) {
		byte[] byteArray=null;
		if(mImageBitmap!=null) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			mImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			byteArray = stream.toByteArray();
		}
		JSONObject holder = new JSONObject();
		JSONObject problemObj = new JSONObject();
		try {
			problemObj.put("title", ((EditText)findViewById(R.id.problem_title)).getText().toString());
			problemObj.put("description", ((EditText)findViewById(R.id.problem_description)).getText().toString());
			problemObj.put("content_type", "image/jpg");
			problemObj.put("original_filename", mImageFile.getName());
			if(byteArray!=null) {
				problemObj.put("picture", Base64.encodeToString(byteArray, Base64.DEFAULT));
			}
			holder.put("problem", problemObj);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		new PostProblem().execute(holder);
	}

	public void takePicture(View view) {
		try {
			mImageFile = createImageFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mImageFile));

		startActivityForResult(takePictureIntent, 1);
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
		return imageF;
	}

	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

			storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "FixItNow");

			if (storageDir != null) {
				if (!storageDir.mkdirs()) {
					if (!storageDir.exists()) {
						Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}

		} else {
			Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
		}

		return storageDir;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inSampleSize = 2;
		bmOptions.inPurgeable = true;
		mImageBitmap = BitmapFactory.decodeFile(mImageFile.getPath(), bmOptions);

		LinearLayout problemLayout = (LinearLayout) findViewById(R.id.problem_layout);
		Context context = getBaseContext();
		FrameLayout layout = new FrameLayout(context);
		ImageView imageView = new ImageView(context);
		imageView.setImageBitmap(mImageBitmap);
		layout.addView(imageView);
	}

	private class PostProblem extends AsyncTask<JSONObject, Void, String> {

		@Override
		protected void onPreExecute() {
			showDialog(3);
		}
		
		@Override
		protected String doInBackground(JSONObject... problems) {
			String response = null;
			for(JSONObject holder : problems) {
				HttpPost post = new HttpPost(SERVER + "problems.json?auth_token=" + mPreferences.getString("auth_token", null));
				post.setHeader("Accept", "application/json");
				post.setHeader("Content-Type", "application/json");
				
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				try {
					StringEntity se = new StringEntity(holder.toString());
					post.setEntity(se);
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				try {
					response = client.execute(post, responseHandler);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return response;
		}
		
		@Override
		protected void onPostExecute(String result) {
			removeDialog(3);
	     }
	}
}
