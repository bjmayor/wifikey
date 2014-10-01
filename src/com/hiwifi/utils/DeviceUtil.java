package com.hiwifi.utils;

import com.hiwifi.hiwifi.Gl;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

public class DeviceUtil {
	public static boolean isXiaomi() {
		try {
			return android.os.Build.BRAND.startsWith("Xiaomi");
		} catch (Exception e) {
		}
		return false;
	}

	public static boolean isMeizu() {
		try {
			return android.os.Build.BRAND.startsWith("Meizu");
		} catch (Exception e) {
		}
		return false;
	}
	
	public static boolean isSamSung() {
		try {
			return android.os.Build.BRAND.startsWith("samsung");
		} catch (Exception e) {
		}
		return false;
	}

	/**
	 * 1.手机才有，其它如pad等返回null 2.需要权限android.permission.READ_PHONE_STATE
	 * 3.有bug.在少数的一些手机设备上，该实现有漏洞，会返回垃圾，如:zeros或者asterisks的产品
	 */
	public static String getImei() {
		return ((TelephonyManager) Gl.Ct().getSystemService(
				Context.TELEPHONY_SERVICE)).getDeviceId();
	}

	/**
	 * ANDROID_ID是设备第一次启动时产生和存储的64bit的一个数，当设备被wipe后该数重置 ANDROID_ID似乎是获取Device
	 * ID的一个好选择，但它也有缺陷：
	 * 
	 * 它在Android <=2.1 or Android >=2.3的版本是可靠、稳定的，但在2.2的版本并不是100%可靠的
	 * 在主流厂商生产的设备上，有一个很经常的bug，就是每个设备都会产生相同的ANDROID_ID：9774d56d682e549c
	 */
	public static String getAndroidId() {
		return Secure
				.getString(Gl.Ct().getContentResolver(), Secure.ANDROID_ID);
	}
	
	/**
	 * 1.可能为null，需要有wifi。
	 * 2.这不是一个真实的地址。而且这个地址能轻易地被伪造。
	 * 3.需求权限 android.permission.ACCESS_WIFI_STATE
	 * @return
	 */
	public static String getMacAddress()
	{
		WifiManager wm = (WifiManager)Gl.Ct().getSystemService(Context.WIFI_SERVICE);
		String m_szWLANMAC = "";
		try {
			m_szWLANMAC = wm.getConnectionInfo().getMacAddress();
		} catch (Exception e) {
			m_szWLANMAC = "";
		}
		return m_szWLANMAC;
		 
	}
}
