package com.hiwifi.app.views;

import com.hiwifi.hiwifi.Gl;
import com.hiwifi.hiwifi.R;
import com.umeng.analytics.MobclickAgent;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class RadarButton extends View {

	public boolean sound_on;

	private boolean sound_play;
	private boolean is_touch_without_move;
	private int width;
	private int height;
	private float touch_x;
	private float touch_y;
	private float min_move;
	private float img_width;
	private float img_height;
	private float img_left;
	private float img_top;
	private RectF img_rect;
	private Bitmap img_on;
	private Bitmap img_off;
	private Bitmap img_flash;
	private PaintFlagsDrawFilter canvas_filter;

	public void setPlay() {
		sound_play = true;
		postInvalidate();
	}

	public void setStop() {
		sound_play = false;
		postInvalidate();
	}

	public RadarButton(Context context) {
		super(context);
		init_sys();
	}

	public RadarButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init_sys();
	}

	public RadarButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init_sys();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int act = event.getAction();
		float x = event.getX();
		float y = event.getY();
		if (x > img_left && x < img_left + img_width) {
			if (act == MotionEvent.ACTION_DOWN) {
				is_touch_without_move = true;
				touch_x = x;
				touch_y = y;
			} else if (act == MotionEvent.ACTION_MOVE) {
				float disx = Math.abs(x - touch_x);
				float disy = Math.abs(y - touch_y);
				if (is_touch_without_move) {
					is_touch_without_move = (disx + disy < min_move);
				}
			} else if (act == MotionEvent.ACTION_UP) {
				if (is_touch_without_move) {
					sound_on = !sound_on;
					postInvalidate();
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		float w = getSize(widthMeasureSpec);
		min_move = width * 0.1f;
		img_width = width * 0.15f * Radar.zoom;
		img_height = img_width / 0.77f;
		img_left = (width - img_width) / 2;
		img_top = 0;
		width = (int) w;
		height = (int) img_height;
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			init_graphic();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (sound_on) {
			if (sound_play) {
				canvas.drawBitmap(img_flash, img_left, img_top, null);
			} else {
				canvas.drawBitmap(img_on, img_left, img_top, null);
			}
		} else {
			canvas.drawBitmap(img_off, img_left, img_top, null);
		}
		super.onDraw(canvas);
	}

	private int getSize(int measureSpec) {
		return MeasureSpec.getSize(measureSpec);
	}

	private void load_img(Bitmap output, int id) {
		try {
			Bitmap temp = BitmapFactory.decodeResource(getResources(), id);
			if (temp != null) {
				Canvas c = new Canvas(output);
				c.setDrawFilter(canvas_filter);
				c.drawBitmap(temp, null, img_rect, null);
				// temp.recycle();
				temp = null;
			}
		} catch (Exception e) {

		}
	}

	private void init_sys() {
		canvas_filter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
				| Paint.FILTER_BITMAP_FLAG);
	}

	private Bitmap creat_new_img(Bitmap output) {
		if (output != null) {
			output.recycle();
			output = null;
		}
		return Bitmap.createBitmap((int) img_width, (int) img_height,
				Bitmap.Config.ARGB_8888);
	}

	/**
	 * load images and adjust all images to the same size save to img_on,
	 * img_off and img_flash
	 */
	private void init_graphic() {
		img_on = creat_new_img(img_on);
		img_off = creat_new_img(img_off);
		img_flash = creat_new_img(img_flash);
		img_rect = new RectF(0, 0, img_width, img_height);
		load_img(img_on, R.drawable.radar_sound_on);
		load_img(img_off, R.drawable.radar_sound_off);
		load_img(img_flash, R.drawable.radar_sound_flash);
	}
}
