package com.mstr.pulltorefresh.sample;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LauncherActivity extends ListActivity{
	private static final String[] ITEMS = {"ListView", "ScrollView"};
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ITEMS));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = null;
		switch (position) {
		case 0:
			intent = new Intent(this, PullToRefreshListActivity.class);
			break;
			
		case 1:
			intent = new Intent(this, PullToRefreshScrollViewActivity.class);
			break;
		}
		
		if (intent != null) {
			startActivity(intent);
		}
	}
}
