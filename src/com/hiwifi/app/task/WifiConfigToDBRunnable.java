package com.hiwifi.app.task;

import java.util.Iterator;
import java.util.List;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.os.Build;
import android.text.TextUtils;

import com.hiwifi.model.wifi.AccessPoint;
import com.hiwifi.model.wifi.WifiAdmin;
import com.hiwifi.store.AccessPointModel;

public class WifiConfigToDBRunnable extends DaemonTask {
	@Override
	public void execute() {
		List<WifiConfiguration> configs = WifiAdmin.sharedInstance()
				.getWifiManager().getConfiguredNetworks();
		if (configs != null && configs.size() > 0 && !isCanceled()) {
			Iterator<WifiConfiguration> iterator = configs.iterator();
			while (iterator.hasNext()) {
				WifiConfiguration config = (WifiConfiguration) iterator.next();
				if (!config.allowedKeyManagement.get(KeyMgmt.NONE)) {
					if (config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)
							|| config.allowedKeyManagement.get(KeyMgmt.WPA_EAP)) {
						String username = "";
						String password = "";
						if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR2){
							WifiEnterpriseConfig enterpriseConfig = config.enterpriseConfig;
							if(enterpriseConfig!=null){
								username = enterpriseConfig.getIdentity();
								password = enterpriseConfig.getPassword();
							}
						}
						if (!TextUtils.isEmpty(username)
								&& !TextUtils.isEmpty(password)) {
							AccessPoint accessPoint = WifiAdmin
									.sharedInstance().getAccessPointByBSSID(
											config.BSSID);
							if (accessPoint != null) {
								accessPoint
										.getDataModel()
										.setUserCount(
												username,
												password,
												false,
												AccessPointModel.PasswordSource.PasswordSourceLocal);
								accessPoint.sync();
							}
						}
					} else {
						String passwordString = config.preSharedKey;
						String webkeyPass = config.wepKeys[0];
						if (!TextUtils.isEmpty(passwordString)
								&& !passwordString.equals("*")) {
							AccessPoint accessPoint = WifiAdmin
									.sharedInstance().getAccessPointByBSSID(
											config.BSSID);
							if (accessPoint != null) {
								// accessPoint
								// .getDataModel()
								// .setPassword(
								// passwordString,
								// false,
								// AccessPointModel.PasswordSource.PasswordSourceLocal);
								accessPoint
										.getDataModel()
										.setUserCount(
												"",
												passwordString,
												false,
												AccessPointModel.PasswordSource.PasswordSourceLocal);
								accessPoint.sync();
							}

						} else if (!TextUtils.isEmpty(webkeyPass)
								&& !webkeyPass.equals("*")) {
							AccessPoint accessPoint = WifiAdmin
									.sharedInstance().getAccessPointByBSSID(
											config.BSSID);
							if (accessPoint != null) {
								// accessPoint
								// .getDataModel()
								// .setPassword(
								// passwordString,
								// false,
								// AccessPointModel.PasswordSource.PasswordSourceLocal);
								accessPoint
										.getDataModel()
										.setUserCount(
												"",
												webkeyPass,
												false,
												AccessPointModel.PasswordSource.PasswordSourceLocal);
								accessPoint.sync();
							}
						}
					}
				}
			}
		}
		onFinished(true);
	}

}
