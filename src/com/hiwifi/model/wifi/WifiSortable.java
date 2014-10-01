package com.hiwifi.model.wifi;

import java.util.Comparator;

import u.aly.br;
import android.net.wifi.WifiManager;

import com.hiwifi.model.log.HWFLog;
import com.hiwifi.model.wifi.AccessPoint.WifiConnectState;

public class WifiSortable {
	public static final String TAG = "WifiSortable";

	public static Comparator<AccessPoint> defaultSortable() {
		return sortByUseRecommend();
	}

	public static Comparator<AccessPoint> sortBySignal() {
		return new Comparator<AccessPoint>() {

			@Override
			public int compare(AccessPoint lhs, AccessPoint rhs) {
				HWFLog.i(TAG, "SSID:" + lhs.getScanResult().SSID + " level:"
						+ lhs.getScanResult().level);
				HWFLog.i(TAG, "SSID:" + rhs.getScanResult().SSID + " level:"
						+ rhs.getScanResult().level);
				return rhs.getScanResult().level - lhs.getScanResult().level;
			}
		};
	}

	public static Comparator<AccessPoint> sortByPassword() {
		return new Comparator<AccessPoint>() {

			@Override
			public int compare(AccessPoint lhs, AccessPoint rhs) {
				int lNeedPassword = lhs.needPassword() ? 1 : 0;
				int rNeedPassword = rhs.needPassword() ? 1 : 0;
				return lNeedPassword - rNeedPassword;
			}
		};
	}

	public static Comparator<AccessPoint> sortBySSID() {
		return new Comparator<AccessPoint>() {

			@Override
			public int compare(AccessPoint lhs, AccessPoint rhs) {
				return lhs.getScanResult().SSID
						.compareTo(rhs.getScanResult().SSID);
			}
		};
	}

	public static Comparator<AccessPoint> sortByUseTimes() {
		return new Comparator<AccessPoint>() {

			@Override
			public int compare(AccessPoint lhs, AccessPoint rhs) {
				HWFLog.e(TAG, "usertime--" + lhs.getDataModel().useTimes + "--"
						+ rhs.getDataModel().useTimes);
				return rhs.getDataModel().useTimes
						- lhs.getDataModel().useTimes;
			}
		};
	}

	public static Comparator<AccessPoint> sortByNetTime() {
		return new Comparator<AccessPoint>() {

			@Override
			public int compare(AccessPoint lhs, AccessPoint rhs) {
				return (int) (lhs.getNetTime() - rhs.getNetTime());
			}
		};
	}

	public static Comparator<AccessPoint> sortByUseRecommend() {
		return new Comparator<AccessPoint>() {

			@Override
			public int compare(AccessPoint lhs, AccessPoint rhs) {
				return scoreOfAccessPoint(rhs) - scoreOfAccessPoint(lhs);
			}

			public int scoreOfAccessPoint(AccessPoint accessPoint) {
				int useTimes = 0;
				int signal = 0;
				int open = 0;
				if (accessPoint.getDataModel().useTimes >= 10) {
					useTimes = 100;
				} else if (accessPoint.getDataModel().useTimes > 0) {
					useTimes = 60;
				}
				signal = 20 * WifiManager.calculateSignalLevel(
						accessPoint.getScanResult().level, 5);
				if (!accessPoint.needPassword()) {
					open = 40;
				} else {
					if (accessPoint.isConfiged()
							&& accessPoint.isConfigedPasswordCorrect()) {
						open = 100;
					} else if (accessPoint.hasRemotePassword()) {
						open = 60;
					}
				}
				// HWFLog.e(TAG, "SSID:" + accessPoint.getPrintableSsid()
				// + "total:" + (useTimes + signal + open) + "useTimes:"
				// + useTimes + "signal:" + signal + "open:" + open);
				return useTimes + signal + open;
			}
		};
	}

	public static Comparator<AccessPoint> sortByWifiType() {
		return new Comparator<AccessPoint>() {

			@Override
			public int compare(AccessPoint lhs, AccessPoint rhs) {
				return scoreOfAccessPoint(rhs) - scoreOfAccessPoint(lhs);
			}

			private int scoreOfAccessPoint(AccessPoint point) {
				int score = 0;
				WifiConnectState connectState = point.getConnectState();
				switch (connectState) {
				case connectState_local_restore:// 本地存储已存储密码
					score = 1000+point.getSignalPersent();
					break;
				case connectState_unlock:// 可解锁
					score = 900+point.getSignalPersent();
					break;
				case connectState_canconnect:// 无密码可以直接连接
					score = 800+point.getSignalPersent();
					break;
				case connectState_portal:// 无密码连接后需要使用网页登入的
					score = 700+point.getSignalPersent();
					break;
				case connectState_needUserCount:// 需要用户名密码
					score = 600+point.getSignalPersent();
					break;
				case connectState_needpassword:// 需要密码
					score = 500+point.getSignalPersent();
					break;
				case connectState_chinaUnicom:// 中国联通，
					score = 400+point.getSignalPersent();
					break;
				case connectState_chinaNet:// 电信热点WIFI
					score = 300+point.getSignalPersent();
					break;
				case connectState_chinaCmcc:// 中国移动
				case connectState_chinaCmcc_edu:
				case connectState_chinaCmcc_auto:
					score = 200+point.getSignalPersent();
					break;
				case connectState_unknown:// 未知
					score = 100+point.getSignalPersent();
					break;
				default:
					score = point.getSignalPersent();
					break;
				}
				return score;
			}

		};
	}
}
