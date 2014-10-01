package com.hiwifi.activity.wifi;

import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.hiwifi.model.wifi.WifiAdmin;

public class WifiFragment extends Fragment {
	protected WifiLock wifiLock;
	protected WifiAdmin mWifiAdmin;

	public WifiFragment() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mWifiAdmin = WifiAdmin.sharedInstance();
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		wifiLock = mWifiAdmin.getWifiManager().createWifiLock(
				WifiManager.WIFI_MODE_SCAN_ONLY, "debug");
//		wifiLock.acquire();
		super.onResume();
	}
	
	@Override
	public void onStop() {
//		wifiLock.release();
		super.onStop();
	}

}
