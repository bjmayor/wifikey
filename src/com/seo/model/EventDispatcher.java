/**
 * EventDispatcher.java
 * com.hiwifi.model
 * hiwifiKoala
 * shunping.liu create at 20142014年8月1日下午12:59:35
 */
package com.seo.model;

/**
 * @author shunping.liu@hiwifi.tw
 * 
 */
public class EventDispatcher {

	public static final String ACTION_USER = "user_status_changed";
	public static final String ACTION_ROUTER = "router_selected_changed";



	public static void dispatchPluginNeedUpgrade() {

	}

	public static void dispatchRomNeedUpgrade() {

	}

//	public static void dispatchRouterChanged() {
//		Gl.getContext().sendBroadcast(new Intent(ACTION_ROUTER));
//	}

	public static void dispatchMessageMarkAsRead() {

	}

	public static void dispatchMessageDeleteAll() {

	}

//	public static void dispatchPushMessage(PushMessage pushMessage) {
//
//	}
}
