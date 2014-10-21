package com.mstr.pulltorefresh.sample;

import java.util.Arrays;
import java.util.LinkedList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mstr.pulltorefresh.library.PullToRefreshBase;
import com.mstr.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.mstr.pulltorefresh.library.PullToRefreshListView;

public class PullToRefreshListActivity extends Activity implements OnRefreshListener<ListView> {
	private String[] items = Cheeses.sCheeseStrings;
	
	private LinkedList<String> list;

	private PullToRefreshListView listView;
	
	private ArrayAdapter<String> adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_list);
		
		listView = (PullToRefreshListView)findViewById(R.id.pull_refresh_list);
		listView.setOnRefreshListener(this);
		
		list = new LinkedList<String>();
		list.addAll(Arrays.asList(items));
		
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
		listView.setAdapter(adapter);
	}
	
	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> view) {
		new Handler().postDelayed(new Runnable() {
			public void run() {
				list.addFirst("Added after refresh...");
				adapter.notifyDataSetChanged();
				
				listView.onRefreshComplete();
			}
		}, 4000);
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> view) {}
}
