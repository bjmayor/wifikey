package com.hiwifi.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Scroller;

public class ScrollableLayout extends LinearLayout{

	    private Scroller scroller;

	    public ScrollableLayout(Context context) {
	        super(context);
	        init();
	    }

	    public ScrollableLayout(Context context, AttributeSet attrs) {
	        super(context, attrs);
	        init();
	    }

	    public ScrollableLayout(Context context, AttributeSet attrs, int defStyle) {
	        super(context, attrs, defStyle);
	        init();
	    }

	    private void init(){
	        scroller = new Scroller(getContext());
	    }

	    @Override
	    public void scrollTo(int x, int y) {
	        super.scrollTo(x, y);
	        postInvalidate();
	    }

	    @Override
	    public void computeScroll() {
	        if (!scroller.isFinished()) {
	            if (scroller.computeScrollOffset()) {
	                int oldX = getScrollX();
	                int oldY = getScrollY();
	                int x = scroller.getCurrX();
	                int y = scroller.getCurrY();
	                if (oldX != x || oldY != y) {
	                    scrollTo(x, y);
	                }
	                // Keep on drawing until the animation has finished.
	                invalidate();
	            } else {
	                clearChildrenCache();
	            }
	        } else {
	            clearChildrenCache();
	        }
	    }

	   public void smoothScrollTo(int dy, int duration) {

	        int oldScrollX = getScrollX();
	        int oldScrollY = getScrollY();
	        scroller.startScroll(oldScrollX, oldScrollY, getScrollX(), dy, duration);
	        invalidate();
	    }

	    private void enableChildrenCache() {
	        final int count = getChildCount();
	        for (int i = 0; i < count; i++) {
	            final View layout = (View) getChildAt(i);
	            layout.setDrawingCacheEnabled(true);
	        }
	    }

	    private void clearChildrenCache() {
	        final int count = getChildCount();
	        for (int i = 0; i < count; i++) {
	            final View layout = (View) getChildAt(i);
	            layout.setDrawingCacheEnabled(false);
	        }
	    }

}
