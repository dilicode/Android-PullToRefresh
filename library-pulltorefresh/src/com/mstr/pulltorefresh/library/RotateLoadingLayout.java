package com.mstr.pulltorefresh.library;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView.ScaleType;

public class RotateLoadingLayout extends LoadingLayout {
	private static final boolean DEBUG = true;
	private static final String LOG_TAG = "RotateLoadingLayout";
	
	private static final int ROTATION_ANIMATION_DURATION = 1200;
	
	private Drawable imageDrawable;
	
	private int pivotX;
	private int pivotY;
	
	private Matrix matrix;
	
	private RotateAnimation refreshRotateAnimation;
	
	public RotateLoadingLayout(Context context) {
		super(context);
		
		headerImage.setScaleType(ScaleType.MATRIX);
		matrix = new Matrix();
		headerImage.setImageMatrix(matrix);
		
		imageDrawable = getResources().getDrawable(R.drawable.default_rotate);
		headerImage.setImageDrawable(imageDrawable);
		
		pivotY = Math.round(imageDrawable.getIntrinsicHeight() / 2f);
		pivotX = Math.round(imageDrawable.getIntrinsicWidth() / 2f);
	
		refreshRotateAnimation = new RotateAnimation(0f, 720f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		refreshRotateAnimation.setInterpolator(new LinearInterpolator());
		refreshRotateAnimation.setDuration(ROTATION_ANIMATION_DURATION);
		refreshRotateAnimation.setRepeatCount(Animation.INFINITE);
		refreshRotateAnimation.setRepeatMode(Animation.RESTART);
	}

	@Override
	protected void onPull(float scaleValue) {
		if (DEBUG) {
			Log.d(LOG_TAG, "scaleValue: " + scaleValue);
		}
		
		matrix.setRotate(scaleValue * 90, pivotX, pivotY);
		headerImage.setImageMatrix(matrix);
	}
	
	@Override
	public void refresh() {
		super.refresh();
		
		headerImage.startAnimation(refreshRotateAnimation);
	}
	
	@Override
	public void reset() {
		super.reset();
		
		headerImage.clearAnimation();
		matrix.reset();
		headerImage.setImageMatrix(matrix);
	}
}