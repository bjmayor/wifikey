package com.hiwifi.utils;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;

public class PhoneNumberCapturer {
	public static String getNumber(Activity a) {
		TelephonyManager tm = (TelephonyManager) a
				.getSystemService(Context.TELEPHONY_SERVICE);
		String tel = tm.getLine1Number();
		if (tel != null) {
			if (tel.startsWith("+86")) {
				tel = tel.substring(3);
			}
		} else {
			tel = "";
		}
		return tel;
	}
}
