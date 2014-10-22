package com.mstr.pulltorefresh.library;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class PullToRefreshScrollView extends PullToRefreshBase<ScrollView>{

	public PullToRefreshScrollView(Context context) {
		super(context);
	}
	
	public PullToRefreshScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected ScrollView createRefreshableView(Context context,
			AttributeSet attrs) {
		return new ScrollView(context, attrs);
	}

	@Override
	protected boolean isReadyForPull() {
		return refreshableView.getScrollY() == 0;
	}

}
