package com.mstr.pulltorefresh.library;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

public class PullToRefreshListView extends PullToRefreshBase<ListView> {
	private LoadingLayout headerLoadingLayout;
	
	public PullToRefreshListView(Context context) {
		super(context);
	}
	
	public PullToRefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	} 
	
	@Override
	protected ListView createRefreshableView(Context context, AttributeSet attrs) {
		return new ListView(context, attrs);
	}
	
	@Override
	protected boolean isReadyForPull() {
		Adapter adapter = refreshableView.getAdapter();
		
		if (null == adapter || adapter.isEmpty()) {
			return true;
		} else {
			return refreshableView.getFirstVisiblePosition() == 0;
		}
	}
	
	@Override
	protected void onRefreshing(boolean shouldScroll) {
		super.onRefreshing(false);
		
		headerLayout.reset();
		headerLayout.hideAllViews();
		
		headerLoadingLayout.setVisibility(View.VISIBLE);
		headerLoadingLayout.refresh();
		
		if (shouldScroll) {
			disableLoadingLayoutVisibilityChanges();

			// We scroll slightly so that the ListView's header/footer is at the
			// same Y position as our normal header/footer
			setHeaderScroll(getScrollY() + getHeaderContentHeight());
			
			refreshableView.setSelection(0);
			
			smoothScrollTo(0);
		}
	}
	
	@Override
	protected void onReset() {
		if (headerLoadingLayout.getVisibility() == View.VISIBLE) {
			headerLayout.showAllViews();
			
			headerLoadingLayout.setVisibility(View.GONE);
			
			setHeaderScroll(-getHeaderContentHeight());
		}
		
		super.onReset();
	}
	
	@Override
	protected void init(Context context, AttributeSet attrs) {
		super.init(context, attrs);
		
		FrameLayout frameLayout = new FrameLayout(getContext());
		headerLoadingLayout = createLoadingLayout(getContext());
		headerLoadingLayout.setVisibility(View.GONE);
		frameLayout.addView(headerLoadingLayout, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		refreshableView.addHeaderView(frameLayout, null, false);
	}
	
	public void setAdapter(ListAdapter adapter) {
		refreshableView.setAdapter(adapter);
	}
}