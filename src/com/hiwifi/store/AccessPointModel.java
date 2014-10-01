package com.hiwifi.store;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.text.TextUtils;

import com.hiwifi.store.AccessPointDbMgr.AccessPointTable;
import com.hiwifi.utils.encode.Security;

public class AccessPointModel extends Observable implements Serializable,
		Runnable {

	public int id = -1;
	public String SSID = "";
	public String BSSID = "";
	public float longitude = 0;
	public float latitude = 0;
	public int createTime = 0;
	public int updateTime = 0;
	private String password = "";// 这个是明文
	private String userName = "";
	public int passwordType = PasswordSource.PasswordSourceLocal.ordinal();
	public int syncUpTime = 0;
	public int syncDownTime = 0;
	public int useTimes = 0;// 使用次数

	public int level = 0;// scanresult 级别
	public String capability;// scanresult
	public int channel;// scanresult

	public String account;// cmcc
	public int passwordStatus = PasswordStatus.PasswordStatusInited.ordinal();
	private int isPortal = PortalFalse;
	public static final int PortalTrue = 1;
	public static final int PortalFalse = 0;

	public static enum PasswordStatus {
		PasswordStatusInited, PasswordStatusValid, PasswordStatusInvalid
	}

	public static enum PasswordSource {
		PasswordSourceLocal, PasswordSourceRemote, PasswordSourceSpecial// cmcc等
	}

	public void sync() {
		new Thread(this).start();
	}

	// need decode the password geting from server
	public String decodePassword(String encoded) {
		if (encoded != null && !TextUtils.isEmpty(encoded)) {
			return Security.decode_string(encoded);
		}
		return "";
	}

	public AccessPointModel() {
		super();
	}

	public AccessPointModel(ScanResult scanResult) {
		super();
		updateByScanResult(scanResult);
		this.BSSID = scanResult.BSSID;
	}

	public void updateByScanResult(ScanResult scanResult) {
		this.SSID = scanResult.SSID;
		this.createTime = (int) System.currentTimeMillis() / 1000;
		this.updateTime = (int) System.currentTimeMillis() / 1000;
		this.level = scanResult.level;
		this.capability = scanResult.capabilities;
		this.channel = scanResult.frequency;
	}

	public void updateByCursor(Cursor cursor) {
		this.id = cursor.getInt(0);
		this.SSID = cursor.getString(1);
		this.BSSID = cursor.getString(2);
		this.longitude = cursor.getFloat(3);
		this.latitude = cursor.getFloat(4);
		this.createTime = cursor.getInt(5);
		this.updateTime = cursor.getInt(6);
		this.password = decodePassword(cursor.getString(7));
		this.passwordType = cursor.getInt(8);
		this.syncUpTime = cursor.getInt(9);
		this.syncDownTime = cursor.getInt(10);
		this.useTimes = cursor.getInt(11);
		this.level = cursor.getInt(12);
		this.capability = cursor.getString(13);
		this.channel = cursor.getInt(14);
		this.account = cursor.getString(15);
		this.passwordStatus = cursor.getInt(16);
		this.isPortal = cursor.getInt(17);
	}

	public AccessPointModel(Cursor cursor) {
		super();
		updateByCursor(cursor);
	}

	/*
	 * public void setPassword(String password, Boolean hasEncode,
	 * PasswordSource type) { if(hasEncode) { this.password =
	 * decodePassword(password); } else { this.password = password; }
	 * this.passwordType = type.ordinal(); setChanged(); notifyObservers(this);
	 * }
	 */

	public void setUserCount(String userName, String password,
			Boolean hasEncode, PasswordSource type) {
		if (hasEncode) {
			this.password = decodePassword(password);
		} else {
			this.password = password;
		}
		this.passwordType = type.ordinal();
		this.userName = userName;
		sync();
		setChanged();
		notifyObservers(this);
	}

	public AccessPointModel setPasswordStatus(PasswordStatus status) {
		this.passwordStatus = status.ordinal();
		if (status == PasswordStatus.PasswordStatusInvalid) {
			this.syncUpTime = 0;// 重设一下。才会重新传服务器
		}
		return this;
	}

	public boolean isPortal() {
		return isPortal == PortalTrue;
	}

	public void setIsPortal(boolean isPortal) {
		if (isPortal) {
			this.isPortal = PortalTrue;
		} else {
			this.isPortal = PortalFalse;
		}
	}

	public String getPassword(Boolean needEncode) {
		if (needEncode) {
			return Security.encode_string(this.password);
		} else {
			return this.password;
		}
	}

	public String getUserName() {
		return this.userName;
	}

	public void resetAp() {
		setUserCount("", "", false, PasswordSource.PasswordSourceLocal);
	}

	@Override
	public void run() {
		if (this.id != -1) {
			AccessPointDbMgr mgr = AccessPointDbMgr.shareInstance();
			Map<String, String> fieldMap = new HashMap<String, String>();
			fieldMap.put(AccessPointTable.FLD_BSSID, this.BSSID);
			fieldMap.put(AccessPointTable.FLD_createTime, this.createTime + "");
			fieldMap.put(AccessPointTable.FLD_latitude, this.latitude + "");
			fieldMap.put(AccessPointTable.FLD_longitude, this.longitude + "");
			if (!TextUtils.isEmpty(this.password)) {
				fieldMap.put(AccessPointTable.FLD_password,
						Security.encode_string(this.password));
				fieldMap.put(AccessPointTable.FLD_passwordstatus,
						this.passwordStatus + "");
			}
			fieldMap.put(AccessPointTable.FLD_passwordType, this.passwordType
					+ "");
			fieldMap.put(AccessPointTable.FLD_SSID, this.SSID);
			fieldMap.put(AccessPointTable.FLD_syncDownTime, this.syncDownTime
					+ "");
			fieldMap.put(AccessPointTable.FLD_syncUpTime, this.syncUpTime + "");
			fieldMap.put(AccessPointTable.FLD_useTimes, this.useTimes + "");
			fieldMap.put(AccessPointTable.FLD_level, this.level + "");
			fieldMap.put(AccessPointTable.FLD_channel, this.channel + "");
			fieldMap.put(AccessPointTable.FLD_capability, this.capability + "");
			fieldMap.put(AccessPointTable.FLD_account, this.account);
			mgr.updateByMap(this.id, fieldMap);
		}
	}

	@Override
	public String toString() {
		return String
				.format("{AccessPointModel:{id:%d,SSID:%s, BSSID:%s,longitude:%f,latitude:%f,useTimes:%d}}",
						this.id, this.SSID, this.BSSID, this.longitude,
						this.latitude, this.useTimes);
	}

}
