package com.mstr.pulltorefresh.library;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public abstract class PullToRefreshBase<V extends View> extends LinearLayout {
	protected static final boolean DEBUG = true;
	
	protected static final String LOG_TAG = "PullToRefresh";
	
	private static final int FRICTION = 3;
	
	private static final long DEFAULT_SMOOTH_SCROLL_DURATION_MS = 200;
	
	protected V refreshableView;
	private FrameLayout refreshableViewWrapper;
	protected LoadingLayout headerLayout;
	
	private boolean isBeingDragged;
	
	private int touchSlop;
	
	private float initialMotionX;
	private float initialMotionY;
	
	private float lastMotionX;
	private float lastMotionY;
	
	private State state = State.RESET;
	
	private Interpolator scrollAnimationInterpolator;

	private OnRefreshListener<V> onRefreshListener;
	
	private boolean layoutVisibilityChangesEnabled = true;
	
	public PullToRefreshBase(Context context) {
		super(context);
	
		init(context, null);
	}
	
	public PullToRefreshBase(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context, attrs);
	}
	
	protected void init(Context context, AttributeSet attrs) {
		setOrientation(LinearLayout.VERTICAL);
		
		touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		
		headerLayout = createLoadingLayout(context);
		addViewInternal(headerLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		refreshableView = createRefreshableView(context, attrs);
		addRefreshableView(context, refreshableView);
	}
	
	private void addRefreshableView(Context context, View refreshableView) {
		refreshableViewWrapper = new FrameLayout(context);
		refreshableViewWrapper.addView(refreshableView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		addViewInternal(refreshableViewWrapper, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}
	
	protected LoadingLayout createLoadingLayout(Context context) {
		LoadingLayout loadingLayout = new RotateLoadingLayout(context);
		loadingLayout.setVisibility(View.INVISIBLE);
		
		return loadingLayout;
	}
	
	protected abstract V createRefreshableView(Context context, AttributeSet attrs);
	
	protected abstract boolean isReadyForPull();
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		
		if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
			isBeingDragged = false;
			return false;
		}
		
		if (action != MotionEvent.ACTION_DOWN && isBeingDragged) {
			return true;
		}
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (isReadyForPull()) {
				initialMotionX = lastMotionX = ev.getX();
				initialMotionY = lastMotionY = ev.getY();
				
				isBeingDragged = false;
			}
			break;
			
		case MotionEvent.ACTION_MOVE:
			if (isReadyForPull()) {
				float diffY = ev.getY() - lastMotionY;
				float diffX = ev.getX() - lastMotionX;
				
				// pull down event
				if (diffY >= touchSlop && Math.abs(diffY) > Math.abs(diffX)) {
					lastMotionX = ev.getX();
					lastMotionY = ev.getY();
					isBeingDragged = true;
				}
			}
			break;
		}
		
		return isBeingDragged;
    }
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN && ev.getEdgeFlags() != 0) {
			return false;
		}
		
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (isReadyForPull()) {
				initialMotionX = lastMotionX = ev.getX();
				initialMotionY = lastMotionY = ev.getY();
				return true;
			}
			break;
			
		case MotionEvent.ACTION_MOVE:
			if (isBeingDragged) {
				lastMotionX = ev.getX();
				lastMotionY = ev.getY();
				pullEvent();
				return true;
			}
			break;
			
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if (isBeingDragged) {
				isBeingDragged = false;
				
				if (state == State.RELEASE_TO_REFRESH && onRefreshListener != null) {
					setState(State.REFRESHING); 
				} else if (isRefreshing()) {
					smoothScrollTo(0);
				} else {
					setState(State.RESET);
				}
				
				return true;
			}
			
		}
		
		return false;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (DEBUG) {
			Log.d(LOG_TAG, "onSizeChanged" + w + " " + h + " " + oldw + " " + oldh);
		}
		
		super.onSizeChanged(w, h, oldw, oldh);
		
		refreshLoadingLayoutSize();
		
		post(new Runnable() {
			@Override
			public void run() {
				requestLayout();
			}
		});
	}
	
	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		if (refreshableView instanceof ViewGroup) {
			((ViewGroup)refreshableView).addView(child, index, params);
		} else {
			throw new UnsupportedOperationException("addView not supported");
		}
	}
	
	private void addViewInternal(View child, LayoutParams params) {
		super.addView(child, -1, params);
	}
	
	private void pullEvent() {
		int scrollY = Math.round(Math.min(initialMotionY - lastMotionY, 0) / FRICTION);
		
		setHeaderScroll(scrollY);
		
		if (!isRefreshing()) {
			int headerContentHeight = getHeaderContentHeight();
			
			headerLayout.onPull(Math.abs(scrollY) / (float)headerContentHeight);
			
			if (state != State.RELEASE_TO_REFRESH && Math.abs(scrollY) > headerContentHeight) {
				setState(State.RELEASE_TO_REFRESH);
			} else if (state != State.PULL_TO_REFRESH && Math.abs(scrollY) <= headerContentHeight) {
				setState(State.PULL_TO_REFRESH);
			}
		}
	}
	
	protected void setHeaderScroll(int scrollY) {
		if (DEBUG) {
			Log.d(LOG_TAG, "setHeaderScroll: " + scrollY);
		}
		final int maximumPullScroll = getMaximumPullScroll();
		scrollY = Math.min(maximumPullScroll, Math.max(-maximumPullScroll, scrollY));
		
		if (layoutVisibilityChangesEnabled) {
			if (scrollY < 0) {
				headerLayout.setVisibility(View.VISIBLE);
			} else {
				headerLayout.setVisibility(View.INVISIBLE);
			}
		}
		
		scrollTo(0, scrollY);
	}
	
	private int getMaximumPullScroll() {
		return getHeight() / FRICTION;
	}
	
	private boolean isRefreshing() {
		return state == State.REFRESHING || state == State.MANUAL_REFRESHING;
	}
	
	private void refreshLoadingLayoutSize() {
		int maximumPullScroll = Math.round(getMaximumPullScroll() * 1.2f);
		
		if (DEBUG) {
			Log.d(LOG_TAG, "maximumPullScroll: " + maximumPullScroll);
		}
		
		headerLayout.setHeight(maximumPullScroll);
		setPadding(getPaddingLeft(), -maximumPullScroll, getPaddingRight(), getPaddingBottom());
	}
	
	private void setState(State state) {
		switch (state) {
		case PULL_TO_REFRESH:
			headerLayout.pullToRefresh();
			break;
			
		case RELEASE_TO_REFRESH:
			headerLayout.releaseToRefresh();
			break;
			
		case REFRESHING:
		case MANUAL_REFRESHING:
			onRefreshing(true);
			break;
			
		case RESET:
			onReset();
			break;
		}
		
		this.state = state;
	}
	
	protected void disableLoadingLayoutVisibilityChanges() {
		layoutVisibilityChangesEnabled = false;
	}
	
	protected void onRefreshing(boolean shouldScroll) {
		headerLayout.refresh();
		
		if (shouldScroll) {
			OnSmoothScrollFinishedListener listener = new OnSmoothScrollFinishedListener() {
				public void onSmoothScrollFinished() {
					callRefreshListener();
				}
			};
			
			smoothScrollTo(-getHeaderContentHeight(), listener);
		} else {
			callRefreshListener();
		}
	}
	
	protected void onReset() {
		isBeingDragged = false;
		layoutVisibilityChangesEnabled = true;
		
		headerLayout.reset();
		
		smoothScrollTo(0);
	}
	
	public void onRefreshComplete() {
		if (isRefreshing()) {
			setState(State.RESET);
		}
	}
	
	protected int getHeaderContentHeight() {
		return headerLayout.getContentHeight();
	}
	
	protected void smoothScrollTo(int toY) {
		smoothScrollTo(toY, null);
	}
	
	protected void smoothScrollTo(int toY, OnSmoothScrollFinishedListener listener) {
		int oldScrollY = getScrollY();
		if (oldScrollY != toY) {
			if (scrollAnimationInterpolator == null) {
				scrollAnimationInterpolator = new DecelerateInterpolator();
			}
			
			post(new SmoothScrollRunnable(getScrollY(), toY, DEFAULT_SMOOTH_SCROLL_DURATION_MS, listener));
		}
	}
	
	public void setOnRefreshListener(OnRefreshListener<V> listener) {
		this.onRefreshListener = listener;
	}
	
	private void callRefreshListener() {
		if (onRefreshListener != null) {
			onRefreshListener.onPullDownToRefresh(this);
		}
	}
	
	public void manualRefresh() {
		if (!isRefreshing()) {
			setState(State.MANUAL_REFRESHING);
		}
	}
	
	@Override
	public Parcelable onSaveInstanceState() {
		SavedState savedState = new SavedState(super.onSaveInstanceState());
		savedState.stateValue = state.getValue();
		
		return savedState;
	}
	
	@Override
	public void onRestoreInstanceState(Parcelable parcelable) {
		if (!(parcelable instanceof SavedState)) {
			super.onRestoreInstanceState(parcelable);
			return;
		}
		
		SavedState savedState = (SavedState)parcelable;
		state = State.mapIntToValue(savedState.stateValue);
		if (state == State.REFRESHING || state == State.MANUAL_REFRESHING) {
			setState(state);
		}
		
		super.onRestoreInstanceState(savedState.getSuperState());
	}
	
	private static class SavedState extends BaseSavedState {
		int stateValue;
		
		public SavedState(Parcel source) {
			super(source);
			
			stateValue = source.readInt();
		}
		
		public SavedState(Parcelable superState) {
			super(superState);
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			
			dest.writeInt(stateValue);
		}
		
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel source) {
				return new SavedState(source);
			}
			
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
	
	public static interface OnRefreshListener<V extends View> {
		public void onPullDownToRefresh(PullToRefreshBase<V> view);
		
		public void onPullUpToRefresh(PullToRefreshBase<V> view);
	}
	
	private static enum State {
		RESET(0x0),
		
		PULL_TO_REFRESH(0x1),
		
		RELEASE_TO_REFRESH(0x2),
		
		REFRESHING(0x8),
		
		MANUAL_REFRESHING(0x9);
		
		private int value;
		
		private State(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
		
		public static State mapIntToValue(final int stateInt) {
			for (State state : State.values()) {
				if (state.value == stateInt) {
					return state;
				}
			}
			
			return RESET;
		}
	}
	
	private class SmoothScrollRunnable implements Runnable {
		private static final int INITIAL_NUMERIC_VALUE = -1;
		
		private int fromY;
		private int toY;
		private long duration;
		private long startTimeMillis = INITIAL_NUMERIC_VALUE;
		private int currentY = INITIAL_NUMERIC_VALUE;
		
		private boolean shouldStop;
		
		private OnSmoothScrollFinishedListener listener;
		
		public SmoothScrollRunnable(int fromY, int toY, long duration, OnSmoothScrollFinishedListener listener) {
			this.fromY = fromY;
			this.toY = toY;
			this.duration = duration;
			this.listener = listener;
		}
		
		@Override
		public void run() {
			if (startTimeMillis == INITIAL_NUMERIC_VALUE) {
				startTimeMillis = System.currentTimeMillis();
			} else {
				long normalizedTime = (1000 * (System.currentTimeMillis() - startTimeMillis)) / duration;
				normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);
				
				final int deltaY = Math.round((fromY - toY) * scrollAnimationInterpolator.getInterpolation(normalizedTime / 1000f));
				currentY = fromY - deltaY;
				setHeaderScroll(currentY);
				
				if (DEBUG) {
					Log.d(LOG_TAG, "SmoothScrollRunnable, currentY " + currentY);
				}
			}
			
			if (!shouldStop && currentY != toY) {
				ViewCompat.postOnAnimation(PullToRefreshBase.this, this);
			} else {
				if (listener != null) {
					listener.onSmoothScrollFinished();
				}
			}
		}
		
		public void stop() {
			shouldStop = true;
			
			removeCallbacks(this);
		}
	}
	
	private static interface OnSmoothScrollFinishedListener {
		public void onSmoothScrollFinished();
	}
}