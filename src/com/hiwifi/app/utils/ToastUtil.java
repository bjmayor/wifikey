package com.hiwifi.app.utils;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hiwifi.hiwifi.R;
import com.hiwifi.model.log.LogUtil;

public class ToastUtil extends Toast {


	public static final int EMPTY = -99999;
	public static final int ONLYTEXT = 1;
	public static final int SUCCESS = 2;
	public static final int ERROR = 3;
	public static final int WARN = 4;
	

	public ToastUtil(Context context) {
		super(context);
	}


	public static Toast makeImageToast(Context context, int type,
			CharSequence textUp, CharSequence textDown, int duration) {
		Toast result = new Toast(context);
//		LayoutInflater inflate = (LayoutInflater) context
//				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LayoutInflater inflate = LayoutInflater.from(context);
		View v = inflate.inflate(R.layout.image_toast_one, null);
		ImageView dialog_iv = (ImageView) v.findViewById(R.id.dialog_iv);
		TextView dialog_tv_one = (TextView) v.findViewById(R.id.dialog_tv_1);
		TextView dialog_tv_two = (TextView) v.findViewById(R.id.dialog_tv_2);

		if (type == ONLYTEXT) {
			dialog_iv.setVisibility(View.GONE);
		} else if (type == SUCCESS) { 
			dialog_iv.setBackgroundResource(R.drawable.dialog_right);
		} else if (type == ERROR) {
			dialog_iv.setBackgroundResource(R.drawable.dialog_wrong);
		} else if (type == WARN) {
			dialog_iv.setBackgroundResource(R.drawable.dialog_warn);
		}
		if (!TextUtils.isEmpty(textUp)) {
			dialog_tv_one.setText(textUp);
		}
		LogUtil.d("Tag:", textUp.toString());
		if (!TextUtils.isEmpty(textDown)) {
			dialog_tv_two.setText(textDown);
			dialog_tv_two.setVisibility(View.VISIBLE);
		}
		result.setView(v);
		result.setGravity(Gravity.CENTER, 0, 0);
		result.setDuration(duration);
		return result;

	}

	private static Handler handler = new Handler(Looper.getMainLooper());

	private static Toast toast = null;

	private static Object synObj = new Object();

	public static void showMessage(final Context act, final String msg) {
		showMessage(act, msg, Toast.LENGTH_SHORT);
	}

	public static void showMessage(final Context act, final int msg) {
		showMessage(act, msg, Toast.LENGTH_SHORT);
	}

	public static void showMessage(final Context act, final String msg,
			final int len) {
		// new Thread(new Runnable() {
		// public void run() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				synchronized (synObj) {
					if (toast != null) {

						toast.setText(msg);
						toast.setDuration(len);
					} else {
						toast = Toast.makeText(act, msg, len);
					}
					if (!TextUtils.isEmpty(msg))
						toast.show();
				}
			}
		});
		// }
		// }).start();
	}

	public static void showMessage(final Context act, final int msg,
			final int len) {
		// new Thread(new Runnable() {
		// public void run() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				synchronized (synObj) {
					if (toast != null) {
						toast.setText(msg);
						toast.setDuration(len);
					} else {
						toast = Toast.makeText(act, msg, len);
					}
					toast.show();
				}
			}
		});
		// }
		// }).start();
	}

	public static long lastTime;

	public static boolean isToastShowing() {
		boolean isShow = false;
		if (lastTime == 0) {
			lastTime = System.currentTimeMillis();
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					lastTime = 0;
				}
			}, 1800);
			isShow = true;
		} else {
			isShow = false;
		}
		return isShow;
	}

}
