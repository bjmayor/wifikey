package com.hiwifi.model.speedtest;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

public abstract class DiyViewBase extends View {

	public float width = 0, height = 0;
	public int fps = 0, dpi;
	private boolean canRefresh = true, sizeFromOutside = false;
	private boolean needRefresh=false;
	private Timer mTimer;
	// ================cache graphic
	private boolean useCache = false;
	private Bitmap cacheBitmap = null;
	private Canvas cacheCanvas = null;
	Paint bmpPaint = new Paint();
	// ================
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) 
				DiyViewBase.this.invalidate();
			else
				DiyViewBase.this.handleMessage(msg);
		}
	};
	
	public void RefreshNext(){
		needRefresh=true;
	}
	
	public DiyViewBase(Context context) {
		super(context);
		initialize(context);
	}

	public DiyViewBase(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}

	public DiyViewBase(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initialize(context);
	}

	private void initialize(Context context){
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		dpi= metrics.densityDpi;
		initView();
	}

	public void sendMessage(Message message){
		handler.sendMessage(message);
	}
	
	public void setFPS(int fps, int delay) {
		if (mTimer != null)
			mTimer.cancel();
		if (fps > 0) {
			this.fps = fps;
			fps = fps > 100 ? 10 : (int) (1000 / fps);
			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (timerTask()||needRefresh) {
						Message message = new Message();
						message.what = 1;
						handler.sendMessage(message);
						needRefresh=false;
					}
				}
			}, delay, fps);
		}
	}

	public boolean isUseCache() {
		return this.useCache;
	}

	public void setImgCache(boolean useCache) {
		this.useCache = useCache;
	}

	public void setSize(float width, float height) {
		this.width = width;
		this.height = height;
		sizeFromOutside = true;
	}

	public Paint newPaint(boolean antiAlias, int color, Style style) {
		Paint ret = new Paint();
		ret.setAntiAlias(antiAlias);
		ret.setColor(color);
		ret.setStyle(style);
		return ret;
	}

	public Paint newPaint(boolean antiAlias, int color, Style style,
			Align textAlign, float textSize, float strokeWidth) {
		Paint ret = new Paint();
		ret.setAntiAlias(antiAlias);
		ret.setColor(color);
		ret.setStyle(style);
		ret.setTextAlign(textAlign);
		ret.setTextSize(textSize);
		ret.setStrokeWidth(strokeWidth);
		return ret;
	}

	public final void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (this.canRefresh) {
			this.canRefresh = false;
			if (this.useCache) {
				canvas.drawBitmap(cacheBitmap, 0, 0, bmpPaint);
				this.refreshView(this.cacheCanvas);
			} else {
				this.setBackground(canvas);
				this.refreshView(canvas);
			}
			this.canRefresh = true;
		}
	}

	@Override
	protected final void onLayout(boolean changed, int left, int top,
			int right, int bottom) {
		int w = right - left, h = bottom - top;
		this.finishLayout(changed, left, top, right, bottom);
		if (sizeFromOutside) {
			if ((w > 0) && (w < width))
				width = w;
			else
				width -= left;
			if ((h > 0) && (h < height))
				height = h;
			else
				height -= top;

		} else {
			width = w;
			height = h;
		}

		super.setMeasuredDimension((int) width, (int) height);
		if ((width > 0) && (height > 0)) {
			setCacheGraphic();
		}
	}

	private void setCacheGraphic() {
		cacheBitmap = Bitmap.createBitmap((int) width, (int) height,
				Config.ARGB_8888);
		cacheCanvas = new Canvas();
		cacheCanvas.setBitmap(cacheBitmap);
		setBackground(cacheCanvas);
	}

	@Override
	public abstract boolean onTouchEvent(MotionEvent event);

	public abstract void handleMessage(Message message);
	// @Override
	// protected abstract void onMeasure(int widthMeasureSpec,int
	// heightMeasureSpec);
	protected abstract void finishLayout(boolean changed, int left, int top,
			int right, int bottom);

	public abstract float getScroll();

	public abstract void setBackground(Canvas canvas);

	public abstract void refreshView(Canvas canvas);

	public abstract void initView();

	public abstract boolean timerTask();
}