package com.hiwifi.app.views;

import java.util.Timer;
import java.util.TimerTask;

import com.hiwifi.hiwifi.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class TextViewAdvance extends View {

	private float mid_y;
	private float top_y;
	private float old_y;
	private float new_y;
	private float text_x;
	private float text_height;
	private int new_alpha;
	private int old_alpha;
	private int draw_cnt;
	private int r, g, b;
	private int ar, ag, ab;
	private int nr, ng, nb;
	private boolean is_showing;
	private boolean is_hiding;
	private boolean is_same_time;
	private boolean is_center;
	private boolean have_background_color;
	private Paint text_paint;
	private String new_str;
	private String old_str;
	private Timer mTimer;

	public void setNoBackgroundColor() {
		have_background_color = false;
	}

	public void setBackgroundColor(int tr, int tg, int tb) {
		nr = tr;
		ng = tg;
		nb = tb;
		if (!have_background_color) {
			have_background_color = true;
			r = nr;
			b = nb;
			g = ng;
		}
		ar = (nr - r) >> 6;
		ag = (ng - g) >> 6;
		ab = (nb - b) >> 6;
		if (ar == 0) {
			if (nr > r) {
				ar = 1;
			} else {
				ar = -1;
			}
		}
		if (ag == 0) {
			if (ng > g) {
				ag = 1;
			} else {
				ag = -1;
			}
		}
		if (ab == 0) {
			if (nb > b) {
				ab = 1;
			} else {
				ab = -1;
			}
		}

	}

	public TextViewAdvance(Context context) {
		super(context);
		initVeiw(context);
	}

	public TextViewAdvance(Context context, AttributeSet attrs) {
		super(context, attrs);
		initVeiw(context);
		get_value(context, attrs, 0);
	}

	public TextViewAdvance(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initVeiw(context);
		get_value(context, attrs, defStyleAttr);
	}

	public void show_new_str(String new_str) {
		if (new_str != null) {
			if (mTimer != null) {
				old_str = String.copyValueOf(this.new_str.toCharArray());
				this.new_str = null;
			}
			this.new_str = new_str;
			new_y = mid_y + text_height;
			old_y = mid_y;
			new_alpha = 0;
			old_alpha = 255;
			is_hiding = true;
			is_showing = is_same_time;
			setFPS(60, 0);
		}
	}

	public void set_same_time_load(boolean is_same_time) {
		this.is_same_time = is_same_time;
	}

	public void set_typeface(Typeface tf) {
		text_paint.setTypeface(tf);
	}

	public void set_text(String str) {
		old_str = String.copyValueOf(str.toCharArray());
		str = null;
		postInvalidate();
	}

	public void set_text_size(float size) {
		text_paint.setTextSize(size);
		FontMetricsInt fmi = text_paint.getFontMetricsInt();
		text_height = fmi.bottom - fmi.top;
		requestLayout();
		fmi = null;
	}

	public void set_text_color(int color) {
		text_paint.setColor(color);
	}

	// @Override
	// public boolean onTouchEvent(MotionEvent event) {
	// show_new_str("hehe" + String.valueOf(Math.random() * 100));
	// return super.onTouchEvent(event);
	// }

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		setMeasuredDimension(width, (int) (text_height * 3));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float temp = text_height * 0.4f;
		if (have_background_color) {
			canvas.drawColor(Color.rgb(r, g, b));
		}
		if (draw_cnt < 2) {
			draw_cnt++;
		}
		if (old_str != null) {
			text_paint.setAlpha(old_alpha);
			canvas.drawText(old_str, text_x, old_y + temp, text_paint);
		}
		if (new_str != null) {
			text_paint.setAlpha(new_alpha);
			canvas.drawText(new_str, text_x, new_y + temp, text_paint);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			int w = (right - left) >> 1;
			mid_y = (bottom - top) >> 1;
			top_y = mid_y - text_height;
			old_y = mid_y;
			new_y = mid_y + text_height;
			if (is_center) {
				text_paint.setTextAlign(Align.CENTER);
				text_x = w;
			} else {
				text_paint.setTextAlign(Align.LEFT);
				text_x = 0;
			}
		}
	}

	private boolean moving() {
		float err;
		if (is_hiding) {
			err = (old_y - top_y);
			if (err < 0) {
				is_hiding = false;
				is_showing = true;
			} else {
				old_y -= err / 10f + 0.4f;
				if (err < 255) {
					err *= 4;
				}
				old_alpha = (int) (err > 255 ? 255 : (err < 0 ? 0 : err));
			}
		}
		if (is_showing) {
			err = (new_y - mid_y);
			if (err < 0) {
				is_hiding = false;
				is_showing = false;
			} else {
				new_y -= err / 10f + 0.4f;
				if (err < 255) {
					err *= 4;
				}
				new_alpha = (int) (err > 255 ? 0 : (err < 0 ? 255 : 255 - err));
			}
		}
		return is_hiding || is_showing;
	}

	private void get_value(Context context, AttributeSet attrs, int defStyleAttr) {
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.TextViewAdvance, defStyleAttr, 0);
		if (a != null) {
			// 获得参数个数
			int count = a.getIndexCount();
			int index = 0;
			// 遍历参数。先将index从TypedArray中读出来，
			// 得到的这个index对应于attrs.xml中设置的参数名称在R中编译得到的数
			// 这里会得到各个方向的宽和高
			for (int i = 0; i < count; i++) {
				index = a.getIndex(i);
				switch (index) {
				case R.styleable.TextViewAdvance_text:
					String str = a.getString(index);
					set_text(str);
					break;
				case R.styleable.TextViewAdvance_text_color:
					int c = a.getColor(index, 0);
					set_text_color(c);
					break;
				case R.styleable.TextViewAdvance_text_in_center:
					is_center = a.getBoolean(index, false);
					break;
				case R.styleable.TextViewAdvance_text_size:
					set_text_size(a.getDimension(index, 15f));
					break;
				case R.styleable.TextViewAdvance_text_same_time_load:
					set_same_time_load(a.getBoolean(index, false));
					break;
				}
			}
			a.recycle();
		}
	}

	private void initVeiw(Context context) {
		text_paint = new Paint();
		text_paint.setTextAlign(Align.LEFT);
		text_paint.setAlpha(255);
		text_paint.setStyle(Style.FILL);
		set_text_size(50);
		set_text_color(Color.BLACK);
		old_alpha = 255;
		new_alpha = 255;
		draw_cnt = 0;
		is_center = false;
		is_same_time = false;
	}

	private void setFPS(int fps, int delay) {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		if (fps > 0) {
			int freah_time = fps > 100 ? 10 : (int) (1000 / fps);
			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					boolean refresh = false;
					if (!moving()) {
						mTimer.cancel();
						old_str = String.copyValueOf(new_str.toCharArray());
						new_str = null;
						mTimer = null;
						old_y = mid_y;
						new_y = mid_y + text_height;
						old_alpha = 255;
						new_alpha = 0;
						refresh = true;
					}
					if (have_background_color) {
						if ((ar > 0 && r < nr) || (ar < 0 && r > nr)) {
							r += ar;
							refresh = true;
						}
						if ((ag > 0 && g < ng) || (ag < 0 && g > ng)) {
							g += ag;
							refresh = true;
						}
						if ((ab > 0 && b < nb) || (ab < 0 && b > nb)) {
							b += ab;
							refresh = true;
						}
						r = r & 0xff;
						g = g & 0xff;
						b = b & 0xff;
					}
					if (refresh) {
						if (1 >= draw_cnt) {
							postInvalidate();
						}
					}
					if (draw_cnt > 0) {
						draw_cnt--;
					}
				}
			}, delay, freah_time);
		}
	}
}
