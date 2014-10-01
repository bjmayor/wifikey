/**
 * Device.java
 * com.hiwifi.model.router
 * hiwifiKoala
 * shunping.liu create at 20142014年8月1日下午12:29:00
 */
package com.hiwifi.model.router;

/**
 * @author shunping.liu@hiwifi.tw
 * 
 */
public class Device {

	private String mac;
	private String iconName;
	private String name;

	/**
	 * 
	 */
	public Device() {
	}

	public boolean isBlocked() {
		return false;
	}

	public void setBlocked(boolean blocked) {

	}

	public void setSpeedLimit(double upSpeed, double downSpeed) {

	}

	public boolean isOnline() {
		return true;
	}

}
