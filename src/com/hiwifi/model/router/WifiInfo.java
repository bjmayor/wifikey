/**
 * WifiInfo.java
 * com.hiwifi.model.router
 * hiwifiKoala
 * shunping.liu create at 20142014年8月1日下午12:48:16
 */
package com.hiwifi.model.router;

import java.util.ArrayList;

import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.model.request.ResponseParserInterface;
import com.hiwifi.model.request.ServerResponseParser;

/**
 * @author shunping.liu@hiwifi.tw
 * 
 */
public class WifiInfo implements ResponseParserInterface {

	private String ssid;
	private String device;
	private boolean isWifiEnabled;
	private boolean isWiFiSleepOn;
	private SignalMode mode;
	public ArrayList<WifiSleepTime> wifiSleepTimes = new ArrayList<WifiSleepTime>();

	public enum SignalMode {
		Unkown("unkown"), Max("max"), Min("min"), Mid("mid"), Crossed("140");
		String value;

		private SignalMode(String value) {
			this.value = value;
		}

		public String value() {
			return this.value;
		}
	}

	public synchronized void setMode(SignalMode mode) {
		this.mode = mode;
	}

	/**
	 * 
	 */
	public WifiInfo(String ssid) {
		this.ssid = ssid;
		this.mode = SignalMode.Unkown;
	}

	@Override
	public String toString() {
		return String.format("{ssid:%s, device:%s}", ssid, device);
	}

	public synchronized final String getSsid() {
		return ssid;
	}

	public synchronized final void setSsid(String ssid) {
		this.ssid = ssid;
	}

	public synchronized final String getDevice() {
		return device;
	}

	public synchronized final void setDevice(String device) {
		this.device = device;
	}

	public synchronized final boolean isWifiEnabled() {
		return isWifiEnabled;
	}

	public synchronized final void setWifiEnabled(boolean isWifiEnabled) {
		this.isWifiEnabled = isWifiEnabled;
	}

	public synchronized final boolean isWiFiSleepOn() {
		return wifiSleepTimes.size()>0;
	}

	public synchronized final void setWiFiSleepOn(boolean isWiFiSleepOn) {
		this.isWiFiSleepOn = isWiFiSleepOn;
	}

	public synchronized final boolean isCrossModeOn() {
		return mode == SignalMode.Crossed;
	}

	@Override
	public void parse(RequestTag tag, ServerResponseParser parser) {

	}

}
