package com.hiwifi.app.views;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.hiwifi.hiwifi.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
//import android.view.MotionEvent;
import android.view.View;

/**
 * 
 * @author Zi-ZhiHao.Kang
 * @version 1.1
 * @since 2014-06-16
 */
public class Radar extends View {

	public final static float zoom = 0.75f;

	private int cycle_top;
	private int cycle_left;
	private int range;
	private int center_x;
	private int center_y;
	private int width;
	private int height;
	private int value;
	private int label_id;
	private float img_top;
	private float text_x;
	private float text_y;
	private float text_size;
	private float small_text_x;
	private float small_cycle_r;
	private float small_cycle_x;
	private float small_cycle_y;
	private float tx;
	private float ty;
	private float angle;
	private float set_angle;
	private boolean loss_signal;
	private Path path;
	private Path shadow_path;
	private Paint back_paint;
	private Paint text_paint;
	private Paint shadow_paint;
	private Paint small_text_paint;
	private Bitmap bachground_image;
	private ArrayList<Bitmap> img_list;
	private ArrayList<Float> img_left_list;
	private RadarValueChangeActionListener listener;
	private Timer mTimer;

	/**
	 * constructor
	 * 
	 * @param context
	 *            the context of the view
	 */
	public Radar(Context context) {
		super(context);
		init_variables();
	}

	/**
	 * constructor
	 * 
	 * @param context
	 *            the context of the view
	 * @param attrs
	 *            set of attribute of view
	 */
	public Radar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init_variables();
	}

	/**
	 * 
	 * @param context
	 *            the context of the view
	 * @param attrs
	 *            set of attribute of view
	 * @param defStyleAttr
	 *            default style attribute of view
	 */
	public Radar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init_variables();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		tx = event.getX();
		ty = event.getY();
		if (listener != null) {
			listener.onTouchGetAngle(get_angle());
		}
		return false;
	}

	/**
	 * set the label shown in center of the Radar view
	 * 
	 * @param id
	 *            the id of the label
	 */
	public void setCenterImgId(int id) {
		if (id > 4) {
			id = 4;
		} else if (id < 0) {
			id = 0;
		}
		label_id = id;
	}

	public void setRadarValueChangeActionListener(
			RadarValueChangeActionListener listener) {
		this.listener = listener;
	}

	/**
	 * set the percent of wifi singals
	 * 
	 * @param value
	 *            the value to show the percent in the small cycle
	 */
	public void setValue(int value) {
		this.value = value;
	}

	/**
	 * set the position of the small cycle while loading
	 * 
	 * @param angle
	 *            the angle while loading
	 */
	public void set_init_angle(float angle) {
		this.angle = angle;
		set_angle = angle;
		loss_signal = true;
		value = 0;
		calculate_data();
	}

	/**
	 * set the position of the small cycle
	 * 
	 * @param angle
	 *            the location of the small cycle
	 * @param loss_singal
	 *            if there has signal
	 * @param value
	 *            the percent level of the signal
	 */
	public void set_angle(float angle, boolean loss_singal, int value) {
		set_angle = angle;
		this.loss_signal = loss_singal;
		this.value = value;
	}

	/**
	 * kill the timer to stop the animation
	 */
	public void stopCheck() {
		if (mTimer != null) {
			mTimer.cancel();
		}
	}

	/**
	 * start a timer to run the animation
	 */
	public void startCheck() {
		if (mTimer != null) {
			mTimer.cancel();
		}
		int fps = 60;
		fps = (int) (1000 / fps);
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				calculate_data();
			}
		}, 0, fps);
	}

	/**
	 * calculate the height by using the width and zoom rate
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		float w = MeasureSpec.getSize(widthMeasureSpec);
		setMeasuredDimension((int) w, (int) (w * zoom));
	}

	/**
	 * change the size of the background while the size is changed
	 */
	@SuppressLint("DrawAllocation")
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			System.out.println("layout");
			width = right - left;
			height = bottom - top;
			init_background();
			float h = height / 5f;
			img_top = (height - h) / 2;
			load_center_image(h);
			invalidate();
		}
	}

	/**
	 * HeHe
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (bachground_image != null) {
			canvas.drawBitmap(bachground_image, cycle_left, cycle_top,
					back_paint);
			Bitmap t = img_list.get(label_id);
			if (t != null) {
				canvas.drawBitmap(t, img_left_list.get(label_id), img_top, null);
			}
			if (small_cycle_x > 0) {
				canvas.drawCircle(small_cycle_x + 3, small_cycle_y + 3,
						small_cycle_r, shadow_paint);
				canvas.drawPath(shadow_path, shadow_paint);
				canvas.drawCircle(small_cycle_x, small_cycle_y, small_cycle_r,
						back_paint);
				canvas.drawPath(path, back_paint);
				canvas.drawText(String.valueOf(value), text_x, text_y,
						text_paint);
				canvas.drawText("%", small_text_x, text_y, small_text_paint);
			}
		}
	}

	/**
	 * i really do not want to explain the code of this method
	 */
	private void calculate_data() {
		if (angle != set_angle) {
			// calculate the error between set angle and current angle
			float err = (set_angle - angle) / 10;
			// the max speed to change current angle
			float max_speed = 0.1f;
			// check the speed
			if (err > max_speed) {
				err = max_speed;
			} else if (err < -max_speed) {
				err = -max_speed;
			}
			// if the small cycle is gray
			boolean show_gray = false;
			// change the current angle
			if (err > 0.001 || err < -0.001) {
				angle += err;
			} else {
				show_gray = loss_signal;
				angle = set_angle;
			}
			// check callback and callback give current angle
			if (listener != null) {
				listener.valueChanging(angle);
			}
			// calculate the location to capture color from background
			float capture_color_x = (float) (Math.cos(angle) * range);
			float capture_color_y = (float) (Math.sin(angle) * range);
			// calculate the location to draw small cycle around center
			small_cycle_x = capture_color_x * 0.725f;
			small_cycle_y = capture_color_y * 0.725f;
			// create a template color for the small cycle
			int now_color = Color.rgb(180, 180, 180);
			// create a template r to draw triangle
			float temp_r = range * 0.723f + small_cycle_r;
			// calculate the point of the triangle
			float triangle_x1 = (float) (Math.cos(angle - 0.023f) * temp_r);
			float triangle_y1 = (float) (Math.sin(angle - 0.023f) * temp_r);
			float triangle_x2 = (float) (Math.cos(angle + 0.023f) * temp_r);
			float triangle_y2 = (float) (Math.sin(angle + 0.023f) * temp_r);
			temp_r += small_cycle_r * 0.3f;
			float triangle_x3 = (float) (Math.cos(angle) * temp_r);
			float triangle_y3 = (float) (Math.sin(angle) * temp_r);
			// calculate the location to draw the small cycle in the view
			small_cycle_x += center_x;
			small_cycle_y += center_y;
			// calculate the location to draw the percent value in small cycle
			float temp_x = text_paint.measureText(String.valueOf(value)) / 2;
			text_x = small_cycle_x - temp_x;
			text_y = small_cycle_y + text_size;
			small_text_x = text_x + temp_x * 1.85f;
			// transport the location around center to location in view
			capture_color_x += center_x;
			capture_color_y += center_y;
			triangle_x1 += center_x;
			triangle_y1 += center_y;
			triangle_x2 += center_x;
			triangle_y2 += center_y;
			triangle_x3 += center_x;
			triangle_y3 += center_y;
			// add the triangle point to path
			path.reset();
			path.moveTo(triangle_x1, triangle_y1);
			path.lineTo(triangle_x2, triangle_y2);
			path.lineTo(triangle_x3, triangle_y3);
			path.close();
			// triangle shadow
			shadow_path.reset();
			shadow_path.moveTo(triangle_x1 + 3, triangle_y1 + 3);
			shadow_path.lineTo(triangle_x2 + 3, triangle_y2 + 3);
			shadow_path.lineTo(triangle_x3 + 3, triangle_y3 + 3);
			shadow_path.close();
			// get color
			if (!show_gray && bachground_image != null) {
				try {
					now_color = bachground_image.getPixel((int) capture_color_x
							- cycle_left, (int) capture_color_y - cycle_top);
				} catch (Exception e) {

				}
			}
			// set color for small cycle
			back_paint.setColor(now_color);
			// HeHe
			postInvalidate();
		}
	}

	/**
	 * 
	 * @param index
	 * @param height
	 * @param res
	 * @param id
	 * @param canvas_filter
	 */
	private void load_image(int index, float height, Resources res, int id,
			PaintFlagsDrawFilter canvas_filter) {
		if (img_list.size() > index) {
			img_list.get(index).recycle();
			img_list.remove(index);
			img_left_list.remove(index);
		}
		Bitmap read = BitmapFactory.decodeResource(res, id);
		float h = read.getHeight();
		float w = read.getWidth();
		w = height / h * w;
		float l = (width - w) / 2;
		RectF rect = new RectF(0, 0, w, height);
		Bitmap out = Bitmap.createBitmap((int) w, (int) height,
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(out);
		c.setDrawFilter(canvas_filter);
		c.drawBitmap(read, null, rect, null);
		img_list.add(index, out);
		img_left_list.add(index, l);
	}

	private void load_center_image(float img_height) {
		Resources res = getResources();
		PaintFlagsDrawFilter canvas_filter = new PaintFlagsDrawFilter(0,
				Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
		int id[] = { R.drawable.level0, R.drawable.level1, R.drawable.level2,
				R.drawable.level3, R.drawable.level4 };
		for (int i = 0; i < 5; i++) {
			load_image(i, img_height, res, id[i], canvas_filter);
		}
	}

	/**
	 * initialize the variables
	 */
	private void init_variables() {
		// initialize paint
		back_paint = new Paint();
		back_paint.setStyle(Style.FILL);
		back_paint.setAntiAlias(true);
		text_paint = new Paint();
		text_paint.setStyle(Style.FILL);
		text_paint.setAntiAlias(true);
		text_paint.setTextAlign(Align.LEFT);
		text_paint.setColor(Color.WHITE);
		small_text_paint = new Paint();
		small_text_paint.setStyle(Style.FILL);
		small_text_paint.setAntiAlias(true);
		small_text_paint.setTextAlign(Align.LEFT);
		small_text_paint.setColor(Color.WHITE);
		shadow_paint = new Paint();
		shadow_paint.setStyle(Style.FILL);
		shadow_paint.setColor(Color.rgb(150, 150, 150));
		shadow_paint.setAntiAlias(true);
		loss_signal = true;
		// initialize the path to draw triangle and the shadow
		path = new Path();
		shadow_path = new Path();
		// initialize variables
		bachground_image = null;
		img_list = new ArrayList<Bitmap>();
		img_left_list = new ArrayList<Float>();
		angle = (float) (-Math.PI / 2);
	}

	/**
	 * load the background image and zoom the image to fix the size of the view
	 */
	private void init_background() {
		// calculate the size of the background
		int size = (int) (width * zoom);
		// calculate the location of the background
		cycle_left = (width - size) >> 1;
		cycle_top = (height - size) >> 1;
		range = (int) (size * 0.42f);
		RectF out_range = new RectF(0.008f * size, 0.015f * size,
				0.993f * size, size);
		// resize and zoom the background to fix the view
		PaintFlagsDrawFilter canvas_filter;
		canvas_filter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
				| Paint.FILTER_BITMAP_FLAG);
		if (bachground_image != null) {
			bachground_image.recycle();
		}
		Bitmap temp = BitmapFactory.decodeResource(getResources(),
				R.drawable.radar_back);
		bachground_image = Bitmap.createBitmap(size, size,
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bachground_image);
		c.setDrawFilter(canvas_filter);
		c.drawBitmap(temp, null, out_range, null);
		temp.recycle();
		temp = null;
		c = null;
		out_range = null;
		canvas_filter = null;
		// calculate the center point
		center_x = width >> 1;
		center_y = height >> 1;
		// calculate variable base on the size of the view
		small_cycle_r = range * 0.11f;
		text_size = small_cycle_r * 0.85f;
		text_paint.setTextSize(text_size);
		small_text_paint.setTextSize(text_size * 0.56f);
		text_size *= 0.4f;
	}

	/**
	 * get the angle between center and touch point
	 * 
	 * @return the value of the angle
	 */
	private float get_angle() {
		float x = tx - center_x;
		float y = ty - center_y;
		if (x < 0) {
			return (float) (Math.atan(y / x) + Math.PI);
		} else {
			return (float) Math.atan(y / x);
		}
	}

	/**
	 * callback interface
	 * 
	 * @author Zi-ZhiHao.Kang
	 * @version 1.1
	 * @since 2014-06-16
	 */
	public interface RadarValueChangeActionListener {
		/**
		 * callback while touch the view
		 * 
		 * @param angle
		 *            the value of the angle between center and touch point
		 */
		public void onTouchGetAngle(float angle);

		/**
		 * callback while value changing
		 * 
		 * @param value
		 *            the current angle in annimation
		 */
		public void valueChanging(float value);
	}

}
