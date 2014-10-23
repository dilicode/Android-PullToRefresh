package com.mstr.pulltorefresh.sample;

import java.util.Arrays;
import java.util.LinkedList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mstr.pulltorefresh.library.PullToRefreshBase;
import com.mstr.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.mstr.pulltorefresh.library.PullToRefreshListView;

public class PullToRefreshListActivity extends Activity implements OnRefreshListener<ListView> {
	private static final String[] items =  { "Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam", "Abondance", "Ackawi",
			"Acorn", "Adelost", "Affidelice au Chablis", "Afuega'l Pitu", "Airag", "Airedale", "Aisy Cendre",
			"Allgauer Emmentaler", "Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam", "Abondance", "Ackawi",
			"Acorn", "Adelost", "Affidelice au Chablis", "Afuega'l Pitu", "Airag", "Airedale", "Aisy Cendre",
			"Allgauer Emmentaler" };
	
	private static final int MENU_MANUAL_REFRESH = 0;
	
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
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_MANUAL_REFRESH, 0, "Manual Refresh");
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == MENU_MANUAL_REFRESH) {
			listView.manualRefresh();
			
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> view) {
		getData();
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> view) {}
	
	private void getData() {
		new Handler().postDelayed(new Runnable() {
			public void run() {
				list.addFirst("Added after refresh...");
				adapter.notifyDataSetChanged();
				
				listView.onRefreshComplete();
			}
		}, 4000);
	}
}
