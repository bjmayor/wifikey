package com.hiwifi.model.wifi.adapter;

import android.content.Context;
import android.content.Intent;

import com.hiwifi.activity.protalpage.JSTestActivity;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.model.wifi.AccessPoint;
import com.hiwifi.model.wifi.WifiAdmin;

public class DefaultConnectAdapter extends ConnectAdapter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DefaultConnectAdapter(Context context) {
		super(context);
	}

	@Override
	public void login() {
		if (!JSTestActivity.is_open) {
			JSTestActivity.is_open = true;
			Intent intent = new Intent(Gl.Ct(), JSTestActivity.class);
			AccessPoint pString = WifiAdmin.sharedInstance()
					.connectedAccessPoint();
			intent.putExtra("ssid",
					pString == null ? "" : pString.getPrintableSsid());
			intent.putExtra("from", this.getClass().toString());
			getContext().startActivity(intent);
		}
	}

	@Override
	public void logout() {

	}

	@Override
	public void cancel() {

	}

	@Override
	public Boolean supportAutoAuth() {
		return false;
	}

}
