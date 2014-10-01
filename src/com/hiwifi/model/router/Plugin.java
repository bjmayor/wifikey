/**
 * Plugin.java
 * com.hiwifi.model.router
 * hiwifiKoala
 * shunping.liu create at 20142014年8月1日下午12:28:29
 */
package com.hiwifi.model.router;

/**
 * @author shunping.liu@hiwifi.tw
 * 
 */
public class Plugin {

	private String id;
	private String iconUrl;
	private String name;
	private String switcherid;// 开关状态

	/**
	 * 
	 */
	public Plugin() {
	}

	public boolean canUninstall() {
		return true;
	}

	public boolean canClose() {
		return true;
	}

	public boolean isEnabled() {
		return true;
	}

	public void setSwitcher(boolean enabled) {

	}

}
