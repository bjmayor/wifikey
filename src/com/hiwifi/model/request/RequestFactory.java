/**
 * RequestFactory.java
 * com.hiwifi.model.request
 * hiwifiKoala
 * shunping.liu create at 20142014年8月8日下午1:49:52
 */
package com.hiwifi.model.request;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.hiwifi.app.utils.RecentApplicatonUtil;
import com.hiwifi.constant.ConfigConstant;
import com.hiwifi.constant.RequestConstant.RequestTag;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.model.ClientInfo;
import com.hiwifi.model.User;
import com.hiwifi.model.log.LogUtil;
import com.hiwifi.model.request.RequestManager.ResponseHandler;
import com.hiwifi.model.router.Router;
import com.hiwifi.model.router.RouterManager;
import com.hiwifi.model.router.WifiInfo.SignalMode;
import com.hiwifi.model.wifi.AccessPoint;
import com.hiwifi.model.wifi.state.Account;
import com.hiwifi.model.wifi.state.Account.Type;
import com.hiwifi.store.AccessPointDbMgr;
import com.hiwifi.store.AccessPointModel;
import com.hiwifi.store.AppInfoModel;
import com.hiwifi.support.http.RequestParams;

/**
 * @author shunping.liu@hiwifi.tw
 * 
 */
public class RequestFactory {

	// 检查app升级
	public static void checkAppUpGrade(Context context,
			ResponseHandler responseHandler) {
		RequestParams params = new RequestParams();
		params.put("ver", Gl.getAppVersionCode() + "");
		params.put("app_src", ConfigConstant.APP_SRC);
		RequestManager.requestByTag(context, RequestTag.URL_APP_UPDATE_CHECK,
				params, responseHandler);
	}

	// 发送验证码
	public static void sendVerycode(Context context,
			ResponseHandler responseHandler, String phoneNumber) {
		RequestParams params = new RequestParams();
		params.put("mobile", phoneNumber);
		params.put("app_src", ConfigConstant.APP_SRC);
		RequestManager.requestByTag(context, RequestTag.URL_USER_VERYCODE_SEND,
				params, responseHandler);
	}

	// 检查手机号是否已使用
	public static void checkMobileAvailable(Context context,
			ResponseHandler responseHandler, String phoneNumber) {
		RequestParams params = new RequestParams();
		params.put("mobile", phoneNumber);
		RequestManager.requestByTag(context, RequestTag.URL_USER_MOBILE_CHECK,
				params, responseHandler);
	}

	// 手机号登录或注册
	public static void loginByPhone(Context context,
			ResponseHandler responseHandler, String phoneNumber,
			String codeOrPassword) {
		RequestParams params = new RequestParams();
		params.put("mobile", phoneNumber);
		params.put("regcode", codeOrPassword);
		params.put("app_src", ConfigConstant.APP_SRC);
		RequestManager.requestByTag(context,
				RequestTag.URL_USER_LOGIN_BY_PHONE, params, responseHandler);
	}

	// 手机号登录或注册
	public static void regByPhone(Context context,
			ResponseHandler responseHandler, String phoneNumber, String code) {
		RequestParams params = new RequestParams();
		params.put("mobile", phoneNumber);
		params.put("regcode", code);
		params.put("app_src", ConfigConstant.APP_SRC);
		RequestManager.requestByTag(context, RequestTag.URL_USER_REG_BY_PHONE,
				params, responseHandler);
	}

	// 邮箱登录
	public static void loginByPhoneOrEmail(Context context,
			ResponseHandler responseHandler, String email, String password) {
		RequestParams params = new RequestParams();
		params.put("email", email);
		params.put("password", password);
		params.put("app_src", ConfigConstant.APP_SRC);
		RequestManager.requestByTag(context, RequestTag.URL_USER_LOGIN, params,
				responseHandler);
	}

	public static void logout(Context context, ResponseHandler responseHandler) {
		RequestParams params = new RequestParams();
		params.put("app_src", ConfigConstant.APP_SRC);
		RequestManager.requestByTag(context, RequestTag.API_OPEN_LOGOUT,
				params, responseHandler);
	}

	public static void getUserInfo(Context context,
			ResponseHandler responseHandler) {
		RequestParams map = new RequestParams();
		map.put("token", User.shareInstance().getToken());
		RequestManager.requestByTag(context, RequestTag.URL_USER_INFO_GET, map,
				responseHandler);
	}

	public static void modifyUserInfo(Context context,
			ResponseHandler responseHandler, String userName) {
		RequestParams params = new RequestParams();
		params.put("token", User.shareInstance().getToken());
		params.put("newusername", userName);
		RequestManager.requestByTag(context, RequestTag.URL_USER_NAME_EDIT,
				params, responseHandler);
	}

	public static void modifyUserInfo(Context context,
			ResponseHandler responseHandler, String userName, String password) {
		RequestParams params = new RequestParams();
		params.put("newusername", userName);
		params.put("newpwd", password);
		RequestManager.requestByTag(context, RequestTag.URL_USER_INFO_EDIT,
				params, responseHandler);
	}

	public static void modifyPassword(Context context,
			ResponseHandler responseHandler, String oldPassword,
			String newPassword) {
		RequestParams params = new RequestParams();
		params.put("oldpwd", oldPassword);
		params.put("newpwd", newPassword);
		RequestManager.requestByTag(context, RequestTag.URL_USER_PWD_EDIT,
				params, responseHandler);
	}

	public static void uploadUserPhoto(Context context,
			ResponseHandler responseHandler, Bitmap bitmap) {
		RequestParams params = new RequestParams();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(
				baos.toByteArray());
//		System.out.println("----length:"+baos.toByteArray().length/1024);
		params.put("pic", inputStream, "myphoto.png", "binary", true);
		params.put("token", User.shareInstance().getToken());
		RequestManager.requestByTag(context, RequestTag.URL_USER_AVATAR_EDIT,
				params, responseHandler);
	}

	public static void uploadUserPhoto(Context context,
			ResponseHandler responseHandler, int id) {
		RequestParams params = new RequestParams();
		params.put("token", User.shareInstance().getToken());
		params.put("pic", Gl.Ct().getResources().openRawResource(id),
				"head.jpg");
		RequestManager.requestByTag(context, RequestTag.URL_USER_AVATAR_EDIT,
				params, responseHandler);
	}

	public static void getRouters(Context context,
			ResponseHandler responseHandler) {
		RequestManager.requestByTag(context, RequestTag.URL_ROUTER_LIST_GET,
				null, responseHandler);
	}

	public static void getRouterInfo(Context context,
			ResponseHandler responseHandler) {
		RequestManager.requestByTag(context,
				RequestTag.OPENAPI_CLINET_ROUTER_INFO_GET, null,
				responseHandler);
	}

	// 获取已安装插件列表
	public static void getIntalledPlugins(Context context,
			ResponseHandler responseHandler) {
		RequestParams params = new RequestParams();
		params.put("rid", RouterManager.shareInstance().currentRouter()
				.getRouterId()
				+ "");
		RequestManager.requestByTag(context,
				RequestTag.URL_PLUGIN_INSTALLED_LIST_GET, params,
				responseHandler);
	}

	// 获取黑名单列表
	public static void getBlockedDevices(Context context,
			ResponseHandler responseHandler) {
		RequestParams params = new RequestParams();
		RequestManager.requestByTag(context,
				RequestTag.PATH_NETWORK_BLOCKED_LIST_GET, params,
				responseHandler);
	}

	/**
	 * 绑定路由器, 切换时再绑，提高效率
	 * 
	 * @param onlyCurrent
	 *            true只绑定当前路由 false绑定所有路由
	 */
	public static void bindRouters(Context context,
			ResponseHandler responseHandler, Boolean onlyCurrent) {
		String mac = "";
		Router router = RouterManager.shareInstance().currentRouter();
		if (TextUtils.isEmpty(router.getClientSecret()) && router.isOnline()) {
			mac = router.getMac();
		} else if (onlyCurrent) {
			return;
		}
		RequestParams params = new RequestParams();
		if (onlyCurrent) {
			params.put("macs", mac);
		}
		params.put("token", User.shareInstance().getToken());
		RequestManager.requestByTag(context, RequestTag.API_OPEN_BIND_SET,
				params, responseHandler);
	}

	// 获取历史流量
	public static void getTrafficWithHistory(Context context,
			ResponseHandler responseHandler) {
		RequestManager.requestByTag(context,
				RequestTag.OPENAPI_CLINET_TRAFFIC_LIST_GET, null,
				responseHandler);
	}

	// 获取实时流量
	public static void getTrafficByNow(Context context,
			ResponseHandler responseHandler) {
		RequestManager.requestByTag(context,
				RequestTag.OPENAPI_CLINET_TRAFFIC_GET, null, responseHandler);
	}

	// ///*******************信道管理**************////
	// 获取当前使用的信道
	public static void getChannel(Context context,
			ResponseHandler responseHandler) {
		RequestParams params = new RequestParams();
		// 一个路由器可能有多个wifi,先要获取wifi列表,极2上有2.4g和5g，但5g不能修改信道，暂时写死是没问题。但其实不太好
		params.put("device", "radio0.network1");
		RequestManager.requestByTag(context,
				RequestTag.OPENAPI_WIFI_CHANNEL_GET, params, responseHandler);
	}

	// 设置信道
	public static void setChannel(Context context,
			ResponseHandler responseHandler, int channel) {
		RequestParams params = new RequestParams();
		params.put("channel", channel + "");
		// 一个路由器可能有多个wifi,先要获取wifi列表
		params.put("device", "radio0.network1");
		RequestManager.requestByTag(context,
				RequestTag.OPENAPI_WIFI_CHANNEL_SET, params, responseHandler);
	}

	// 获取信道列表
	public static void getChannelRank(Context context,
			ResponseHandler responseHandler) {
		RequestManager
				.requestByTag(context,
						RequestTag.OPENAPI_WIFI_CHANNEL_RANK_GET, null,
						responseHandler);
	}

	// 获取wifi睡眠时间
	public static void getSleepTime(Context context,
			ResponseHandler responseHandler) {
		RequestManager
				.requestByTag(context,
						RequestTag.OPENAPI_CLIENT_WIFI_SLEEP_GET, null,
						responseHandler);
	}

	// 设置wifi睡眠时间
	public static void setSleepTime(Context context,
			ResponseHandler responseHandler, String startTime, String endTime) {
		RequestParams params = new RequestParams();
		params.put("start", startTime);
		params.put("end", endTime);
		RequestManager.requestByTag(context,
				RequestTag.OPENAPI_CLIENT_WIFI_SLEEP_SET, params,
				responseHandler);
	}

	// ///*******************加速管理**************////
	// 设置某设备一键加速
	public static void setDeviceSpeedUp(Context context,
			ResponseHandler responseHandler, String deviceMac) {
		RequestParams params = new RequestParams();
		params.put("item_id", deviceMac);
		RequestManager.requestByTag(context,
				RequestTag.OPENAPI_CLINET_SPEEDUP_SET, params, responseHandler);
	}

	// 取消某设备的加速
	public static void cancelDeviceSpeedUp(Context context,
			ResponseHandler responseHandler, String deviceMac) {
		RequestParams params = new RequestParams();
		params.put("item_id", deviceMac);
		RequestManager.requestByTag(context,
				RequestTag.OPENAPI_CLINET_CANCEL_SPEEDUP, params,
				responseHandler);
	}

	// 获取加速设备列表
	public static void getSpeedUpDevices(Context context,
			ResponseHandler responseHandler) {
		RequestManager.requestByTag(context,
				RequestTag.OPENAPI_CLINET_SPEEDUPLIST_GET, null,
				responseHandler);
	}

	// 获取Qos配置
	public static void getQos(Context context, ResponseHandler responseHandler) {
		RequestManager.requestByTag(context, RequestTag.OPENAPI_CLINET_QOS_GET,
				null, responseHandler);
	}

	// 设置Qos配置
	public static void setQos(Context context, ResponseHandler responseHandler,
			String mac, String up, String down, String name) {
		RequestParams params = new RequestParams();
		params.put("mac", mac);
		params.put("up", up);
		params.put("down", down);
		params.put("name", name);
		RequestManager.requestByTag(context, RequestTag.OPENAPI_CLINET_QOS_SET,
				params, responseHandler);
	}

	// ///*******************消息相关**************////
	// 获取消息列表
	public static void getMessageList(Context context,
			ResponseHandler responseHandler, int startId, int fetchNum) {
		RequestParams params = new RequestParams();
		params.put("start", startId + "");
		params.put("count", fetchNum + "");
		RequestManager.requestByTag(context,
				RequestTag.API_MESSAGE_MESSAGE_LIST_GET, params,
				responseHandler);
	}

	// 获取消息未读数
	public static void getNumOfUnReadMessage(Context context,
			ResponseHandler responseHandler) {
		RequestManager.requestByTag(context, RequestTag.API_MESSAGE_UNREAD_GET,
				null, responseHandler);
	}

	// 获取消息详情
	public static void getMessageDetail(Context context,
			ResponseHandler responseHandler, int mid) {
		RequestParams params = new RequestParams();
		params.put("mid", mid + "");
		RequestManager.requestByTag(context, RequestTag.API_MESSAGE_VIEW_GET,
				params, responseHandler);
	}

	// 设置所有消息为已读
	public static void setAllMessageAsRead(Context context,
			ResponseHandler responseHandler) {
		RequestManager.requestByTag(context,
				RequestTag.API_MESSAGE_ALL_READ_SET, null, responseHandler);
	}

	// 删除所有消息，指请求服务器删除
	public static void deleteAllMessage(Context context,
			ResponseHandler responseHandler) {
		RequestManager.requestByTag(context, RequestTag.API_MESSAGE_DELETE_ALL,
				null, responseHandler);
	}

	/**
	 * 获取推送消息开关状态 只返回处于关闭状态的状态开关 返回空列表代表全部是开启状态
	 */
	public static void getMessagePushStatus(Context context,
			ResponseHandler responseHandler) {
		RequestParams params = new RequestParams();
		// 参数名不一致，设计有问题，持保留意见
		params.put("logintoken", User.shareInstance().getToken());
		params.put("pushtoken", ClientInfo.shareInstance().getPushToken());
		params.put("getuitoken", ClientInfo.shareInstance().getPushToken());
		RequestManager.requestByTag(context, RequestTag.API_MESSAGE_SWITCH_GET,
				params, responseHandler);
	}

	// 获取消息列表
	public static void setMessagePushStatus(Context context,
			ResponseHandler responseHandler, int messageType, Boolean enabled) {
		RequestParams params = new RequestParams();
		params.put("logintoken", User.shareInstance().getToken());
		params.put("pushtoken", ClientInfo.shareInstance().getPushToken());
		params.put("getuitoken", ClientInfo.shareInstance().getPushToken());
		params.put("msgtype", messageType + "");
		params.put("status", enabled ? "1" : "0");
		RequestManager.requestByTag(context, RequestTag.API_MESSAGE_SWITCH_SET,
				params, responseHandler);
	}

	/*********************** 路由器相关操作 ************/
	// 重启当前路由器
	public static void rebootCurrentRouter(Context context,
			ResponseHandler responseHandler) {
		RequestManager.requestByTag(context, RequestTag.URL_ROUTER_REBOOT,
				null, responseHandler);
	}

	// 设置路由器名称
	public static void setRouterName(Context context,
			ResponseHandler responseHandler, String name) {
		RequestParams params = new RequestParams();
		params.put("rid", RouterManager.shareInstance().currentRouter()
				.getRouterId()
				+ "");
		params.put("name", name);
		RequestManager.requestByTag(context, RequestTag.URL_ROUTER_NAME_SET,
				params, responseHandler);
	}

	// 设置led状态
	public static void getLedStatus(Context context,
			ResponseHandler responseHandler) {
		RequestManager
				.requestByTag(context,
						RequestTag.OPENAPI_CLINET_LED_STATUS_GET, null,
						responseHandler);
	}

	// 设置led状态
	public static void setLedStatus(Context context,
			ResponseHandler responseHandler, Boolean isOn) {
		if (isOn) {
			RequestManager.requestByTag(context,
					RequestTag.OPENAPI_CLINET_LED_STATUS_SET_ON, null,
					responseHandler);
		} else {
			RequestManager.requestByTag(context,
					RequestTag.OPENAPI_CLINET_LED_STATUS_SET_OFF, null,
					responseHandler);
		}

	}

	public static void getWiFiStatus(Context context,
			ResponseHandler responseHandler) {
		RequestManager.requestByTag(context,
				RequestTag.OPENAPI_CLIENT_WIFI_SWITCH_GET, null,
				responseHandler);
	}

	// 设置wifi开关
	public static void setWiFiStatus(Context context,
			ResponseHandler responseHandler, Boolean isOn) {
		RequestParams params = new RequestParams();
		params.put("rid", RouterManager.shareInstance().currentRouter()
				.getRouterId()
				+ "");
		params.put("status", isOn ? "1" : "0");
		RequestManager.requestByTag(context,
				RequestTag.OPENAPI_CLIENT_WIFI_SWITCH_SET, params,
				responseHandler);
	}

	// 检查Rom升级
	public static void checkRomUpgrade(Context context,
			ResponseHandler responseHandler) {
		RequestParams params = new RequestParams();
		params.put("rid", RouterManager.shareInstance().currentRouter()
				.getRouterId()
				+ "");
		RequestManager.requestByTag(context,
				RequestTag.URL_ROUTER_UPGRADE_CHECK, params, responseHandler);
	}

	// 设置信号强度模式
	public static void setSignalMode(Context context,
			ResponseHandler responseHandler, SignalMode mode) {
		RequestParams params = new RequestParams();
		params.put("device", "radio0.network1");
		params.put("txpwr", mode.value());
		RequestManager.requestByTag(context,
				RequestTag.OPENAPI_CLIENT_ROUTER_CROSS_STATUS_SET, params,
				responseHandler);
	}

	// 获取信号强度模式
	public static void getSignalMode(Context context,
			ResponseHandler responseHandler) {
		RequestParams params = new RequestParams();
		params.put("device", "radio0.network1");
		RequestManager.requestByTag(context,
				RequestTag.OPENAPI_CLIENT_ROUTER_CROSS_STATUS_GET, params,
				responseHandler);
	}

	/****************** 设备相关 **********************/
	public static void getConnectedDevices(Context context,
			ResponseHandler responseHandler) {
		RequestManager.requestByTag(context,
				RequestTag.OPENAPI_CLINET_DEVICE_LIST_GET, null,
				responseHandler);

	}

	/************** 天天wifi相关 ***************/
	// TODO 返回数据格式可能会变
	public static void getRecommendApps(Context context,
			ResponseHandler responseHandler) {
		RequestParams params = new RequestParams();
		params.put("app_type", "hiwifi");
		params.put("token", User.shareInstance().hasLogin() ? User
				.shareInstance().getToken() : "");
		params.put("client_id", ClientInfo.shareInstance().getClientId());
		RequestManager.requestByTag(context,
				RequestTag.HIWIFI_APP_RECOMMEND_GET, params, responseHandler);
	}

	// 获取周围
	// TODO 格式要变，需要检查一下。
	public static void getPasswords(Context context,
			ResponseHandler responseHandler, List<AccessPoint> list) {
		RequestParams params = new RequestParams();
		JSONArray dataArray = RequestBodyBuilder.buildGetPwdJsonObject(list);
		LogUtil.d("Password:", "json: " + dataArray.toString());
		params.put(RequestManager.key_json, dataArray.toString());
		RequestManager.requestByTag(context, RequestTag.HIWIFI_PWD_GET, params,
				responseHandler);
	}

	// 获取商家密码
	public static void getOpPassword(Context context,
			ResponseHandler responseHandler, Account.Type opType) {
		RequestParams params = new RequestParams();
		params.put("type", Type.TypeIsCmcc.ordinal() + "");
		RequestManager.requestByTag(context, RequestTag.HIWIFI_OPONE_GET,
				params, responseHandler);
	}

	// 上报登录日志
	public static void sendOpLog(Context context,
			ResponseHandler responseHandler, final String code,
			final String reason, final Account account, final String action) {
		RequestParams params = new RequestParams();
		params.put("error_code", code);
		params.put("action", action);
		params.put("error_text", reason);
		if (account != null) {
			params.put("aid", account.getAid() + "");
			params.put("mobile", account.getUsername());
			params.put("type", account.getType() + "");
		}
		params.put("act_date", new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss").format(new Date()));
		RequestManager.requestByTag(context, RequestTag.HIWIFI_OPLOG_SEND,
				params, responseHandler);
	}

	// 上报扫描到的ap列表
	public static void sendApList(Context context,
			ResponseHandler responseHandler, List<AccessPointModel> listToUpload) {
		RequestParams params = new RequestParams();
		params.put(RequestManager.key_json, RequestBodyBuilder
				.buildSyncUpJsonObject(listToUpload).toString());
		RequestManager.requestByTag(context, RequestTag.HIWIFI_APLIST_SEND,
				params, responseHandler);
	}

	// 获取配置信息
	public static void getConfig(Context context,
			ResponseHandler responseHandler) {
		RequestManager.requestByTag(context, RequestTag.HIWIFI_CONFIG_GET,
				null, responseHandler);
	}

	// 保存我的wifi列表
	public static void sendMyApList(Context context,
			ResponseHandler responseHandler) {
		List<AccessPointModel> list = AccessPointDbMgr.shareInstance()
				.getLocalAPList();
		RequestParams params = new RequestParams();
		params.put(RequestManager.key_json, RequestBodyBuilder
				.buildSaveWifiBackupJsonObject(list).toString());
		RequestManager.requestByTag(context, RequestTag.HIWIFI_MYAPLIST_SEND,
				params, responseHandler);
	}

	// 获取剩余时间
	public static void getRemainTime(Context context,
			ResponseHandler responseHandler) {
		RequestManager.requestByTag(context, RequestTag.HIWIFI_TIME_GET, null,
				responseHandler);
	}

	// 获取不自动连接的wifi列表
	public static void getSSIDThanNotAutoConnected(Context context,
			ResponseHandler responseHandler) {
		RequestManager.requestByTag(context, RequestTag.HIWIFI_BLOCKEDWIFI_GET,
				null, responseHandler);
	}

	// 获取已分享平台
	public static void getSharedPlatform(Context context,
			ResponseHandler responseHandler) {
		RequestManager.requestByTag(context,
				RequestTag.HIWIFI_STATUS_SHAREDAPP_GET, null, responseHandler);
	}

	// 设置分享状态
	public static void setPlathformSharedStatus(Context context,
			ResponseHandler responseHandler) {
		RequestManager.requestByTag(context,
				RequestTag.HIWIFI_SHAREDAPP_REPORT, null, responseHandler);
	}

	// 设置分享状态
	public static void setRouterShared(Context context,
			ResponseHandler responseHandler, final String mac,
			final boolean enabled, final String rid) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("mac", mac);
		map.put("status", enabled ? "1" : "0");
		RequestManager.requestByTag(context,
				RequestTag.HIWIFI_ROUTER_SHARE_SET, null, responseHandler);
	}

	// 设置分享状态
	public static void getRouterBuyConfig(Context context,
			ResponseHandler responseHandler) {
		RequestManager.requestByTag(context,
				RequestTag.HIWIFI_CONFIG_BUYROUTER_GET, null, responseHandler);
	}

	// 发送最近打开的设备列表
	public static void sendRecentOpenedAppList(Context context,
			ResponseHandler responseHandler) {
		ArrayList<AppInfoModel> list = RecentApplicatonUtil
				.getAllInstallApplications();
		RequestParams params = new RequestParams();
		params.put(RequestManager.key_json, RequestBodyBuilder
				.buildUploadAppJsonObject(list).toString());
		RequestManager.requestByTag(context, RequestTag.HIWIFI_RECENTAPP_SEND,
				params, responseHandler);
	}

	// 发送安装的app列表
	public static void sendInstalledAppList(Context context,
			ResponseHandler responseHandler) {
		ArrayList<AppInfoModel> list = RecentApplicatonUtil
				.getAllInstallApplications();
		RequestParams params = new RequestParams();
		params.put(RequestManager.key_json, RequestBodyBuilder
				.buildUploadAppJsonObject(list).toString());
		RequestManager.requestByTag(context, RequestTag.HIWIFI_ALLAPP_SEND,
				params, responseHandler);
	}

	// 上报crashlog
	public static void sendCrashLog(Context context,
			ResponseHandler responseHandler, String log) {
		RequestParams params = new RequestParams();
		params.put(RequestManager.key_json, log);
		RequestManager.requestByTag(context, RequestTag.HIWIFI_CRASH_SEND,
				params, responseHandler);
	}

	public static void setPushToken(Context context,
			ResponseHandler responseHandler, String clientId) {
		RequestParams map = new RequestParams();
		map.put("token", User.shareInstance().getToken());
		map.put("push_token", clientId);
		map.put("app_type", ConfigConstant.APP_SRC);
		map.put("app_src", ConfigConstant.APP_SRC);
		RequestManager.requestByTag(context, RequestTag.URL_PUSHTOKEN_SET, map,
				responseHandler);
	}

	// 上报需要portal的wifi
	public static void sendPortalWifi(Context context,
			ResponseHandler responseHandler, AccessPoint accessPoint) {
		// TODO
	}

	// 获取密码已查看次数
	public static void getPasswordViewTimes(Context context,
			ResponseHandler responseHandler) {
		RequestParams map = new RequestParams();
		map.put("token", User.shareInstance().getToken());
		RequestManager.requestByTag(context,
				RequestTag.HIWIFI_PWD_VIEWTIMES_GET, map, responseHandler);
	}

	// 上报密码被查看
	public static void sendPasswordHasViewed(Context context, String bssid,
			ResponseHandler responseHandler) {
		RequestParams map = new RequestParams();
		map.put("token", User.shareInstance().getToken());
		map.put("bssid", bssid);
		RequestManager.requestByTag(context, RequestTag.HIWIFI_PWD_VIEWD_SET,
				map, responseHandler);
	}

	// 获取用户密码(找回密码用)
	public static void resetUserPassword(Context context,
			ResponseHandler responseHandler, String userPhone, String veryCode) {
		RequestParams map = new RequestParams();
		map.put("secode", veryCode);
		map.put("mobile", userPhone);
		RequestManager.requestByTag(context, RequestTag.URL_USER_PWD_RESET,
				map, responseHandler);
	}

	// TODO 获取发现列表
	public static void getDiscoverList(Context context,
			ResponseHandler responseHandler) {
		RequestManager.requestByTag(context,
				RequestTag.HIWIFI_DISCOVER_LIST_GET, null, responseHandler);
	}
	

}
