package com.mstr.pulltorefresh.library;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class LoadingLayout extends FrameLayout {
	private FrameLayout innerLayout;
	private ImageView headerImage;
	private TextView headerText;
	private TextView subHeaderText;
	
	public LoadingLayout(Context context) {
		super(context);
		
		LayoutInflater.from(getContext()).inflate(R.layout.loading_layout, this);
		
		innerLayout = (FrameLayout)findViewById(R.id.fl_inner);
		headerImage = (ImageView)findViewById(R.id.pull_to_refresh_image);
		headerText = (TextView)findViewById(R.id.pull_to_refresh_text);
		subHeaderText = (TextView)findViewById(R.id.pull_to_refresh_sub_text);
	}
	
	public void setHeight(int height) {
		ViewGroup.LayoutParams layoutParams = getLayoutParams();
		if (layoutParams != null) {
			if (layoutParams.height != height) {
				layoutParams.height = height;
				requestLayout();
			}
		}
	}
	
	public int getContentHeight() {
		return innerLayout.getHeight();
	}
	
	public void pullToRefresh() {
		headerText.setText(R.string.pull_to_refresh);
	}
	
	public void releaseToRefresh() {
		headerText.setText(R.string.release_to_refresh);
	}
	
	public void refresh() {
		headerText.setText(R.string.refreshing);
		subHeaderText.setVisibility(View.GONE);
	}
	
	public void reset() {
		headerText.setText(R.string.pull_to_refresh);
	}
	
	public void hideAllViews() {
		if (headerText.getVisibility() == View.VISIBLE) {
			headerText.setVisibility(View.INVISIBLE);
		}
		
		if (headerImage.getVisibility() == View.VISIBLE) {
			headerImage.setVisibility(View.INVISIBLE);
		}
		
		if (subHeaderText.getVisibility() == View.VISIBLE) {
			subHeaderText.setVisibility(View.INVISIBLE);
		}
	}
	
	public void showAllViews() {
		if (headerText.getVisibility() == View.INVISIBLE) {
			headerText.setVisibility(View.VISIBLE);
		}
		
		if (headerImage.getVisibility() == View.INVISIBLE) {
			headerImage.setVisibility(View.VISIBLE);
		}
		
		if (subHeaderText.getVisibility() == View.INVISIBLE) {
			subHeaderText.setVisibility(View.VISIBLE);
		}
	}
}