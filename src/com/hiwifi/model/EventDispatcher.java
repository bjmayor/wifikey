/**
 * EventDispatcher.java
 * com.hiwifi.model
 * hiwifiKoala
 * shunping.liu create at 20142014年8月1日下午12:59:35
 */
package com.hiwifi.model;

import android.content.Intent;

import com.hiwifi.hiwifi.Gl;

/**
 * @author shunping.liu@hiwifi.tw
 * 
 */
public class EventDispatcher {

	public static final String ACTION_USER = "user_status_changed";
	public static final String ACTION_ROUTER = "router_selected_changed";

	public static void dispatchUserStatusChanged() {
		ClientInfo.shareInstance().setCurrentUserId(
				User.shareInstance().getUid());
		Gl.Ct().sendBroadcast(new Intent(ACTION_USER));
	}

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
