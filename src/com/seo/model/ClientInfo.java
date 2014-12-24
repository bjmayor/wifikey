/**
 * ClientInfo.java
 * com.hiwifi.model
 * hiwifiKoala
 * shunping.liu create at 
 */
package com.seo.model;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import com.seo.constant.RequestConstant;
import com.seo.model.request.ResponseParserInterface;
import com.seo.model.request.ServerResponseParser;
import com.seo.model.request.ServerResponseParser.ServerCode;
import com.seo.store.KeyValueModel;
import com.seo.utils.DeviceUtil;
import com.seo.utils.encode.MD5Util;
import com.seo.wifikey.Gl;

/**
 * @description 设备唯一的信息都在这里管理
 * @author shunping.liu@hiwifi.tw
 * 
 */
public class ClientInfo implements ResponseParserInterface {

	private String currentUserId = "";
	private int currentRouterId;
	private String pushToken;
	private String uuid;
	private SharedPreferences sp;
	private final String spName = "clientInfo";
	private final String key_uid = "key_uid";
	private final String Key_rid = "Key_rid";
	private final String Key_isFirstStarted = "Key_isFirstStarted"
			+ Gl.getAppVersionCode();// 是否第一次启动
	private final String Key_pushToken = "Key_pushToken";
	private final String Key_uuid = "Key_uuid";// 客户端唯一编号
	private final String key_contact = "feedbacker_contact";
	private final String key_autoshare = "key_autoshare";
	private final String key_autobackup = "key_autobackup";
	private final String key_notify = "key_notify";

	private ClientInfo() {
		sp = Gl.Ct().getSharedPreferences(spName, 0);
		currentRouterId = sp.getInt(Key_rid, 0);
		currentUserId = sp.getString(key_uid, null);
		pushToken = sp.getString(Key_pushToken, null);
		autoShared = true;
		autoBackup = true;
	}

	private static ClientInfo clientInfoInstance = null;

	public synchronized static ClientInfo shareInstance() {
		if (clientInfoInstance == null) {
			clientInfoInstance = new ClientInfo();
		}
		return clientInfoInstance;
	}

	public synchronized final String getCurrentUserId() {
		return currentUserId;
	}

	public synchronized final void setCurrentUserId(String currentUserId) {
		this.currentUserId = currentUserId;
		sp.edit().putString(key_uid, currentUserId).commit();
	}

	public synchronized final int getCurrentRouterId() {
		return currentRouterId;
	}

	public synchronized final void setCurrentRouterId(int currentRouterId) {
		this.currentRouterId = currentRouterId;
		// EventDispatcher.dispatchRouterChanged();
		sp.edit().putInt(Key_rid, currentRouterId).commit();
	}

	public synchronized final String getPushToken() {
		return pushToken;
	}

	public synchronized final void setPushToken(String pushToken) {
		this.pushToken = pushToken;
		sp.edit().putString(Key_pushToken, pushToken).commit();
	}

	public synchronized final boolean isFirstStarted() {
		return sp.getBoolean(Key_isFirstStarted, true);
	}

	public synchronized final void setFirstStarted(boolean isFirstStarted) {
		sp.edit().putBoolean(Key_isFirstStarted, isFirstStarted).commit();
	}

	@SuppressWarnings("finally")
	public synchronized final String getUUID() {
		if (this.uuid != null) {
			return this.uuid;
		} else {
			this.uuid = sp.getString(Key_uuid, null);
			UUID uuid = null;
			if (this.uuid != null) {
				return this.uuid;
			} else {
				final String androidId = DeviceUtil.getAndroidId();
				try {
					if (androidId != null
							&& !"9774d56d682e549c".equals(androidId)) {
						uuid = UUID.nameUUIDFromBytes(androidId
								.getBytes("utf8"));
					} else {
						final String deviceId = DeviceUtil.getImei();
						uuid = deviceId != null ? UUID
								.nameUUIDFromBytes(deviceId.getBytes("utf8"))
								: UUID.randomUUID();
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} finally {
					if (uuid != null) {
						sp.edit().putString(Key_uuid, uuid.toString());
						this.uuid = uuid.toString();
						return this.uuid;
					} else {
						return null;
					}
				}
			}
		}

	}

	@SuppressLint("DefaultLocale")
	public synchronized final String getClientId() {
		return MD5Util
				.encryptToMD5("Hiapp2014")
				.toLowerCase();
	}

	public synchronized final String getModel() {
		return android.os.Build.BRAND + " " + android.os.Build.MODEL;
	}

	public synchronized String getUserContact() {
		return sp.getString(key_contact, "");
	}

	public synchronized void setUserContact(String userContact) {
		sp.edit().putString(key_contact, userContact).commit();
	}

	private boolean autoShared;
	private boolean autoBackup;
	private boolean notifyStatus;

	public synchronized final boolean isAutoShared() {
		autoShared = sp.getBoolean(key_autoshare, true);
		return autoShared;
	}

	public synchronized final void setAutoShared(boolean autoShared) {
		this.autoShared = autoShared;
		sp.edit().putBoolean(key_autoshare, autoShared).commit();
	}

	public synchronized final boolean isAutoBackup() {
		autoBackup = sp.getBoolean(key_autobackup, true);
		return autoBackup;
	}

	public synchronized final void setAutoBackup(boolean autoBackup) {
		this.autoBackup = autoBackup;
		sp.edit().putBoolean(key_autobackup, autoBackup).commit();
	}

    public synchronized final boolean isNotifyOpen() {
        notifyStatus = KeyValueModel.getBooleanValue(key_notify,true);
        return notifyStatus;
    }

    public synchronized final void setNotify(boolean status) {
        this.notifyStatus = status;
        KeyValueModel.setBooleanValue(key_notify,status);
    }

	boolean appNeedUpdate = false;
	public String upgradeInfo = "";

	public synchronized boolean appNeedUpdate() {
		return appNeedUpdate;
	}

	@Override
	public void parse(RequestConstant.RequestIdentify identify, ServerResponseParser parser) {
		if (parser.code == ServerCode.OK.value()) {
			switch (identify.getTag()) {
			default:
				break;
			}
		}

	}

}
