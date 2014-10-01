/**
 * Router.java
 * com.hiwifi.model.router
 * hiwifiKoala
 * shunping.liu create at 
 */
package com.hiwifi.model.router;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.model.request.ResponseParserInterface;
import com.hiwifi.model.request.ServerResponseParser;
import com.hiwifi.model.router.WifiInfo.SignalMode;

import android.text.TextUtils;

/**
 * @description 路由器信息管理类
 * @author shunping.liu@hiwifi.tw
 * 
 */
public class Router implements ResponseParserInterface {

	private int routerId;
	private String mac;
	private String aliasName;
	private boolean isOnline;
	private boolean ledStatus;
	private String np;// 当前路由连接的宽带运营商名称
	private int rptCount;// 极卫星个数
	private int appCount;// 插件个数
	private int deviceCount;// 设备个数
	private String romVersion;
	private String clientSecret = "";
	private WifiInfo wifiInfo;// wifi信息

	private RomInfo romInfo;// rom版本信息

	/**
	 * 
	 */
	public Router(int routerId) {
		this.routerId = routerId;
	}

	public synchronized final RomInfo getRomInfo() {
		return romInfo;
	}

	public synchronized final int getRouterId() {
		return routerId;
	}

	public synchronized final void setRouterId(int routerId) {
		this.routerId = routerId;
	}

	public synchronized final String getMac() {
		return mac;
	}

	public synchronized final void setMac(String mac) {
		this.mac = mac;
	}

	public synchronized final String getAliasName() {
		return aliasName;
	}

	public synchronized final void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}

	public synchronized final boolean isOnline() {
		return isOnline;
	}

	public synchronized final void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

	public synchronized final boolean isLedStatusOn() {
		return ledStatus;
	}

	public synchronized final void setLedStatus(String ledStatus) {
		this.ledStatus = ledStatus.equalsIgnoreCase("1");
	}

	public synchronized final String getNp() {
		return np;
	}

	public synchronized final void setNp(String np) {
		this.np = np;
	}

	public synchronized final int getRptCount() {
		return rptCount;
	}

	public synchronized final void setRptCount(int rptCount) {
		this.rptCount = rptCount;
	}

	public synchronized final int getAppCount() {
		return appCount;
	}

	public synchronized final void setAppCount(int appCount) {
		this.appCount = appCount;
	}

	public synchronized final String getRomVersion() {
		return romVersion;
	}

	public synchronized final void setRomVersion(String romVersion) {
		this.romVersion = romVersion;
	}

	public synchronized final String getClientSecret() {
		return clientSecret;
	}

	public synchronized final void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public synchronized final WifiInfo getWifiInfo() {
		return wifiInfo;
	}

	public synchronized final void setWifiInfo(WifiInfo wifiInfo) {
		this.wifiInfo = wifiInfo;
	}

	@Override
	public String toString() {
		return String.format(
				"{rid:%d,mac:%s,name:%s,wifiInfo:%s, clientSecret:%s}",
				routerId, mac, aliasName, wifiInfo, clientSecret);
	}

	/******/

	public boolean isPluginNeedUpgrade() {
		return false;
	}

	public boolean isRomNeedUpgrade() {
		return false;
	}

	public void save() {

	}

	public ArrayList<Plugin> getInstalledPlugin() {
		return null;
	}

	public ArrayList<Device> getConnectedDevice() {
		return null;
	}

	public boolean isBinded() {
		return !TextUtils.isEmpty(this.clientSecret);
	}

	public void onBindSucess() {

	}

	public void onUnBindSucess() {

	}

	public class RomInfo {

		public String version;
		public int size;
		public String changeLog;
		public boolean needUpgrade;
		public boolean isForceUpgrade;

		public RomInfo() {

		}

	}

	@Override
	public void parse(RequestTag tag, ServerResponseParser parser) {
		switch (tag) {
		case URL_ROUTER_UPGRADE_CHECK:
			if (this.romInfo == null) {
				this.romInfo = new RomInfo();
			}
			this.romInfo.version = parser.originResponse.optString("version",
					"unkown");
			this.romInfo.needUpgrade = 1 == parser.originResponse.optInt(
					"need_upgrade", 0);
			this.romInfo.changeLog = parser.originResponse.optString(
					"changelog", "");
			this.romInfo.size = parser.originResponse.optInt("size", 0);
			this.romInfo.isForceUpgrade = 1 == parser.originResponse.optInt(
					"force_upgrade", 0);
			break;
		case OPENAPI_CLINET_ROUTER_INFO_GET:
			JSONObject appData;
			try {
				appData = parser.originResponse.getJSONObject("app_data");
				this.ledStatus = 1 == appData.optInt("led_status", 0);
				if (this.romInfo == null) {
					this.romInfo = new RomInfo();
				}
				this.rptCount = appData.optInt("rpt_cnt", 0);
				this.deviceCount = appData.optInt("device_cnt", 0);
				this.romInfo.version = appData.optString("rom_version",
						"unkown");
				this.wifiInfo = new WifiInfo(
						appData.optString("ssid", "unkown"));
				this.wifiInfo.setWifiEnabled(appData.optInt(
						"wifi_swich_status", 1) == 1);
				if (appData.optInt("wifi_txpwr_status") == 1) {
					this.wifiInfo.setMode(SignalMode.Crossed);
				}
				this.wifiInfo.wifiSleepTimes.clear();
				this.wifiInfo.wifiSleepTimes.add(new WifiSleepTime(appData
						.optString("wifi_sleep_start"), appData
						.optString("wifi_sleep_end")));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;

		default:
			break;
		}
	}

}
