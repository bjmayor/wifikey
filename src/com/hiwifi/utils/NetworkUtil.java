package com.hiwifi.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

public class NetworkUtil {

	public static boolean checkConnection(Context mContext) {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null) {
			return networkInfo.isAvailable();
		}
		return false;
	}

	public static boolean isWifi(Context mContext) {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null && activeNetInfo.getTypeName().equalsIgnoreCase("WIFI")) {
			return true;
		}
		return false;
	}
	
	public static boolean isWifiConnectted(Context context){
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		State wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();

		if(wifi == State.CONNECTED){
			//WIFI已连接
			return true;
		}
		return false;
	}
	public static boolean isMobileConnected(Context context){
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		State mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();

		if(mobile == State.CONNECTED){
			//WIFI已连接
			return true;
		}
		return false;
	}
	
	
}
