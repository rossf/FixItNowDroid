package com.fixitnowdroid;

import java.util.ArrayList;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ProblemListAdapter extends BaseExpandableListAdapter {

	private ArrayList<View> compressedProblems;
	public String[][] descriptions;
	private Activity parentActivity;

	public ProblemListAdapter(Activity parentActivity, ArrayList<View> compressedProblems, String[][] descriptions) {
		this.parentActivity = parentActivity;
		this.compressedProblems = compressedProblems;
		this.descriptions = descriptions;
	}

	public Object getChild(int groupPosition, int childPosition) {
		return descriptions[groupPosition][childPosition];
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public int getChildrenCount(int groupPosition) {
		int i = 0;
		try {
			i = descriptions[groupPosition].length;

		} catch (Exception e) {
		}

		return i;
	}

	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		TextView textView = (TextView) getGenericView();
		textView.setText(getChild(groupPosition, childPosition).toString());
		return textView;
	}

	public View getGenericView() {
		// Layout parameters for the ExpandableListView
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		TextView textView = new TextView(parentActivity);
		textView.setLayoutParams(lp);
		// Center the text vertically
		textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
		// textView.setTextColor(R.color.marcyred);
		// Set the text starting position
		textView.setPadding(250, 0, 0, 0);
		textView.setTextSize(18);
		return textView;
	}

	public Object getGroup(int groupPosition) {
		return compressedProblems.get(groupPosition);
	}

	public int getGroupCount() {
		return compressedProblems.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		View problem = (View) getGroup(groupPosition);
		return problem;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public boolean hasStableIds() {
		return true;
	}
}
