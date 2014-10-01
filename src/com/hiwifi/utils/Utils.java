package com.hiwifi.utils;

import java.io.File;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import com.hiwifi.app.utils.ToastUtil;
import com.hiwifi.hiwifi.Gl;

public class Utils {
	public enum Level {
		NORMAL(0), ONLYTEXT(1), SUCCESS(2), ERROR(3), WARN(4);
		int code;

		public int getCode() {
			return code;
		}

		private Level(int code) {
			this.code = code;
		}
	}

	public static void showToast(Context context, int code, String msg,
			int showTime) {
		if (context == null) {
			context = Gl.Ct();
		}
		if (code == -1 || code >= 9999) {
			if ("".equals(msg) || msg == null) {
				String message = Gl.getErrorMap().get(String.valueOf(code));
				if (!TextUtils.isEmpty(message)) {
					ToastUtil.showMessage(context, message);
				}
			} else {
				ToastUtil.showMessage(context, msg);
			}
		} else {
			String message = Gl.getErrorMap().get(String.valueOf(code));
			if (!TextUtils.isEmpty(message)) {
				ToastUtil.showMessage(context, message);
			}
		}
	}

	// 按级别划分
	public static void showToast(Context context, int code, String msg,
			int showTime, Utils.Level level) {
		String message = "";
		if (context == null) {
			context = Gl.Ct();
		}
		if (code == -1 || code >= 9999) {
			if ("".equals(msg) || msg == null) {
				message = Gl.getErrorMap().get(String.valueOf(code));
			} else {
				message = msg;
			}
		} else {
			message = Gl.getErrorMap().get(String.valueOf(code));
		}
		if (level instanceof Level) {
			if (!TextUtils.isEmpty(message)) {
				switch (level.getCode()) {
				case 0:
					ToastUtil.showMessage(context, message);
					break;
				case 1:
				case 2:
				case 3:
				case 4:
					ToastUtil.makeImageToast(context, level.getCode(), message,
							null, showTime).show();
					break;
				default:
					break;
				}
			}
		}
	}

	/**
	 * 读取sd卡存储信息
	 */
	public static void readSDCard() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs sf = new StatFs(sdcardDir.getPath());
			long blockSize = sf.getBlockSize();
			long blockCount = sf.getBlockCount();
			long availCount = sf.getAvailableBlocks();
			Log.d("", "block大小:" + blockSize + ",block数目:" + blockCount
					+ ",总大小:" + blockSize * blockCount / 1024 + "KB");
			Log.d("", "可用的block数目：:" + availCount + ",剩余空间:" + availCount
					* blockSize / 1024 + "KB");
		}
	}

	/**
	 * 读取内部存储信息
	 */
	public static void readSystem() {

		File root = Environment.getRootDirectory();

		StatFs sf = new StatFs(root.getPath());

		long blockSize = sf.getBlockSize();

		long blockCount = sf.getBlockCount();

		long availCount = sf.getAvailableBlocks();

		Log.d("", "block大小:" + blockSize + ",block数目:" + blockCount + ",总大小:"
				+ blockSize * blockCount / 1024 + "KB");

		Log.d("", "可用的block数目：:" + availCount + ",可用大小:" + availCount
				* blockSize / 1024 + "KB");
	}

	/**
	 * 获取手机mac地址<br/>
	 * 错误返回12个0
	 */
	public static String getMacAddress(Context context) {
		// 获取mac地址：
		String macAddress = "";
		try {
			WifiManager wifiMgr = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = (null == wifiMgr ? null : wifiMgr
					.getConnectionInfo());
			if (null != info) {
				if (!TextUtils.isEmpty(info.getMacAddress()))
					macAddress = info.getMacAddress().replace(":", "");
				else
					return macAddress;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return macAddress;
		}
		return macAddress;
	}
}
