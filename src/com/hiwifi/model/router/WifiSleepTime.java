/**
 * WifiSleepTime.java
 * com.hiwifi.model.router
 * hiwifiKoala
 * shunping.liu create at 20142014年8月1日下午12:55:59
 */
package com.hiwifi.model.router;

/**
 * @author shunping.liu@hiwifi.tw
 * 
 */
public class WifiSleepTime {

	public String wifiSleepStart;
	public String wifiSleepEnd;

	/**
	 * 
	 */
	public WifiSleepTime() {
	}

	public WifiSleepTime(String sleepStart, String sleepEnd) {
		this.wifiSleepStart = sleepStart;
		this.wifiSleepEnd = sleepEnd;
	}

}
