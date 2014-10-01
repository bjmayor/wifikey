package com.hiwifi.app.views;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

import com.hiwifi.hiwifi.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class LogoPageV2 extends View {// SurfaceView implements
										// SurfaceHolder.Callback {

	// setting from outside
	private float start_r;
	private float speed;
	private float interval;
	private float x;
	private float y;
	private int cycle_cnt;
	private int delay;
	private int bar_color;
	private int center_color;
	private int background_color;

	// local fields
	private boolean is_stop;
	private boolean is_started = false;
	private float r_x;
	private float r_y;
	private float[] r_array;
	private float angle_speed;
	private float max_distance;
	private float max_angle_speed;
	private float bar_r;
	private float angle;
	private float angle_a;
	private float inner_cycle_r;
	private int width;
	private int height;
	private int timer;
	private int load_cnt;
	private int bar_alpha;
	private int fps = 60;
	private OnFinishListener listener;
	private LinkedList<Float> r_list;
	private LinkedList<Float> tail;
	private Paint bar_paint;
	private Paint tail_paint;
	private Paint cycle_paint;
	private Paint center_paint;
	private Timer mTimer;
	private RectF tail_cycle;

	public void setOnFinishListener(OnFinishListener listener) {
		this.listener = listener;
	}

	public void start() {
		is_started = true;
		setFPS(fps, 500);
	}

	public void stop() {
		is_stop = true;
		if (!is_started) {
			logoFinished();
		}
	}

	public void set_cycle_count(int cnt) {
		cycle_cnt = cnt;
		calculate_max_r();
	}

	public void set_r_interval(float interval) {
		this.interval = interval;
		calculate_max_r();
	}

	public void set_error_x_from_center(float x) {
		this.x = x;
		calculate_max_r();
	}

	public void set_error_y_from_center(float y) {
		this.y = y;
		calculate_max_r();
	}

	public void set_start_r(float start_r) {
		this.start_r = start_r;
		bar_r = start_r / 20f;
	}

	public void set_speed(float speed) {
		this.speed = speed;
	}

	public void set_delay(int delay) {
		this.delay = (int) (delay * fps / 1000f);
	}

	public void set_background_color(int color) {
		background_color = color;
	}

	public void set_center_color(int color) {
		center_color = color;
		center_paint.setColor(center_color);
		cycle_paint.setColor(center_color);
	}

	public void set_bar_color(int color) {
		bar_color = color;
		bar_paint.setColor(bar_color);
		tail_paint.setColor(bar_color);
	}

	public LogoPageV2(Context context) {
		super(context);
		initVeiw(context, null, 0);
	}

	public LogoPageV2(Context context, AttributeSet attrs) {
		super(context, attrs);
		initVeiw(context, attrs, 0);
	}

	public LogoPageV2(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initVeiw(context, attrs, defStyleAttr);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			width = right - left;
			height = bottom - top;
			if (start_r < 0) {
				start_r = width / 3.0f;
			}
			if (bar_r < 0) {
				bar_r = start_r / 20f;
				tail_paint.setStrokeWidth(bar_r * 1.2f);
			}
			if (interval < 0) {
				interval = width / 10;
			}
			if (speed < 0) {
				speed = width / 100;
			}
			inner_cycle_r = start_r - bar_r * 0.6f;
			calculate_max_r();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(background_color);
		if (r_array != null) {
			for (float i : r_array) {
				draw_cycle(canvas, i);
			}
		}
		canvas.drawCircle(r_x, r_y, start_r, center_paint);
		draw_small_cycle(canvas, angle, bar_r, inner_cycle_r);
		draw_tail(canvas);
	}

	private void draw_tail(Canvas c) {
		if (tail != null) {
			tail.addFirst(angle - 90);
			int size = tail.size();
			if (size > 60) {
				tail.removeLast();
			}
			if (is_stop) {
				for (int i = 0; i < 3 && tail.size() > 0; i++) {
					tail.removeLast();
				}
			}
			if (size > 1) {
				float a_old = 0;
				int alpha = bar_alpha;
				int alpha_dec = alpha / size;
				if (alpha_dec < 1) {
					alpha_dec = 1;
				}
				boolean is_first_time = true;
				for (float i : tail) {
					if (!is_first_time) {
						float draw_angle = a_old - i;
						if (draw_angle < 0) {
							draw_angle += 360;
						}
						tail_paint.setAlpha(alpha);
						c.drawArc(tail_cycle, i, draw_angle, false, tail_paint);
						if (alpha > 0) {
							alpha -= alpha_dec;
						}
					} else {
						is_first_time = false;
					}
					a_old = i;
				}
			}
		}
	}

	private void draw_cycle(Canvas c, float r) {
		if (r > start_r) {
			int alpha = 255 - (int) (r / max_distance * 255f);
			if (alpha < 0) {
				alpha = 0;
			}
			cycle_paint.setAlpha(alpha);
			c.drawCircle(r_x, r_y, r, cycle_paint);
		}
	}

	private void draw_small_cycle(Canvas canvas, float angle, float size,
			float start_r) {
		float x = r_x + (float) (Math.sin(Math.PI * angle / 180) * start_r);
		float y = r_y - (float) (Math.cos(Math.PI * angle / 180) * start_r);
		bar_paint.setAlpha(bar_alpha);
		canvas.drawCircle(x, y, size, bar_paint);

	}

	private void initVeiw(Context context, AttributeSet attrs, int defStyleAttr) {
		// local fields
		is_stop = false;
		r_list = new LinkedList<Float>();
		tail = new LinkedList<Float>();
		angle = 0;
		width = 0;
		height = 0;
		load_cnt = 0;
		bar_alpha = 0;
		max_distance = 0;
		max_angle_speed = 4;
		tail_cycle = new RectF();
		bar_paint = new Paint();
		tail_paint = new Paint();
		cycle_paint = new Paint();
		center_paint = new Paint();
		bar_paint.setAntiAlias(true);
		tail_paint.setAntiAlias(true);
		cycle_paint.setAntiAlias(true);
		center_paint.setAntiAlias(true);
		bar_paint.setStyle(Style.FILL);
		tail_paint.setStyle(Style.STROKE);
		cycle_paint.setStyle(Style.FILL);
		center_paint.setStyle(Style.FILL);
		angle_speed = 0;
		// setting from outside
		x = 0;
		y = 0;
		bar_r = -1;
		speed = -1;
		start_r = -1;
		interval = -1;
		cycle_cnt = 4;
		set_delay(700);
		set_background_color(Color.rgb(81, 197, 212));
		set_bar_color(Color.WHITE);
		set_center_color(Color.rgb(130, 212, 224));
		// get settings
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.LogoPageV2, defStyleAttr, 0);
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
				case R.styleable.LogoPageV2_background_color:
					set_background_color(a.getColor(index,
							Color.rgb(81, 197, 212)));
					break;
				case R.styleable.LogoPageV2_bar_color:
					set_bar_color(a.getColor(index, Color.WHITE));
					break;
				case R.styleable.LogoPageV2_center_color:
					set_center_color(a
							.getColor(index, Color.rgb(130, 212, 224)));
					break;
				case R.styleable.LogoPageV2_delay_time_ms:
					set_delay(a.getInt(index, 500));
					break;
				case R.styleable.LogoPageV2_r_interval:
					interval = a.getDimension(index, 100f);
					break;
				case R.styleable.LogoPageV2_r_speed:
					speed = a.getDimension(index, 10f);
					break;
				case R.styleable.LogoPageV2_x_error_from_center:
					x = a.getDimension(index, 0f);
					break;
				case R.styleable.LogoPageV2_y_error_from_center:
					y = a.getDimension(index, 0f);
					break;
				case R.styleable.LogoPageV2_start_r:
					start_r = a.getDimension(index, 400f);
					break;
				case R.styleable.LogoPageV2_cycle_count:
					cycle_cnt = a.getInt(index, 4);
					break;
				}
			}
		}
		a.recycle();
		// setFPS(fps, 500);
	}

	private void calculate_max_r() {
		if (width > start_r && height > start_r) {
			r_x = (width >> 1) + x;
			r_y = (height >> 1) + y;
			// get the max distance from the side of the screen
			if (x >= 0) {
				if (max_distance < r_x) {
					max_distance = r_x;
				}
			} else {
				float temp = (width - r_x);
				if (max_distance < temp) {
					max_distance = temp;
				}
			}
			if (y > 0) {
				if (max_distance < r_y) {
					max_distance = r_y;
				}
			} else {
				float temp = (height - r_y);
				if (max_distance < temp) {
					max_distance = temp;
				}
			}
			tail_cycle.set(r_x - inner_cycle_r, r_y - inner_cycle_r, r_x
					+ inner_cycle_r, r_y + inner_cycle_r);
			max_distance += interval;
		} else {
			max_distance = 1;
		}
	}

	private void addnew() {
		if (is_stop) {
			return;
		} else if (cycle_cnt < 1 || load_cnt < cycle_cnt) {
			r_list.addFirst(start_r);
			load_cnt++;
		} else if (timer++ > delay) {
			timer = 0;
			load_cnt = 0;
		}
	}

	public void setFPS(int fps, int delay) {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		is_stop = false;
		timer = 0;
		if (fps > 0) {
			fps = fps > 100 ? 10 : (int) (1000 / fps);
			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					// change each r in r_list
					if (is_stop) {
						if (bar_alpha > 0) {
							bar_alpha -= 5;
						} else {
							bar_alpha = 0;
						}
					} else {
						if (bar_alpha < 255) {
							bar_alpha += 5;
						} else {
							bar_alpha = 255;
						}
					}
					if (r_list != null) {
						int size = r_list.size();
						if (size == 0) {
							addnew();
							size++;
							if (is_stop && bar_alpha == 0) {
								cancel();
								angle = 0;
								timer = 0;
								load_cnt = 0;
								logoFinished();
								mTimer = null;
							}
						} else {
							if (r_list.getLast() > max_distance) {
								r_list.removeLast();
								size--;
							}
							if (size > 0) {
								if (r_list.getFirst() - interval > start_r) {
									addnew();
									size++;
								}
								r_array = new float[size];
								int ind = 0;
								ListIterator<Float> it = r_list.listIterator();
								while (it.hasNext()) {
									float t = it.next();
									t += speed * (t + 20) / max_distance;
									it.set(t);
									r_array[ind++] = t;
								}
							} else {
								r_array = new float[] {};
							}
						}

					}
					// change the angle
					angle += angle_speed;
					if (angle > 360) {
						angle = 0;
					}
					angle_speed += angle_a;
					if (cycle_cnt < 1) {
						if (angle_speed >= max_angle_speed) {
							angle_a = -0.05f;
						} else if (angle_speed <= 0.5f) {
							angle_a = 0.05f;
							max_angle_speed = 2 + (float) (Math.random() * 3);
						}
					} else {
						if (load_cnt >= cycle_cnt) {
							angle_a = -0.05f;
						} else if (load_cnt < cycle_cnt) {
							angle_a = 0.05f;
						}
						if (angle_speed < 0.5f) {
							angle_speed = 0.5f;
							angle_a = 0;
						}
						if (max_angle_speed < angle_speed) {
							angle_a = -0.1f;
						}
					}
					postInvalidate();
				}
			}, delay, fps);
		}
	}
	
	private void  logoFinished()
	{
		if (this.listener!=null) {
			this.listener.logo_finish();
			this.is_started = false;
		}
	}

	public interface OnFinishListener {
		public void logo_finish();
	}

}
