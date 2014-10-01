/**
 * RequestConstant.java
 * com.hiwifi.constant
 * hiwifiKoala
 * shunping.liu create at 20142014��8��1������11:24:03
 */
package com.hiwifi.constant;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.hiwifi.hiwifi.Gl;
import com.hiwifi.support.http.RequestParams;

/**
 * http 请求管理类
 * 
 * @author shunping.liu@hiwifi.tw
 * 
 */
public class RequestConstant implements Serializable {
	private static final long serialVersionUID = 1L;

	public static enum ContentType {
		JSON("application/json"), URLENCODE("application/x-www-form-urlencoded"), HTML(
				"text/html"), XML("text/xml"), BINARY(
				"application/octet-stream");
		String type;

		public String getType() {
			return type;
		}

		private ContentType(String type) {
			this.type = type;
		}
	}

	public static final String GET = "get";
	public static final String POST = "post";
	public static final String JSON = "json";
	public static final String BINARY = "binary";

	public static String getUrl(RequestTag tag) {
		if (tag.getType().equals(TAG_TYPE_TWX)) {
			return URL_BASE_TWX + tag.value();
		} else if (tag.getType().equals(TAG_TYPE_TWX_PATH)) {
			return URL_BASE_TWX3;
		} else if (tag.getType().equals(TAG_TYPE_OPEN)) {
			return URL_BASE_TWX + tag.value();
		} else if (tag.getType().equals(TAG_TYPE_OPEN_CLIENT)) {
			return HOST_OPEN_CLIENT + RequestTag.OPENAPI_CALL.value();
		} else if (tag.getType().equals(TAG_TYPE_M_S)) {
			return HOST_M_S + tag.value();
		} else if (tag.getType().equals(TAG_TYPE_M)) {
			return HOST_M + tag.value();
		} else if (tag.getType().equals(TAG_TYPE_APP)) {
			return HOST_APP_S + tag.value();
		} else if (tag.getType().equals(TAG_TYPE_USER)) {
			// if (ReleaseConstant.ISDEBUG) {TAG_TYPE_HWF
			// return "http://hongbin.liu.user.hiwifi-dev.com/" + tag.value();
			// }
			return HOST_USER_S + tag.value();
		} else if (tag.getType().equals(TAG_TYPE_WEB)) {
			return tag.value();
		} else if (tag.getType().equals(TAG_TYPE_HWF)) {
			return HOST_HIWIFI + tag.value();
		} else if (tag.getType().equals(TAG_TYPE_HWF_S)) {
			return HOST_HIWIFI_HTTPS + tag.value();
		}
		return tag.value();

	}

	public static final String HOST_M = "http://m.hiwifi.com/";
	public static final String HOST_M_S = "https://m.hiwifi.com/";
	public static final String HOST_APP = "http://app.hiwifi.com/";
	public static final String HOST_APP_S = "https://app.hiwifi.com/";
	public static final String HOST_USER_S = "https://user.hiwifi.com/";
	public static final String HOST_OPEN = "http://openapi.hiwifi.com/";
	public static final String HOST_OPEN_CLIENT = "http://client.openapi.hiwifi.com/";
	public static final String URL_BASE_TWX = HOST_APP_S
			+ "/router.php?m=json&android_client_ver=" + Gl.getAppVersionCode()
			+ "&a=";
	public static final String URL_BASE_TWX3 = HOST_APP_S
			+ "/router.php?m=json&android_client_ver=" + Gl.getAppVersionCode()
			+ "&a=twx_api";
	public static final String HOST_HIWIFI = "http://hf.hiwifi.com";
	public static final String HOST_HIWIFI_HTTPS = "https://hf.hiwifi.com";

	public static final String TAG_TYPE_TWX = "#twx#";
	public static final String TAG_TYPE_TWX_PATH = "#twx_path#";
	public static final String TAG_TYPE_OPEN = "#open#";
	public static final String TAG_TYPE_OPEN_CLIENT = "#open_client#";
	public static final String TAG_TYPE_M_S = "#ms#";
	public static final String TAG_TYPE_M = "#m#";
	public static final String TAG_TYPE_APP = "#app#";
	public static final String TAG_TYPE_APP_S = "#app_#";
	public static final String TAG_TYPE_USER = "#user#";
	public static final String TAG_TYPE_WEB = "#web#";
	public static final String TAG_TYPE_HWF = "#hiwifi#";
	public static final String TAG_TYPE_HWF_S = "#hiwifi_https#";

	/** localkey */
	public static final String LOCALKEY = "oLKmbg1g";
	public static final String APP_ID = "13";
	public static final String APP_SECRET = "srt653fa58ac5c30ccee40b66553a429";

	// 请求地址都在这里定义，禁止出现在其它地方
	public enum RequestTag {
		URL_BUY("http://r.hiwifi.com/4990", TAG_TYPE_WEB), // 极路由购买地址
		URL_APP_DOWNLOAD(HOST_M + "/xiazai/app?type=hiwififree",
				TAG_TYPE_WEB), // app下载地址/xiazai/app?type=hiwififree
		URL_PLUGIN_SET("/m/set_app?android_client_ver="
				+ Gl.getAppVersionCode(), TAG_TYPE_APP), // 更新插件状态
		URL_MOVE("http://www.hiwifi.com/download/Movie/iphone_info.mov",
				TAG_TYPE_WEB), URL_BUY_JISATELLITE(
				"http://www.hiwifi.com/twxredirect/?type=buyjwx", TAG_TYPE_WEB), // 购买极卫星
		URL_ROUTER_INFO(
				"http://4006024680.com/cgi-bin/turbo/api/system/get_info?android_client_ver="
						+ Gl.getAppVersionCode(), TAG_TYPE_WEB), // 获取路由器信息
		URL_ROUTER_ADMIN_GUIDE(
				"http://4006024680.com/cgi-bin/turbo/admin_web/guide/setsafe",
				TAG_TYPE_WEB), // 路由器后台,引导设置密码
		URL_ROUTER_ADMIN("http://4006024680.com/cgi-bin/turbo/admin_mobile",
				TAG_TYPE_WEB), // 路由器后台
		URL_USER_REGISTER(
				HOST_USER_S
						+ "/mobile.php?m=register&a=register&source=mobile&android_client_ver="
						+ Gl.getAppVersionCode(), TAG_TYPE_WEB), URL_USER_LOGIN(
				"/index.php?m=ssov2&a=auth&android_client_ver="
						+ Gl.getAppVersionCode(), TAG_TYPE_USER), URL_USER_FINDPWD(
				HOST_USER_S + "/m/findpwd", TAG_TYPE_WEB), // 找回密码
		URL_USER_LOGIN_BY_PHONE("/mapi.php?m=user&a=login", TAG_TYPE_USER), // 通过手机登录和注册
		URL_USER_REG_BY_PHONE("/mapi.php?m=user&a=init_register", TAG_TYPE_USER), // 通过手机注册,不同于上面那个，这个不会发密码
		URL_USER_INFO_GET("/mapi.php?m=user&a=getprofile&android_client_ver="
				+ Gl.getAppVersionCode(), TAG_TYPE_USER), // 获取用户信息
		URL_USER_NAME_EDIT("/mapi.php?m=user&a=editusername", TAG_TYPE_USER), // 修改昵称
		URL_USER_INFO_EDIT("/mapi.php?m=user&a=change_init_username_pwd",
				TAG_TYPE_USER), // 修改昵称和密码
		URL_USER_AVATAR_EDIT("/mapi.php?m=user&a=update_avatars",
				TAG_TYPE_USER, BINARY), // 修改用户头像
		URL_USER_PWD_RESET("/mapi.php?m=user&a=reset_pwd", TAG_TYPE_USER), // 重置密码
		URL_USER_PWD_EDIT("/mapi.php?m=user&a=change_pwd", TAG_TYPE_USER), // 修改密码
		URL_USER_MOBILE_CHECK("/mapi.php?m=user&a=check_mobile_exit", TAG_TYPE_USER), // 修改密码
		URL_USER_VERYCODE_SEND(
				"/mapi.php?m=sms&a=send_mobile_code&android_client_ver="
						+ Gl.getAppVersionCode(), TAG_TYPE_USER), // 发送短信验证码
		URL_ROUTER_LIST_GET("bind_list_30", TAG_TYPE_TWX), // 获取绑定的路由器列表
		URL_ROUTER_REBOOT("reboot", TAG_TYPE_TWX), // 重启路由
		URL_APP_UPDATE_CHECK("check_android_upgrade", TAG_TYPE_TWX), // 检查app是否可升级
		OPENAPI_CLIENT_WIFI_SWITCH_GET("network.wireless.get_wifi",
				TAG_TYPE_OPEN_CLIENT), // 设置wifi开关
		OPENAPI_CLIENT_WIFI_SWITCH_SET("network.wireless.set_status",
				TAG_TYPE_OPEN_CLIENT), // 获取wifi开关
		OPENAPI_CLINET_LED_STATUS_GET("system.led.get_status",
				TAG_TYPE_OPEN_CLIENT), // 获取led状态
		OPENAPI_CLINET_LED_STATUS_SET_ON("system.led.on", TAG_TYPE_OPEN_CLIENT), // 打开led
		OPENAPI_CLINET_LED_STATUS_SET_OFF("system.led.off",
				TAG_TYPE_OPEN_CLIENT), // 关闭led
		URL_ROUTER_UPGRADE_CHECK("check_router_upgrade", TAG_TYPE_TWX), // 检查路由器是否要升级
		URL_ROUTER_UPGRADE_SET("do_router_upgrade", TAG_TYPE_TWX), // 路由器升级
		URL_PLUGIN_INSTALLED_LIST_GET("get_app_list", TAG_TYPE_TWX), // 获取插件列表
		URL_DEVICE_BLOCK_WIFI_SET("wifi_set_device_block", TAG_TYPE_TWX), // 拉黑设备
		OPENAPI_CLIENT_ROUTER_CROSS_STATUS_GET("network.wireless.get_txpwr",
				TAG_TYPE_OPEN_CLIENT), // 获取路由器穿墙模式
		OPENAPI_CLIENT_ROUTER_CROSS_STATUS_SET("network.wireless.set_txpwr",
				TAG_TYPE_OPEN_CLIENT), // 设置路由器穿墙模式
		URL_ROUTER_NAME_SET("set_router_name", TAG_TYPE_TWX), // 设置路由器名称
		URL_PUSHTOKEN_SET("add_android_push_token", TAG_TYPE_TWX), // 设置pushtoken
		OPENAPI_CLIENT_HEALTH("apps.mobile.get_health_status",
				TAG_TYPE_OPEN_CLIENT), // 获取健康路由状态
		OPENAPI_CLIENT_WIFI_SLEEP_SET("api/wifi/set_wifi_sleep",
				TAG_TYPE_OPEN_CLIENT), // 设置wifi定时开关
		OPENAPI_CLIENT_WIFI_SLEEP_GET("api/wifi/set_wifi_sleep",
				TAG_TYPE_OPEN_CLIENT), // 获取wifi定时开关
		PATH_EXAM_OPTIMIZE_DO("api/exam/do_exam_optimize", TAG_TYPE_TWX_PATH), // 体检优化
		OPENAPI_CLINET_SPEEDUPLIST_GET("network.qos.get_part_speedup_list",
				TAG_TYPE_OPEN_CLIENT), // 获取单项加速列表
		OPENAPI_CLINET_SPEEDUP_SET("network.qos.set_part_speedup",
				TAG_TYPE_OPEN_CLIENT), // 设置单项加速
		OPENAPI_CLINET_QOS_SET("network.qos.set_device_config",
				TAG_TYPE_OPEN_CLIENT), // 设置qos
		OPENAPI_CLINET_QOS_GET("network.qos.get_device_config",
				TAG_TYPE_OPEN_CLIENT), // 获取qos
		OPENAPI_CLINET_CANCEL_SPEEDUP("network.qos.cancel_part_speedup",
				TAG_TYPE_OPEN_CLIENT), // 取消单项加速

		PATH_CLINET_PASSWORD_SET("api/clinet/set_safe_key", TAG_TYPE_TWX_PATH), // 设置安全密码（路由器和WiFi密码）
		PATH_NETWORK_BLOCKED_LIST_GET("api/network/block_list",
				TAG_TYPE_TWX_PATH), // 获取黑名单列表
		PATH_NETWORK_REMOVE_BLOCK_SET("api/network/remove_block",
				TAG_TYPE_TWX_PATH), // 解除黑名单
		OPENAPI_WIFI_CHANNEL_GET("network.wireless.get_channel",
				TAG_TYPE_OPEN_CLIENT), // 获取信道
		OPENAPI_WIFI_CHANNEL_SET("network.wireless.set_channel",
				TAG_TYPE_OPEN_CLIENT), // 设置信道
		OPENAPI_WIFI_CHANNEL_RANK_GET("network.wireless.get_channel_rank",
				TAG_TYPE_OPEN_CLIENT), // 获取路由器信道列表0～11
		PATH_NETWORK_DEVICE_LIST_RPT_GET("api/network/device_list_rpt",
				TAG_TYPE_TWX_PATH), // 获取极卫星设备列表
		PATH_NETWORK_DEVICE_NAME_SET("api/network/set_device_name",
				TAG_TYPE_TWX_PATH), // 设置设备名称
		OPENAPI_CLINET_DEVICE_LIST_GET("network.common.device_list",
				TAG_TYPE_OPEN_CLIENT), // 获取连接的设备列表（包括不在线的）
		OPENAPI_CLINET_ROUTER_INFO_GET("apps.mobile.get_mobi_view_info_out_40",
				TAG_TYPE_OPEN_CLIENT), // 4.0首页获取路由器信息
		OPENAPI_CLINET_TRAFFIC_LIST_GET("network.traffic.status_with_history",
				TAG_TYPE_OPEN_CLIENT), // 获取流量
		OPENAPI_CLINET_TRAFFIC_GET("network.traffic.status",
				TAG_TYPE_OPEN_CLIENT), // 获取流量，轮询用
		API_EXAM_LASTHISTORY_GET("api/Exam/getLastHistory", TAG_TYPE_M_S), // 获取上次体检结果
		API_EXAM_RESULTLIST_GET("api/Exam/getResultList", TAG_TYPE_M_S), // 获取体检结果列表
		API_EXAM_SCORE_GET("api/Exam/getScore", TAG_TYPE_M_S), // 获取体检分数
		API_EXAM_SPEEDTEST_RESULT_GET("api/Exam/getStResult", TAG_TYPE_M_S), // 获取测速结果
		API_EXAM_OPTIMIZE_SCORE_GET("api/Exam/getOptimizeScore", TAG_TYPE_M_S), // 体检优化后得分
		API_EXAM_SPEEDTEST_RANK_GET("api/Exam/getStRank", TAG_TYPE_M_S), // 获取测速排名
		API_ROUTER_CLOUDKEY_GET("api/Router/getCloudkey", TAG_TYPE_M_S), // 获取云key
		API_EXAM_LASTHISTORY_DETAIL_GET("api/Exam/getLastHistoryDetail",
				TAG_TYPE_M_S), // 获取上次体检结果
		API_OPEN_BIND_SET("api/Open/bind", TAG_TYPE_M), // openapi bind路由器
		API_OPEN_LOGOUT("api/Open/logout", TAG_TYPE_M_S), // 用户登出，如果失败应该入队列以后重试

		API_MESSAGE_MESSAGE_LIST_GET("api/Message/get_message_list", TAG_TYPE_M), // 获取消息列表
		API_MESSAGE_VIEW_GET("api/Message/get_message_view", TAG_TYPE_M), // 获取消息详情
		API_MESSAGE_DELETE_ALL("api/Message/del_all_message", TAG_TYPE_M), // 删除所有消息
		API_MESSAGE_SWITCH_GET("api/Message/get_close_switch", TAG_TYPE_M), // 获取消息开关状态
		API_MESSAGE_SWITCH_SET("api/Message/change_switch_status", TAG_TYPE_M), // 设置消息开关状态
		API_MESSAGE_ALL_READ_SET("api/Message/set_all_read", TAG_TYPE_M), // 设置所有消息为已读
		API_MESSAGE_UNREAD_GET("api/Message/get_unread", TAG_TYPE_M), // 获取消息未读数
		API_OPEN_MOBLIE_UPGRADE_SET("api/Open/updateMobilePlug", TAG_TYPE_M), // 设置插件升级

		OPENAPI_CALL("call", TAG_TYPE_OPEN_CLIENT), // 调用openapi接口
		OPENAPI_ROUTER_INFO_GET("router_info", TAG_TYPE_M), // 通过客户端获取mac地址
		/************** 为hiwifi free定义 ****************/
		HIWIFI_APP_RECOMMEND_GET("/hiwififree/box/boxlist", TAG_TYPE_HWF), // 推荐app
		HIWIFI_PWD_GET("/hiwififree/wifi/getPw", TAG_TYPE_HWF_S, JSON), // 获取密码
		HIWIFI_OPONE_GET("/hiwififree/wifi/getOPOne", TAG_TYPE_HWF_S), // 获取运营商密码
		HIWIFI_OPLOG_SEND("/hiwififree/wifi/saveOPLog", TAG_TYPE_HWF_S), // 发送运营商日志
		HIWIFI_APLIST_SEND("/hiwififree/util/saveApScanList", TAG_TYPE_HWF_S,
				JSON), // 发送扫描的wifi列表
		HIWIFI_CONFIG_GET("/hiwififree/wifi/getConfig", TAG_TYPE_HWF), // 获取配置信息
		HIWIFI_MYAPLIST_SEND("/hiwififree/wifi/saveWifiBackup", TAG_TYPE_HWF_S,
				JSON), // 备份wifi
		HIWIFI_TIME_GET("/hiwififree/rank/getRemainTime", TAG_TYPE_HWF_S), // 获取剩余时间
		HIWIFI_BLOCKEDWIFI_GET("/hiwififree/wifi/noAutoLink", TAG_TYPE_HWF), // 获取不自动连接wifi列表
		HIWIFI_STATUS_SHAREDAPP_GET("/hiwififree/rank/getShared",
				TAG_TYPE_HWF_S), // 获取分享状态，指分享微博
		HIWIFI_SHAREDAPP_REPORT("/hiwififree/rank/shareCallback",
				TAG_TYPE_HWF_S), // 分享微博回调
		HIWIFI_PAGE_BUYROUTER(HOST_M
				+ "/api/Page/redirect?act=BuyRouterFromHiwifi", TAG_TYPE_WEB), // 购买极路由地址
		HIWIFI_PAGE_SHAREROUTER(HOST_APP
				+ "mobile.php?m=service&a=install&rid=", TAG_TYPE_WEB), // 共享wifi页面
		HIWIFI_PAGE_DOWNLOADAPP(HOST_M + "/api/Page/redirect?act=downtiantian",
				TAG_TYPE_WEB), // 下载app
		HIWIFI_PAGE_OFFICE_WEBSITE("http://www.hiwifi.com", TAG_TYPE_WEB), // 官网
		HIWIFI_ROUTER_SHARE_SET("/hiwififree/rank/setWifiShare", TAG_TYPE_HWF_S), // 共享wifi
		HIWIFI_CONFIG_BUYROUTER_GET("/hiwififree/page/buyRouterConf",
				TAG_TYPE_HWF), // 购买路由器的配置信息
		HIWIFI_RECENTAPP_SEND("/hiwififree/util/saveRecentApps",
				TAG_TYPE_HWF_S, JSON), // 上报最近打开的app列表
		HIWIFI_ALLAPP_SEND("/hiwififree/util/saveAllApps", TAG_TYPE_HWF_S, JSON), // 上报所有安装的app列表
		HIWIFI_CRASH_SEND("/hiwififree/util/saveCrashLog", TAG_TYPE_HWF_S, JSON), // 上报错误日志
		HIWIFI_PORTAL_SEND("/hiwififree/util/saveCrashLog", TAG_TYPE_HWF_S), // 上报有portal的wifi
		HIWIFI_PWD_VIEWTIMES_GET("/hiwififree/pwd/getLeftTimes", TAG_TYPE_HWF), // 获取密码查看次数
		HIWIFI_PWD_VIEWD_SET("/hiwififree/pwd/getOnePwd", TAG_TYPE_HWF), // 上报用户查看了一次密码
		HIWIFI_DISCOVER_LIST_GET("/hiwififree/find/pagelist", TAG_TYPE_HWF), // 获取发现页列表
		URL_NONE("", "", ""), HIWIFI_CHECK_APP_UPGRADE(
				"/router.php?m=json&a=check_android_upgrade", TAG_TYPE_APP), ;// /
		// TODO 缺流量统计和设备列表
		private String url;
		private String tagType;
		private String method;
		private boolean needLogin;
		private URI requestURI;
		private RequestParams params;

		private RequestTag(String url, String tagType) {
			this.url = url;
			this.tagType = tagType;
			this.method = POST;
			this.needLogin = true;
		}

		private RequestTag(String url, String tagType, String method) {
			this.url = url;
			this.tagType = tagType;
			this.method = method;
			this.needLogin = true;
		}

		public String getType() {
			return tagType;
		}

		public String getMethod() {
			return method;
		}

		public void setURI(URI uri) {
			this.requestURI = uri;
		}

		public void setParams(RequestParams params) {
			this.params = params;
		}

		public RequestParams getParams() {
			return this.params;
		}

		@Override
		public String toString() {
			return String.format(
					"tag:{url:%s,type:%s,method:%s,URI:%s, params:%s}",
					this.url, this.tagType, this.method,
					this.requestURI != null ? this.requestURI.toString()
							: "not set", this.params!=null?this.params.toString():"no params");
		}

		public URI getUri() {
			return this.requestURI;
		}

		public String value() {
			return url;
		}

	}

}
