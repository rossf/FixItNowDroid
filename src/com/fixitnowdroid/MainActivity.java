package com.fixitnowdroid;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.fixitnowdroid.util.DiskLruCache;

public class MainActivity extends BaseActivity {

	private DiskLruCache mDiskCache;
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
	private static final String DISK_CACHE_SUBDIR = "thumbnails";

	private ArrayList<View> compressedProblems = new ArrayList<View>();
	private String[][] descriptions;
	private ProblemListAdapter mAdapter;
	private Intent loginIntent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		loginIntent = new Intent(this, LoginPageActivity.class);
		File cacheDir = getCacheDir(this, DISK_CACHE_SUBDIR);
		mDiskCache = DiskLruCache.openCache(this, cacheDir, DISK_CACHE_SIZE);
		if (!mPreferences.contains("auth_token")) {
			Intent intent = new Intent(this, LoginPageActivity.class);
			startActivity(intent);
		}

		ExpandableListView epView = (ExpandableListView) findViewById(R.id.problem_list);
		mAdapter = new ProblemListAdapter(this, compressedProblems, descriptions);
		epView.setAdapter(mAdapter);
		epView.setOnGroupClickListener(new OnGroupClickListener() {

			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				if (groupPosition == 5) {

				}
				return false;
			}

		});

		epView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				if (groupPosition == 0 && childPosition == 0) {

				}
				return false;
			}

		});
	}

	@Override
	public void onResume() {
		super.onResume();
		new UpdateList().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void newProblem(View view) {
		Intent intent = new Intent(this, NewProblemActivity.class);
		startActivity(intent);
	}

	private class UpdateList extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			showDialog(1);
		}

		@Override
		protected String doInBackground(Void... params) {
			HttpGet get = new HttpGet(SERVER + "problems.json?auth_token=" + mPreferences.getString("auth_token", null));
			get.setHeader("Accept", "application/json");
			get.setHeader("Content-Type", "application/json");
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response = null;
			try {
				response = client.execute(get, responseHandler);
			} catch (HttpResponseException hre) {
				if (hre.getStatusCode() == 401) {
					return "!unauthorized";
				}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null && result.equals("!unauthorized")) {
				startActivity(loginIntent);
				removeDialog(1);
			}
			try {
				parseArray(result);
				removeDialog(1);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void parseArray(String response) throws JSONException {
			if (response != null) {
				JSONArray jArray = new JSONArray(response);
				descriptions = new String[jArray.length()][1];
				for (int i = 0; i < jArray.length(); i++) {
					JSONObject problem = jArray.getJSONObject(i);
					View layout = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
							R.layout.compressed_problem_fragment, null);
					TextView title = (TextView) layout.findViewById(R.id.problem_title);
					title.setText(problem.getString("title"));

					ImageView thumbnail = (ImageView) layout.findViewById(R.id.problem_thumbnail);
					new BitmapWorkerTask().execute(problem.getString("picture_file_name"), thumbnail, problem.getString("id"));
					compressedProblems.add(layout);
					descriptions[i][0] = problem.getString("description");
				}
				mAdapter.descriptions = descriptions;
				mAdapter.notifyDataSetChanged();
			}
		}
	}

	// Creates a unique subdirectory of the designated app cache directory.
	// Tries to use external
	// but if not mounted, falls back on internal storage.
	public static File getCacheDir(Context context, String uniqueName) {
		// Check if media is mounted or storage is built-in, if so, try and use
		// external cache dir
		// otherwise use internal cache dir
		final String cachePath = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
				|| !Environment.isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() : context.getCacheDir()
				.getPath();

		return new File(cachePath + File.separator + uniqueName);
	}

	private class BitmapWorkerTask extends AsyncTask<Object, Void, Object[]> {

		// Decode image in background.
		@Override
		protected Object[] doInBackground(Object... params) {
			final String imageKey = String.valueOf(params[0]);

			// Check disk cache in background thread
			Bitmap bitmap = mDiskCache.get(imageKey);

			if (bitmap == null) { // Not found in disk cache
				// Process as normal

				try {
					String id = params[2].toString();
					URL pictureURL = new URL(BaseActivity.SERVER + "system/problems/pictures/000/000/00" + id + "/thumb/" + imageKey);
					URLConnection connection = pictureURL.openConnection();
					connection.setDoInput(true);
					connection.connect();
					InputStream is = connection.getInputStream();
					bitmap = BitmapFactory.decodeStream(is);
					// Add final bitmap to caches
					if (bitmap != null) {
						addBitmapToCache(imageKey, bitmap);
					}
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			return new Object[] { bitmap, params[1] };
		}

		@Override
		protected void onPostExecute(Object[] resources) {
			if (resources[1] != null) {
				ImageView thumbnail = (ImageView) resources[1];
				thumbnail.setImageBitmap((Bitmap) resources[0]);
			}
		}

		public void addBitmapToCache(String key, Bitmap bitmap) {

			// Also add to disk cache
			if (!mDiskCache.containsKey(key)) {
				mDiskCache.put(key, bitmap);
			}
		}
	}
}
