package com.mstr.pulltorefresh.sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ScrollView;

import com.mstr.pulltorefresh.library.PullToRefreshBase;
import com.mstr.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.mstr.pulltorefresh.library.PullToRefreshScrollView;

public class PullToRefreshScrollViewActivity extends Activity implements OnRefreshListener<ScrollView> {
	private PullToRefreshScrollView pullToRefreshScrollView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_scrollview);
		
		pullToRefreshScrollView = (PullToRefreshScrollView)findViewById(R.id.pull_to_refresh_scrollview);
		pullToRefreshScrollView.setOnRefreshListener(this);
	}
	
	
	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ScrollView> view) {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {}
		}, 4000);
		
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ScrollView> view) {}

}
