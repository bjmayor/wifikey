package com.hiwifi.app.receiver;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.hiwifi.model.wifi.state.CommerceState;

public class CmccStateBroadcastReceiver extends BroadcastReceiver {
	private final String TAG = "CmccStateBroadcastReceiver";
	private static ArrayList<CmccStateEventHandler> ehList = new ArrayList<CmccStateEventHandler>();

	public static boolean addListener(CmccStateEventHandler handler) {
		if (ehList.contains(handler)) {
			return true;
		}
		return ehList.add(handler);
	}

	public static Boolean removeListener(CmccStateEventHandler handler) {
		return ehList.remove(handler);
	}

	public static void removeAllListener() {
		ehList.clear();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		for (int m = 0; m < ehList.size(); m++) {
			((CmccStateEventHandler) ehList.get(m)).handleCmccState(intent);
		}
	}

	/**
	 * http://developer.android.com/reference/android/net/wifi/WifiManager.html#
	 * EXTRA_WIFI_STATE
	 * 
	 * @author ZHF
	 * 
	 */
	@SuppressLint("HandlerLeak")
	public static abstract class CmccStateEventHandler {
		private final static int msg_click = 0;
		private final static int msg_login = 1;
		private final static int msg_logout = 2;
		private final static int msg_vip_state_changed = 3;

		private int leftTime;
		private Boolean isVip;
		@SuppressLint("InlinedApi")
		private Handler mHandler = new Handler(Looper.getMainLooper()) {
			public void handleMessage(android.os.Message msg) {
				Bundle data = msg.getData();
				switch (msg.what) {
				case msg_click:
					onCmccStateClick(leftTime, isVip);
					break;
				case msg_login:
					onCmccStateLogin(leftTime, isVip);
					break;
				case msg_logout:
					onCmccStateLogout(leftTime, isVip);
					break;
				case msg_vip_state_changed:
					onCmccStateVipChanged(leftTime, isVip);
					break;
				default:
					break;
				}
			}
		};

		// connect to wifi changed only connected|disconnected but callback not
		// only one time
		/*
		 * EXTRA_NETWORK_INFO |提供连通性，没有SSID信息 EXTRA_BSSID | support when
		 * connected EXTRA_WIFI_INFO | support when connected
		 */
		@SuppressLint("InlinedApi")
		public final void handleCmccState(Intent intent) {
			Message msg = mHandler.obtainMessage();
			leftTime = intent.getIntExtra(CommerceState.EXTRA_LEFTTIME, 0);
			isVip = intent.getBooleanExtra(CommerceState.EXTRA_ISVIP, false);
			if (intent.getAction().equals(CommerceState.ACTION_CLICK)) {
				msg.what = msg_click;
			} else if (intent.getAction().equals(CommerceState.ACTION_LOGIN)) {
				msg.what = msg_login;
			} else if (intent.getAction().equals(CommerceState.ACTION_LOGOUT)) {
				msg.what = msg_logout;
			}
			mHandler.sendMessage(msg);
		}

		public abstract void onCmccStateLogin(int leftTime, Boolean isVip);

		public abstract void onCmccStateLogout(int leftTime, Boolean isVip);

		public abstract void onCmccStateClick(int leftTime, Boolean isVip);

		public abstract void onCmccStateVipChanged(int leftTime, Boolean isVip);

	}

}
