package com.hiwifi.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.hiwifi.hiwifi.R;

public class GradualBackground extends LinearLayout {

	private Context context;
	private Paint paint = new Paint();
	private int width, height;
	private Rect rect;
	private int color;
	private int delay = 25;
	private int count = 0;
	private boolean isbegin = false;
	private boolean isReverse = false;
	private StatuColor statuColor = StatuColor.PING_SUCCESS;
	private int colorBegin = getResources().getColor(R.color.success_bg),
			colorEnd = getResources().getColor(R.color.need_val);
	private View view;

	public enum StatuColor {
		PING_SUCCESS, PING_FAILURE,PING_SUCCESS_TO_FAILURE,PING_FAILURE_TO_SUCCESS;
	}

	public void setIsbegin(boolean isbegin) {
		this.isbegin = isbegin;
		count = 0;
		invalidate();
	}

	public boolean isReverse() {
		return isReverse;
	}

	public void setReverse(boolean isReverse) {
		this.isReverse = isReverse;
	}

	public void setColor(int state) {
		this.color = color;
	}

	public int getColor() {
		return color;
	}

	public void setStatu(StatuColor statuColor) {
		this.statuColor = statuColor;
		if (this.statuColor == StatuColor.PING_SUCCESS) {
			colorBegin = getResources().getColor(R.color.success_bg);
//			colorEnd = getResources().getColor(R.color.success_bg);
		} else if (this.statuColor == StatuColor.PING_FAILURE) {
			colorBegin = getResources().getColor(R.color.need_val);
//			colorEnd = getResources().getColor(R.color.need_val);
		}else if(this.statuColor == StatuColor.PING_SUCCESS_TO_FAILURE){
			colorBegin = getResources().getColor(R.color.success_bg);
			colorEnd = getResources().getColor(R.color.need_val);
		}else if(this.statuColor == StatuColor.PING_FAILURE_TO_SUCCESS){
			colorBegin = getResources().getColor(R.color.need_val);
			colorEnd = getResources().getColor(R.color.success_bg);
		}
	}
	public StatuColor getStatuColor() {
		return statuColor;
	}

	public GradualBackground(Context context) {
		super(context);
		this.context = context;
		init();
		// TODO Auto-generated constructor stub
	}

	public GradualBackground(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
		// TODO Auto-generated constructor stub
	}

	public GradualBackground(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		init();
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.dispatchDraw(canvas);
		gradualColor(paint, canvas, colorBegin, colorEnd);
	}

	@Override
	protected void onAttachedToWindow() {
		// TODO Auto-generated method stub
		super.onAttachedToWindow();
		setPaints();
	}

	private void init() {
		view = LayoutInflater.from(context).inflate(R.layout.leftmenu, null);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		width = measureWidth(widthMeasureSpec);
		height = measureHeight(heightMeasureSpec);
		setMeasuredDimension(width, height);
		rect = new Rect(0, 0, width, height);
	}

	private void setPaints() {
		// paint.setColor(getColor());
		paint.setAntiAlias(true);
		paint.setStyle(Style.FILL);
	}

	/**
	 * Determines the width of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The width of the view, honoring constraints from measureSpec
	 */
	private int measureWidth(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		}

		return result;
	}

	/**
	 * Determines the height of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The height of the view, honoring constraints from measureSpec
	 */
	private int measureHeight(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		}
		return result;
	}

	private Handler handler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			invalidate();
			return false;
		}
	});

	private void gradualColor(Paint paint, Canvas canvas, int colorBegin,
			int colorEnd) {
		if (isbegin) {
			// while (count < delay) {
			// 具体实现就不用多说了，大致就是把RGB各个颜色值进行加减运算，然后组合成一种RGB颜色值，这个方法是我封装好的现成的
			int r0 = (colorBegin >> 16) & 0xff;
			int r1 = (colorEnd >> 16) & 0xff;
			int g0 = (colorBegin >> 8) & 0xff;
			int g1 = (colorEnd >> 8) & 0xff;
			int b0 = (colorBegin) & 0xff;
			int b1 = (colorEnd) & 0xff;
			int F, r, g, b;
			// for (int i = 0; i < w; ++i) {
			//
			F = (count << 16) / delay;
			r = r0 + ((F * (r1 - r0)) >> 16);
			g = g0 + ((F * (g1 - g0)) >> 16);
			b = b0 + ((F * (b1 - b0)) >> 16);
			paint.setARGB(0xff, r, g, b);
			count++;
			canvas.drawRect(rect, paint);
			Log.d("count:", count + "");
			if (count < delay) {
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						handler.sendEmptyMessage(0x100);
					}
				}, 10);
			} else {
//				this.colorBegin = colorEnd;
//				this.colorEnd = colorBegin;
				isbegin = false;
			}
			// }
		} else {
			if(statuColor == StatuColor.PING_FAILURE_TO_SUCCESS || statuColor == StatuColor.PING_SUCCESS_TO_FAILURE){
				paint.setColor(colorEnd);
			}else {
				paint.setColor(colorBegin);
			}
			canvas.drawRect(rect, paint);
		}
	}
}
