package com.hiwifi.app.views;

import java.util.Timer;
import java.util.TimerTask;

import com.hiwifi.hiwifi.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class UpMoveBar extends View {

	private int width;
	private int height;
	private int draw_cnt;
	private int back_color;
	private int line_width;
	private int line_height;
	private int fps;
	private float cycle_r;
	private float line_top;
	private float set_speed;
	private float move_speed;
	private boolean is_set_r_by_outside;
	private boolean is_set_speed_by_outside;
	private Timer mTimer;
	private Path mask_path;
	private RectF mask_area;
	private Paint line_paint;
	private Paint mask_paint;

	public UpMoveBar(Context context) {
		super(context);
		init_view(context);
	}

	public UpMoveBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init_view(context);
		set_from_xml(context, attrs, 0);
	}

	public UpMoveBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init_view(context);
		set_from_xml(context, attrs, defStyleAttr);
	}

	public void start() {
		setFPS(fps, 0);
	}

	public void stop() {
		setFPS(0, 0);
	}

	public void set_round_r(float r) {
		cycle_r = r;
		requestLayout();
		is_set_r_by_outside = true;
	}

	public void set_mask_color(int color) {
		mask_paint.setColor(color);
	}

	public void set_move_speed(float speed) {
		set_speed = speed;
		requestLayout();
		is_set_speed_by_outside = true;
	}

	public void set_line_color(int color) {
		line_paint.setColor(color);
	}

	public void set_background_color(int color) {
		back_color = color;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			width = right - left;
			height = bottom - top;
			int temp = height >> 1;
			if (width > temp) {
				width = temp;
			}
			setMeasuredDimension(width, height);
			float r = cycle_r;
			if (!is_set_r_by_outside) {
				r = (float) width / 2f;
			}
			mask_area.set(2, 2, width - 2, height - 2);
			mask_path.reset();
			mask_path.addRoundRect(mask_area, r, r, Path.Direction.CCW);
			mask_path.addRect(0, 0, width, height, Path.Direction.CW);
			mask_path.close();
			line_width = height >> 4;
			line_paint.setStrokeWidth(line_width);
			line_top = 0;
			line_height = line_width << 2;
			move_speed = line_height / fps;
			if (move_speed < 1) {
				move_speed = 1;
			}
			if (is_set_speed_by_outside) {
				move_speed *= set_speed;
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (draw_cnt < 2) {
			draw_cnt++;
		}
		canvas.drawColor(back_color);
		draw_lines(canvas);
		canvas.drawPath(mask_path, mask_paint);
	}

	private void draw_lines(Canvas c) {
		float start_t = line_top - line_height;
		while (start_t < height) {
			c.drawLine(-line_width, start_t, width + line_width, start_t
					+ line_height, line_paint);
			start_t += line_width << 1;
		}
		if (line_top < -line_height) {
			line_top += line_height;
		}
	}

	private void set_from_xml(Context context, AttributeSet attrs,
			int defStyleAttr) {
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.UpMoveBar, defStyleAttr, 0);
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
				case R.styleable.UpMoveBar_UpMoveBar_background_color:
					int c1 = a.getColor(index, 0);
					set_background_color(c1);
					break;
				case R.styleable.UpMoveBar_UpMoveBar_bar_color:
					int c2 = a.getColor(index, 0);
					set_line_color(c2);
					break;
				case R.styleable.UpMoveBar_UpMoveBar_mask_color:
					int c3 = a.getColor(index, 0);
					set_mask_color(c3);
					break;
				case R.styleable.UpMoveBar_move_speed:
					set_move_speed(a.getFloat(index, 1));
					break;
				case R.styleable.UpMoveBar_round_r:
					set_round_r(a.getDimension(index, 20));
					break;
				}
			}
			a.recycle();
		}
	}

	private void init_view(Context context) {
		fps = 50;
		width = 0;
		height = 0;
		cycle_r = 0;
		draw_cnt = 0;
		line_top = 0;
		move_speed = 0;
		line_width = 20;
		line_height = line_width << 2;

		mask_path = new Path();
		mask_area = new RectF();

		line_paint = new Paint();
		line_paint.setAntiAlias(true);
		line_paint.setStyle(Style.STROKE);
		line_paint.setStrokeWidth(line_width);

		mask_paint = new Paint();
		mask_paint.setAntiAlias(true);
		mask_paint.setStyle(Style.FILL);

		back_color = Color.rgb(202, 202, 202);
		set_line_color(Color.rgb(181, 181, 181));
		mask_paint.setColor(Color.WHITE);

		is_set_r_by_outside = false;
		is_set_speed_by_outside = false;

		setFPS(fps, 500);
	}

	private void setFPS(int fps, int delay) {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		if (fps > 0) {
			fps = fps > 100 ? 10 : (int) (1000 / fps);
			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (1 >= draw_cnt) {
						line_top -= move_speed;
						postInvalidate();
					}
					if (draw_cnt > 0) {
						draw_cnt--;
					}
				}
			}, delay, fps);
		}
	}
}
