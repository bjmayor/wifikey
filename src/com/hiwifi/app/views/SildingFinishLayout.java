package com.hiwifi.app.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Scroller;

/**
 * 自定义可以滑动的RelativeLayout, 类似于IOS的滑动删除页面效果，当我们要使用
 * 此功能的时候，需要将该Activity的顶层布局设置为SildingFinishLayout，
 * 然后需要调用setTouchView()方法来设置需要滑动的View
 * 
 * @author xiaanming
 * 
 * @blog http://blog.csdn.net/xiaanming
 * 
 */
public class SildingFinishLayout extends RelativeLayout implements
		OnTouchListener {
	/**
	 * SildingFinishLayout布局的父布局
	 */
	private ViewGroup mParentView;
	/**
	 * 处理滑动逻辑的View
	 */
	private View touchView;
	/**
	 * 滑动的最小距离
	 */
	private int mTouchSlop;
	/**
	 * 按下点的X坐标
	 */
	private int downX;
	/**
	 * 按下点的Y坐标
	 */
	private int downY;
	/**
	 * 临时存储X坐标
	 */
	private int tempX;
	/**
	 * 滑动类
	 */
	private Scroller mScroller;
	/**
	 * SildingFinishLayout的宽度
	 */
	private int viewWidth;
	/**
	 * 记录是否正在滑动
	 */
	private boolean isSilding;
	private boolean isFollowTouch = true;
	private boolean isFinish;
	private boolean isSildingUp;

	private OnSildingFinishListener onSildingFinishListener;
	private View outview;
	private int min_x;

	public void setIsFollowTouch(boolean isFollowTouch) {
		this.isFollowTouch = isFollowTouch;
	}

	public SildingFinishLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SildingFinishLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		mScroller = new Scroller(context);

	}

	/**
	 * 设置OnSildingFinishListener, 在onSildingFinish()方法中finish Activity
	 * 
	 * @param onSildingFinishListener
	 */
	public void setOnSildingFinishListener(
			OnSildingFinishListener onSildingFinishListener) {
		this.onSildingFinishListener = onSildingFinishListener;
	}

	/**
	 * 设置Touch的View
	 * 
	 * @param touchView
	 */
	public void setTouchView(View touchView) {
		this.touchView = touchView;
		touchView.setOnTouchListener(this);
	}

	public View getTouchView() {
		return touchView;
	}

	@SuppressLint("Recycle")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downX = tempX = (int) event.getRawX();
			downY = (int) event.getRawY();
			isSildingUp = false;
			isSilding = false;
			break;
		case MotionEvent.ACTION_MOVE:
			int moveX = (int) event.getRawX();
			int moveY = (int) event.getRawY();
			int deltaX = tempX - moveX;
			tempX = moveX;

			if (!isSildingUp && downX < viewWidth * 0.8f) {
				if (!isSilding && Math.abs(moveX - downX) > mTouchSlop) {
					if (Math.abs(moveY - downY) < (Math.abs(moveX - downX) >> 1)) {
						isSilding = true;
					} else {
						isSildingUp = true;
					}
				}
			} else {
				v.onTouchEvent(event);
			}
			if (isSilding) {
				// 若touchView是AbsListView，
				// 则当手指滑动，取消item的点击事件，不然我们滑动也伴随着item点击事件的发生
				// if (isTouchOnAbsListView(v)) {
				MotionEvent cancelEvent = MotionEvent.obtain(event);
				cancelEvent
						.setAction(MotionEvent.ACTION_CANCEL
								| (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
				v.onTouchEvent(cancelEvent);
				// }
				isFinish = deltaX < -min_x;
			}
			if (isFollowTouch && isSilding) {
				int temp = moveX - downX;
				// if (temp >= 0) {
				// mParentView.scrollTo(-temp, 0);
				// } else {
				// mParentView.scrollTo(0, 0);
				// }
				if (temp > viewWidth * 0.75) {
					isFinish = true;
				}
				if (temp >= 0) {
					scrollTo(-temp, 0);
					setBackgroundAlpha(temp);
				} else {
					scrollTo(0, 0);
				}
				// 屏蔽在滑动过程中ListView ScrollView等自己的滑动事件
				if (isTouchOnScrollView(v) || isTouchOnAbsListView(v)
						|| isWebView(v) || isTouchOnViewPager(v)) {
					return true;
				}
			} else if (isTouchOnScrollView(v) || isTouchOnAbsListView(v)
					|| isWebView(v)|| isTouchOnViewPager(v)) {
				v.onTouchEvent(event);
				return true;
			}
			{

			}
			break;
		case MotionEvent.ACTION_UP:
			// if (mParentView.getScrollX() <= -viewWidth / 3) {
			// isFinish = true;
			// scrollRight();
			// } else {
			// scrollOrigin();
			// isFinish = false;
			// }
			if (isFinish) {
				scrollRight();
			} else {
				scrollOrigin();
			}
			if (isSilding) {
				isSilding = false;
				return true;
			}
			break;
		}
		// 假如touch的view是AbsListView或者ScrollView 我们处理完上面自己的逻辑之后
		// 再交给AbsListView, ScrollView自己处理其自己的逻辑
		if (isTouchOnScrollView(v) || isTouchOnAbsListView(v) || isWebView(v)|| isTouchOnViewPager(v)) {
			return v.onTouchEvent(event);
		} else {
			v.onTouchEvent(event);
			// 其他的情况直接返回true
			return true;
		}
	}

	@Override
	public void computeScroll() {
		// 调用startScroll的时候scroller.computeScrollOffset()返回true，
		if (mScroller.computeScrollOffset()) {
			// mParentView.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			float temp = -getScrollX();
			setBackgroundAlpha(temp);
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
			if (mScroller.isFinished() || temp >= (viewWidth * 0.97f - 10)) {
				outview.setAlpha(0f);
				if (onSildingFinishListener != null && isFinish) {
					onSildingFinishListener.onSildingFinish();
				}
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed) {
			// 获取SildingFinishLayout所在布局的父布局
			mParentView = (ViewGroup) this.getParent();
			viewWidth = this.getWidth();
			if (outview == null) {
				addBackgroundView();
			}
			// mTouchSlop = (int) (viewWidth * 0.1f);
			mTouchSlop = 5;
			min_x = viewWidth / 150;
		}
	}

	private void setBackgroundAlpha(float temp) {
		float alpha = 1f - ((float) temp / viewWidth);
		if (alpha > 0.8f) {
			alpha = 0.8f;
		}
		outview.setAlpha(alpha);
	}

	private void addBackgroundView() {
		outview = new View(getContext());
		outview.setBackgroundColor(Color.BLACK);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		mParentView.addView(outview, 0, params);
	}

	/**
	 * 滚动出界面
	 */
	private void scrollRight() {
		if (isFollowTouch) {
			final int delta = (viewWidth + /* mParentView. */getScrollX());
			// 调用startScroll方法来设置一些滚动的参数，我们在computeScroll()方法中调用scrollTo来滚动item
			mScroller.startScroll(/* mParentView. */getScrollX(), 0, -delta + 1,
					0, Math.abs(delta / 4) + 1);
			postInvalidate();
		} else {
			if (onSildingFinishListener != null && isFinish) {
				onSildingFinishListener.onSildingFinish();
			}
		}
	}

	/**
	 * 滚动到起始位置
	 */
	private void scrollOrigin() {
		// int delta = mParentView.getScrollX();
		// mScroller.startScroll(mParentView.getScrollX(), 0, -delta, 0,
		// Math.abs(delta / 4));
		// postInvalidate();
		int delta = this.getScrollX();
		mScroller.startScroll(this.getScrollX(), 0, -delta, 0,
				Math.abs(delta / 4));
		postInvalidate();
	}

	private boolean isWebView(View touchView) {
		return touchView instanceof WebView ? true : false;
	}

	/**
	 * touch的View是否是AbsListView， 例如ListView, GridView等其子类
	 * 
	 * @return
	 */
	private boolean isTouchOnAbsListView(View touchView) {
		return touchView instanceof AbsListView ? true : false;
	}

	/**
	 * touch的view是否是ScrollView或者其子类
	 * 
	 * @return
	 */
	private boolean isTouchOnScrollView(View touchView) {
		boolean ret = touchView instanceof ScrollView ? true : false;
		if (ret) {
			ScrollView sv = (ScrollView) touchView;
			return sv.getScrollY() != 0 || sv.getScrollX() != 0;
		}
		return false;
	}
	
	private boolean isTouchOnViewPager(View touchView) {
		boolean ret = touchView instanceof ViewPager ? true : false;
		if (ret) {
			ViewPager sv = (ViewPager) touchView;
			return sv.getScrollY() != 0 || sv.getScrollX() != 0;
		}
		return false;
	}

	public interface OnSildingListener {
		public void onSilding();
	}

	public interface OnSildingFinishListener {
		public void onSildingFinish();
	}

}
