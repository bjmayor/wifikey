package com.hiwifi.app.receiver;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.hiwifi.model.wifi.WifiAdmin;

public class HiwifiBroadcastReceiver extends BroadcastReceiver {
	private final String TAG = "HiwifiBroadcastReceiver";
	private static ArrayList<WifiEventHandler> ehList = new ArrayList<WifiEventHandler>();

	public static boolean addListener(WifiEventHandler handler) {
		if (ehList.contains(handler)) {
			return true;
		}
		return ehList.add(handler);
	}

	public static Boolean removeListener(WifiEventHandler handler) {
		return ehList.remove(handler);
	}

	public static void removeAllListener() {
		ehList.clear();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// System.out.println("action==="+intent.getAction());
		if (intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)) {
			for (int i = 0; i < ehList.size(); i++) {
				((WifiEventHandler) ehList.get(i)).rssiChanged(intent);
			}
		} else if (intent.getAction().equals(
				WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
			for (int i = 0; i < ehList.size(); i++) {
				((WifiEventHandler) ehList.get(i)).supStatusChanged(intent);
			}
		} else if (intent.getAction().equals(
				WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
			for (int i = 0; i < ehList.size(); i++) {
				((WifiEventHandler) ehList.get(i)).supConnectChanged(intent);
			}
		} else if (intent.getAction().equals(
				WifiManager.NETWORK_IDS_CHANGED_ACTION)) {
			for (int i = 0; i < ehList.size(); i++) {
				((WifiEventHandler) ehList.get(i)).networkIdChanged(intent);
			}
		} else if (intent.getAction().endsWith(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
			WifiAdmin.sharedInstance().onWifiListChanged();
			for (int i = 0; i < ehList.size(); i++) {
				((WifiEventHandler) ehList.get(i)).scanResultsAvaiable(intent);
			}

		} else if (intent.getAction().endsWith(
				WifiManager.WIFI_STATE_CHANGED_ACTION)) {
			WifiAdmin.sharedInstance().onWifiStateChanged();
			for (int j = 0; j < ehList.size(); j++) {
				((WifiEventHandler) ehList.get(j))
						.wifiStatusNotification(intent);
			}

		} else if (intent.getAction().endsWith(
				WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			WifiAdmin.sharedInstance().onWificonnectChanged();
			for (int m = 0; m < ehList.size(); m++) {
				((WifiEventHandler) ehList.get(m))
						.handleWifiConnectChange(intent);
			}
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
	public static abstract class WifiEventHandler {
		private final static int msg_result_avaiable = 0;
		private final static int msg_wificonnect_changed = 1;
		private final static int msg_wifistatus_changed = 2;
		private final static int msg_supstatus_changed = 3;
		private final static int msg_supconnect_changed = 4;
		private final static int msg_networkid_changed = 5;
		private final static int msg_rssi_changed = 6;

		@SuppressLint("InlinedApi")
		private Handler mHandler = new Handler(Looper.getMainLooper()) {
			public void handleMessage(android.os.Message msg) {
				Bundle data = msg.getData();
				switch (msg.what) {
				case msg_result_avaiable:
					onScanResultAvaiable();
					break;
				case msg_wifistatus_changed:
					onWifiStatusChange(
							data.getInt(WifiManager.EXTRA_WIFI_STATE),
							data.getInt(WifiManager.EXTRA_PREVIOUS_WIFI_STATE));
					break;
				case msg_wificonnect_changed:

					onWifiConnectChange(
							(NetworkInfo) data
									.getParcelable(WifiManager.EXTRA_NETWORK_INFO),
							data.getString(WifiManager.EXTRA_BSSID),
							(WifiInfo) data
									.getParcelable(WifiManager.EXTRA_WIFI_INFO));
					break;
				case msg_supconnect_changed:
					onSupConnectChange(data
							.getBoolean(WifiManager.EXTRA_SUPPLICANT_CONNECTED));
					break;
				case msg_supstatus_changed:
					onSupStatusChange(
							(SupplicantState) data
									.getParcelable(WifiManager.EXTRA_NEW_STATE),
							data.getInt(WifiManager.EXTRA_SUPPLICANT_ERROR));
					break;
				case msg_rssi_changed:
					onRssiChange(data.getInt(WifiManager.EXTRA_NEW_RSSI));
					break;
				case msg_networkid_changed:
					onNetworkIdChange();
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
		public final void handleWifiConnectChange(Intent intent) {
			Message msg = mHandler.obtainMessage();
			Bundle data = new Bundle();
			data.putParcelable(WifiManager.EXTRA_NETWORK_INFO,
					intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO));
			data.putString(WifiManager.EXTRA_BSSID,
					intent.getStringExtra(WifiManager.EXTRA_BSSID));
			data.putParcelable(WifiManager.EXTRA_WIFI_INFO,
					intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO));
			msg.setData(data);
			msg.what = msg_wificonnect_changed;
			mHandler.sendMessage(msg);
		}

		// can get list now
		public final void scanResultsAvaiable(Intent intent) {
			mHandler.sendEmptyMessage(msg_result_avaiable);
		}

		/*
		 * Broadcast intent action indicating that Wi-Fi has been enabled,
		 * disabled, enabling, disabling, or unknown. One extra provides this
		 * state as an int. Another extra provides the previous state, if
		 * available.
		 */
		public final void wifiStatusNotification(Intent intent) {
			Message msg = mHandler.obtainMessage();
			msg.what = msg_wifistatus_changed;
			Bundle data = new Bundle();
			data.putInt(WifiManager.EXTRA_WIFI_STATE,
					intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0));
			data.putInt(WifiManager.EXTRA_PREVIOUS_WIFI_STATE, intent
					.getIntExtra(WifiManager.EXTRA_PREVIOUS_WIFI_STATE, 0));
			msg.setData(data);
			mHandler.sendMessage(msg);
		}

		/*
		 * Broadcast intent action indicating that the state of establishing a
		 * connection to an access point has changed.One extra provides the new
		 * SupplicantState. Note that the supplicant state is Wi-Fi specific,
		 * and is not generally the most useful thing to look at if you are just
		 * interested in the overall state of connectivity.
		 * 
		 * See Also EXTRA_NEW_STATE EXTRA_SUPPLICANT_ERROR
		 */
		public final void supStatusChanged(Intent intent) {
			Message msg = mHandler.obtainMessage();
			msg.what = msg_supstatus_changed;
			Bundle data = new Bundle();
			data.putParcelable(WifiManager.EXTRA_NEW_STATE,
					intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE));
			data.putInt(WifiManager.EXTRA_SUPPLICANT_ERROR,
					intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 0));
			msg.setData(data);
			mHandler.sendMessage(msg);
		}

		// singal changed
		public final void rssiChanged(Intent intent) {
			Message msg = mHandler.obtainMessage();
			msg.what = msg_rssi_changed;
			Bundle data = new Bundle();
			data.putInt(WifiManager.EXTRA_NEW_RSSI,
					intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, 0));
			msg.setData(data);
			mHandler.sendMessage(msg);
		}

		public final void networkIdChanged(Intent intent) {
			mHandler.sendEmptyMessage(msg_networkid_changed);
		}

		/*
		 * Broadcast intent action indicating that a connection to the
		 * supplicant has been established (and it is now possible to perform
		 * Wi-Fi operations) or the connection to the supplicant has been lost.
		 * One extra provides the connection state as a boolean, where true
		 * means CONNECTED.
		 */
		public final void supConnectChanged(Intent intent) {
			Message msg = mHandler.obtainMessage();
			Bundle data = new Bundle();
			data.putBoolean(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);
			msg.setData(data);
			msg.what = msg_supconnect_changed;
			mHandler.sendMessage(msg);
		}

		public abstract void onWifiStatusChange(int state, int preState);

		public abstract void onWifiConnectChange(NetworkInfo networkInfo,
				String BSSID, WifiInfo wifiInfo);

		public abstract void onSupStatusChange(SupplicantState newState,
				int error);

		public abstract void onSupConnectChange(Boolean isConnected);

		public abstract void onRssiChange(int newRssi);

		public abstract void onNetworkIdChange();

		public abstract void onScanResultAvaiable();

	}

}
