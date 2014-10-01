package com.hiwifi.model.wifi.adapter;

import android.content.Context;

import com.hiwifi.model.wifi.AccessPoint;

public class ConnectAdapterFactory {
	public static ConnectAdapter createAdapter(AccessPoint accessPoint,
			Context context) {
		if (accessPoint != null) {
			if (accessPoint.getPrintableSsid().equals("CMCC")) {
				return new CMCCConnectAdapter(context);
			} else if (accessPoint.getPrintableSsid().equals("ChinaNet")) {
				return new ChinanetConnectAdapter(context);
			}
		}
		return new DefaultConnectAdapter(context);
	}
}
