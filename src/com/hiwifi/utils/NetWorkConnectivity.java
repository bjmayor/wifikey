package com.hiwifi.utils;

import org.apache.http.HttpHost;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

public class NetWorkConnectivity {
	public static enum NetworkType {
		NetworkTypeIsCTWAP, NetworkTypeIsCTNET, NetworkTypeIsCTWAP2G, NetworkTypeIsCTNET2G, // 中国电信
		NetworkTypeIsCMWAP, NetworkTypeIsCMNET, NetworkTypeIsCMWAP2G, NetworkTypeIsCMNET2G, // 中国移动
		NetworkTypeIsCUWAP, NetworkTypeIsCUNET, NetworkTypeIsCUWAP2G, NetworkTypeIsCUNET2G, // 中国联通

		NetworkTypeIsDisabled, NetworkTypeIsWifi, NetworkTypeIsUnKown,

	}

	private static final String TAG = "HWFConnectivity";

	private static final String CTWAP = "ctwap";
	private static final String CTNET = "ctnet";
	private static final String CMWAP = "cmwap";
	private static final String CMNET = "cmnet";
	private static final String NET_3G = "3gnet";
	private static final String WAP_3G = "3gwap";
	private static final String UNIWAP = "uniwap";
	private static final String UNINET = "uninet";

	public static final int TYPE_NET_WORK_DISABLED = 0;// 网络不可用

	private static Uri PREFERRED_APN_URI = Uri
			.parse("content://telephony/carriers/preferapn");

	public static boolean is3G(NetworkType type) {
		return type == NetworkType.NetworkTypeIsCMNET
				|| type == NetworkType.NetworkTypeIsCMWAP
				|| type == NetworkType.NetworkTypeIsCTNET
				|| type == NetworkType.NetworkTypeIsCTWAP
				|| type == NetworkType.NetworkTypeIsCUNET
				|| type == NetworkType.NetworkTypeIsCUWAP;
	}

	public static boolean is2G(NetworkType type) {
		return type == NetworkType.NetworkTypeIsCMNET2G
				|| type == NetworkType.NetworkTypeIsCMWAP2G
				|| type == NetworkType.NetworkTypeIsCTNET2G
				|| type == NetworkType.NetworkTypeIsCTWAP2G
				|| type == NetworkType.NetworkTypeIsCUNET2G
				|| type == NetworkType.NetworkTypeIsCUWAP2G;
	}

	public static boolean isWifi(NetworkType type) {
		return type == NetworkType.NetworkTypeIsWifi;
	}

	public static boolean isWifi(Context mContext) {
		return isWifi(checkNetworkType(mContext));
	}

	/***
	 * 判断Network具体类型（联通移动wap，电信wap，其他net）
	 * 
	 * */
	public static NetworkType checkNetworkType(Context mContext) {
		try {
			final ConnectivityManager connectivityManager = (ConnectivityManager) mContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			final NetworkInfo mobNetInfoActivity = connectivityManager
					.getActiveNetworkInfo();
			if (mobNetInfoActivity == null || !mobNetInfoActivity.isAvailable()) {
				// 注意一：
				// NetworkInfo 为空或者不可以用的时候正常情况应该是当前没有可用网络，
				// 但是有些电信机器，仍可以正常联网，
				// 所以当成net网络处理依然尝试连接网络。
				// （然后在socket中捕捉异常，进行二次判断与用户提示）。
				return NetworkType.NetworkTypeIsDisabled;
			} else {
				// NetworkInfo不为null开始判断是网络类型
				int netType = mobNetInfoActivity.getType();
				if (netType == ConnectivityManager.TYPE_WIFI) {
					// wifi net处理
					return NetworkType.NetworkTypeIsWifi;
				} else if (netType == ConnectivityManager.TYPE_MOBILE) {
					// 注意二：
					// 判断是否电信wap:
					// 不要通过getExtraInfo获取接入点名称来判断类型，
					// 因为通过目前电信多种机型测试发现接入点名称大都为#777或者null，
					// 电信机器wap接入点中要比移动联通wap接入点多设置一个用户名和密码,
					// 所以可以通过这个进行判断！

					boolean is3G = isFastMobileNetwork(mContext);

					final Cursor c = mContext.getContentResolver().query(
							PREFERRED_APN_URI, null, null, null, null);
					if (c != null) {
						c.moveToFirst();
						final String user = c.getString(c
								.getColumnIndex("user"));
						if (!TextUtils.isEmpty(user)) {
							if (user.startsWith(CTWAP)) {
								return is3G ? NetworkType.NetworkTypeIsCTWAP
										: NetworkType.NetworkTypeIsCTWAP2G;
							} else if (user.startsWith(CTNET)) {
								return is3G ? NetworkType.NetworkTypeIsCTNET
										: NetworkType.NetworkTypeIsCTNET2G;
							}
						}
					}
					c.close();

					// 注意三：
					// 判断是移动联通wap:
					// 其实还有一种方法通过getString(c.getColumnIndex("proxy")获取代理ip
					// 来判断接入点，10.0.0.172就是移动联通wap，10.0.0.200就是电信wap，但在
					// 实际开发中并不是所有机器都能获取到接入点代理信息，例如魅族M9 （2.2）等...
					// 所以采用getExtraInfo获取接入点名字进行判断

					String netMode = mobNetInfoActivity.getExtraInfo();
					if (netMode != null) {
						// 通过apn名称判断是否是联通和移动wap
						netMode = netMode.toLowerCase();

						if (netMode.equals(CMWAP)) {
							return is3G ? NetworkType.NetworkTypeIsCMWAP
									: NetworkType.NetworkTypeIsCMWAP2G;
						} else if (netMode.equals(CMNET)) {
							return is3G ? NetworkType.NetworkTypeIsCMNET
									: NetworkType.NetworkTypeIsCMNET2G;
						} else if (netMode.equals(NET_3G)
								|| netMode.equals(UNINET)) {
							return is3G ? NetworkType.NetworkTypeIsCMNET
									: NetworkType.NetworkTypeIsCMNET2G;
						} else if (netMode.equals(WAP_3G)
								|| netMode.equals(UNIWAP)) {
							return is3G ? NetworkType.NetworkTypeIsCUWAP
									: NetworkType.NetworkTypeIsCUWAP2G;
						}
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			return NetworkType.NetworkTypeIsUnKown;
		}

		return NetworkType.NetworkTypeIsUnKown;

	}

	public static String GetAPN(Context inContext) {
		if (isNeedAPNProxy()) {
			return "cmwap";
		} else {
			return "cmnet";
		}

	}

	public static boolean isNeedAPNProxy() {
		HttpHost proxy = null;
		String proxyHost = android.net.Proxy.getDefaultHost();
		if (proxyHost != null) {
			proxy = new HttpHost(android.net.Proxy.getDefaultHost(),
					android.net.Proxy.getDefaultPort());
			if (proxy.toString().length() > 0) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public static HttpHost GetAPNProxy() {
		// 设置代理 wap
		HttpHost proxy = null;
		String proxyHost = android.net.Proxy.getDefaultHost();
		if (proxyHost != null) {
			proxy = new HttpHost(android.net.Proxy.getDefaultHost(),
					android.net.Proxy.getDefaultPort());
			return proxy;
		} else {
			proxy = new HttpHost("10.0.0.172", 80);
			return proxy;
		}

	}

	public static boolean isUsingWap(Context context) {
		String apnName = GetAPN(context);
		checkNetworkType(context);
		if (("cmwap".equals(apnName) == true
				|| "uniwap".equals(apnName) == true || "3gwap".equals(apnName) == true)
				&& isWifi(checkNetworkType(context)) == false) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean hasNetwork(Context mContext) {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null) {
			return networkInfo.isAvailable();
		}
		return false;
	}

	private static boolean isFastMobileNetwork(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		switch (telephonyManager.getNetworkType()) {
		case TelephonyManager.NETWORK_TYPE_1xRTT:// ~ 50-100 kbps
		case TelephonyManager.NETWORK_TYPE_CDMA:// ~ 14-64 kbps
		case TelephonyManager.NETWORK_TYPE_EDGE:// ~ 50-100 kbps
		case TelephonyManager.NETWORK_TYPE_GPRS:// ~ 100 kbps
		case TelephonyManager.NETWORK_TYPE_IDEN:// ~25 kbps
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			return false;
		case TelephonyManager.NETWORK_TYPE_EVDO_0:// ~ 400-1000 kbps
		case TelephonyManager.NETWORK_TYPE_EVDO_A:// ~ 600-1400 kbps
		case TelephonyManager.NETWORK_TYPE_HSDPA:// ~ 2-14 Mbps
		case TelephonyManager.NETWORK_TYPE_HSPA:// ~ 700-1700 kbps
		case TelephonyManager.NETWORK_TYPE_HSUPA:// ~ 1-23 Mbps
		case TelephonyManager.NETWORK_TYPE_UMTS: // ~ 400-7000 kbps
		case TelephonyManager.NETWORK_TYPE_EHRPD:// ~ 1-2 Mbps
		case TelephonyManager.NETWORK_TYPE_EVDO_B:// ~ 5 Mbps
		case TelephonyManager.NETWORK_TYPE_HSPAP:// ~ 10-20 Mbps
		case TelephonyManager.NETWORK_TYPE_LTE:
			return true; // ~ 10+ Mbps

		default:
			return false;

		}
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
