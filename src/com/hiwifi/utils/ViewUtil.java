package com.hiwifi.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.inputmethod.InputMethodManager;

import com.hiwifi.hiwifi.Gl;

public class ViewUtil {
	/**
	 * 关闭输入法
	 */
	public static void closeInput(Activity mAct) {
		InputMethodManager inputMethodManager = (InputMethodManager) mAct
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(mAct.getCurrentFocus()
				.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	/**
	 * 判断输入法是否打开
	 * 
	 * @param mAct
	 * @return
	 */
	public static boolean isOpenInput(Activity mAct) {
		InputMethodManager imm = (InputMethodManager) mAct
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		boolean isOpen = imm.isActive();
		return isOpen;
	}

	/**
	 * dip转px
	 * 
	 * @param context
	 * @param dipValue
	 * @return
	 */
	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	/**
	 * px转dip
	 * 
	 * @param context
	 * @param pxValue
	 * @return
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 振动的方法
	 * 
	 * @param activity
	 */
	public static void vibrate(Context context) {

		Vibrator vibrator = (Vibrator) context
				.getSystemService(Context.VIBRATOR_SERVICE);
		long[] pattern = { 100, 400, 100, 400 }; // 停止 开启 停止 开启
		vibrator.vibrate(pattern, -1);
	}

	private static DisplayMetrics sDisplay;

	public static int getScreenWidth() {
		if (sDisplay == null) {
			sDisplay = Gl.Ct().getResources().getDisplayMetrics();
		}
		return sDisplay.widthPixels;
	}

	public static int getScreenHeight() {
		if (sDisplay == null) {
			sDisplay = Gl.Ct().getResources().getDisplayMetrics();
		}
		return sDisplay.heightPixels;
	}

	public static int avaiableScreenHeight() {
		int height = getScreenHeight();
		if (hasSmartBar()) {
			height -= 120;
		}
		return height;
	}
	/**
	 * 判断指定activity是否在任务栈的最前端 需要权限：android.permission.GET_TASKS
	 * 
	 * @param context
	 * @param activityName
	 * @return
	 */
	public static boolean isTopActivity(Context context, String activityName) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTasks = am.getRunningTasks(100);
		for (RunningTaskInfo task : runningTasks) {
			if (task.topActivity.getClassName().equals(activityName))
				return true;
		}
		return false;
	}

	public static String getCurrentDate() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
		return df.format(new Date());
		// System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
	}

	// 判断是否有SmartBar
	public static boolean hasSmartBar() {
		try {
			// 新型号可用反射调用Build.hasSmartBar()
			Method method = Class.forName("android.os.Build").getMethod(
					"hasSmartBar");
			return ((Boolean) method.invoke(null)).booleanValue();
		} catch (Exception e) {
		}
		// 反射不到Build.hasSmartBar()，则用Build.DEVICE判断
		if (Build.DEVICE.equals("mx2")) {
			return true;
		} else if (Build.DEVICE.equals("mx") || Build.DEVICE.equals("m9")) {
			return false;
		}
		return false;
	}

	/**
	 * Draw text in center by horizontal
	 * 
	 * @param canvas
	 * @param x
	 * @param y
	 * @param text
	 * @param p
	 */
	public static void drawTextInCenterByHorizontal(Canvas canvas, String text,
			Paint paint, int x, int y) {
		int length = (int) paint.measureText(text);
		canvas.drawText(text, x - length / 2, y, paint);
	}

	/**
	 * textview排版问题
	 * 
	 * @param input
	 * @return
	 */
	public static String toCharacterFormat(String input) {
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 12288) {
				c[i] = (char) 32;
				continue;
			}
			if (c[i] > 65280 && c[i] < 65375)
				c[i] = (char) (c[i] - 65248);
		}
		return new String(c);
	}

	public static String overTime(long time) {
		int oneDay = 24 * 60 * 60;
		int hour = 60 * 60;
		int minute = 60;
		String date = "";
		if (time > hour) {
			int h = (int) (time / hour);
			int min = (int) ((time % hour) / minute);
			int second = (int) ((time % hour) % minute);
			date = /* "剩余" + */h + "时" + min + "分" + second + "秒";
		} else if (time > minute) {
			int min = (int) (time / minute);
			int second = (int) (time % minute);
			if (second != 0) {
				date = /* "剩余" + */min + "分" + second + "秒";
			} else {
				date = /* "剩余" + */min + "分"/* + "0秒" */;
			}
		} else {
			date = /* "剩余" + */time + "秒";
		}
		return date;
	}

	public static String getDpi(Activity context) {
		String dpi = null;
		Display display = context.getWindowManager().getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		@SuppressWarnings("rawtypes")
		Class c;
		try {
			c = Class.forName("android.view.Display");
			@SuppressWarnings("unchecked")
			Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
			method.invoke(display, dm);
			dpi = dm.widthPixels + "*" + dm.heightPixels;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dpi;
	}

	/**
	 * DecimalFormat转换最简便
	 */
	public static String m2(double f) {
		DecimalFormat df = new DecimalFormat("0.00");
		return df.format(f);
	}

	public static double getPhysicSize(Activity context) {
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		// System.out.println("宽：" + dm.widthPixels + "高：" + dm.heightPixels
		// + "密度：" + dm.density + "xdpi:" + dm.xdpi + "ydpi" + dm.ydpi
		// + "密度Dpi" + dm.densityDpi + "noncompatDensity:");
		double diagonalPixels = Math.sqrt(Math.pow(dm.widthPixels, 2)
				+ Math.pow(dm.heightPixels, 2));

		try {
			Class clazz = Class.forName("android.util.DisplayMetrics");
			Field[] declaredFields = clazz.getDeclaredFields();
			for (int i = 0; i < declaredFields.length; i++) {
				declaredFields[i].setAccessible(true);
				// System.out.println("name==" + declaredFields[i].getName()
				// + "value==" + declaredFields[i].getType());
				// System.out.println("-------"
				// + declaredFields[i].get(new Object()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// System.out.println("diagonalPixels==" + diagonalPixels);
		return diagonalPixels / (160 * dm.density);
	}
}
