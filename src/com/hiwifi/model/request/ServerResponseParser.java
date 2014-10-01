/**
 * serverResponseParser.java
 * com.hiwifi.helper.model
 * Hiwifi_Project_V4.1
 * shunping.liu create at 20142014年7月8日下午4:41:05
 */
package com.hiwifi.model.request;

import org.json.JSONObject;

import com.hiwifi.model.User;



/**
 * @author shunping.liu@hiwifi.tw
 * 
 */
public class ServerResponseParser {
	public int code = ServerCode.UnInited.value(),
			app_code = ServerCode.UnInited.value();
	public String message = "", app_msg = "";
	public JSONObject originResponse;
	private boolean isErrorShow;

	public void setErrorShow(boolean isErrorShow) {
		this.isErrorShow = isErrorShow;
	}

	public ServerResponseParser() {
	}

	public ServerResponseParser(JSONObject response) {
		originResponse = response;
		code = response.optInt("code", ServerCode.UnInited.ordinal());
		app_code = response.optInt("app_code", ServerCode.UnInited.ordinal());
		// code = ServerCode.valueOf(response.optInt("code",
		// ServerCode.UnInited.ordinal()));
		// app_code =
		// ServerCode.valueOf(!response.isNull("app_code")?response.optInt("app_code"):ServerCode.UnInited.ordinal());
		message = response.optString("msg", "");
		app_msg = response.isNull("app_msg") ? "" : response
				.optString("app_msg");
		if (!isLoginStatusValid()) {
			User.shareInstance().onTokenExpired();
		} else if (!isPlugStatusUpgrade()) {
			//TODO
//			User.shareInstance().onPlugsUpgrade();
		}/*else if(isPlugStatusUpgrade()){
			User.shareInstance().resetPlugStatus();
		}*/else {
			if (!isClientBindSuccess()) {
				//TODO
//				User.shareInstance().onClientBind();
			} /*else {
				if (HiwifiApplication.getInstance().getRouterInfo() != null) {
					HiwifiApplication.getInstance().getRouterInfo()
							.setClientBind(false);
				}
			}*/
		}
	}

	public Boolean isThrowException() {
		return code == ServerCode.OK.value()
				&& app_code == ServerCode.OK.value();
	}

	public Boolean isLoginStatusValid() {
		return code != ServerCode.TokenError.value()
				&& code != ServerCode.TokenExpire.value();
	}

	public Boolean isPlugStatusUpgrade() {
		return code != ServerCode.PlugNeedUpgrade.value()
				&& code != ServerCode.PlugUnValid.value();
	}

	public Boolean isNormalSuccessCode() {
		return code == ServerCode.OK.value();
	}

	public Boolean isOpenSuccessCode() {
		return code == ServerCode.OK.value()
				&& app_code == ServerCode.OK.value();
	}

	public Boolean isClientBindSuccess() {
		return code != ServerCode.CLIENTUNBINID.value()
				&& app_code != ServerCode.CLIENTUNBINID.value();
	}

//	public ArrayList<RouterInfo> parseRouterList(String mac)
//			throws JSONException {
//		ArrayList<RouterInfo> list = null;
//		JSONArray array = originResponse.isNull("routers") ? null
//				: originResponse.getJSONArray("routers");
//		if (array != null) {
//			list = new ArrayList<RouterInfo>();
//			User.shareInstance().clearUnBindMap();
//			for (int i = 0; i < array.length(); i++) {
//				JSONObject infoObject = array.getJSONObject(i);
//				RouterInfo routerInfo = new RouterInfo();
//				String rmac = infoObject.optString("mac", "");
//				int isonline = infoObject.optInt("is_online", 0);
//				routerInfo.setRid(infoObject.optString("rid", ""));
//				routerInfo.setMac(rmac);
//				routerInfo.setName(infoObject.optString("name", ""));
//				routerInfo.setIsOnline(isonline, mac);
//				routerInfo.setSsid(infoObject.optString("ssid", ""));
//				routerInfo.setAppCnt(infoObject.optInt("app_cnt", 0));
//				//TODO 
//				if(User.shareInstance().isNotEmpty(rmac)){
//					if(User.shareInstance().isUnbindExist(rmac)){
//						User.shareInstance().removeUnbind(rmac);
//					}
//				}else {
//					User.shareInstance().setUnBindMap(rmac, isonline == 0?false:true);
//					LogUtil.d("Tag:", "rmac===>" + rmac);
//				}
//				list.add(routerInfo);
//			}
//		}
//		return list;
//	}
//
//	public ArrayList<Plugin> parsePluginList() throws JSONException {
//		ArrayList<Plugin> list = null;
//		if (originResponse != null && !"".equals(originResponse.toString())) {
//			list = new ArrayList<Plugin>();
//			if (!originResponse.isNull("apps")) {
//				JSONArray jsonArray = originResponse.getJSONArray("apps");
//				for (int i = 0; i < jsonArray.length(); i++) {
//					JSONObject jsonObject2 = jsonArray.getJSONObject(i);
//					Plugin plugsInfo = new Plugin();
//					plugsInfo.setId(jsonObject2.optString("sid", ""));
//					plugsInfo.setName(jsonObject2.optString("name", ""));
//					plugsInfo.setInfo(jsonObject2.optString("info", ""));
//					plugsInfo.setIconUrl(jsonObject2.optString("icon", ""));
//					plugsInfo.setPlugsSwitch(jsonObject2.optInt("switch", 0));
//					list.add(plugsInfo);
//				}
//			}
//		} else {
//			code = ServerCode.UnInited.value();
//		}
//		return list;
//
//	}

	// http://trac.hiwifi.tw/twx/wiki/dev/api_mobile/code_list
	public enum ServerCode {
		Undefined(-4), // 服务器返回了码，但客户端未定义
		UnInited(-3), // 客户端补充
		OK(0), // OK
		NeedEmailInput(301), // email为空
		NeedPasswordInput(302), // password为空
		TokenEmpty(303), // 令牌为空
		RidEmpty(304), // rid为空
		MacEmpty(305), // mac地址为空
		StatusEmpty(306), // 状态错误
		SwitchEmpty(307), // app开关为空
		SidEmpty(308), // 服务id为空
		VerEmpty(309), // 检查更新 版本为空
		TwxPathEmpty(310), // path为空
		RouterNameEmpty(311), // 名字为空
		PushTokenEmpty(312), // 推送token为空
		TokenAuthRequestFailed(313), // token 认证请求失败（未连接到认证服务器）
		TokenAuthFailed(314), // token 认证失败 （认证服务器报错）
		ParameterMissed(315), // 缺少参数
		DataUpdateFailed(316), // 数据更新失败
		ParameterInValid(317), // 参数不合法
		EmailError(401), // 邮箱格式错误
		UserNonexistence(402), // 用户不存在
		PasswordError(403), // 密码错误
		UserDisabled(404), // 用户被禁止
		UserNeedVerify(405), // 用户需要激活
		TokenExpire(406), // 令牌过期，重新申请 App中提示:登录状态已过期，请重新登录
		TokenError(407), // 错误的令牌，查找不到相关信息 App中提示:错误的登录状态，请重新登录
		RidError(408), // rid错误，查找不到相关记录
		RouterNoAccess(409), // 没有权限获取此路由器信息
		NotReady(410), // 数据没有准备好
		RouterNotOnline(411), // 路由器不在线
		DevicesListExpire(412), // 无线列表过期，请刷新
		DevicesMacError(413), // 路由器连接wifi设备mac地址错误
		BindTooMore(414), // 用户已经绑定5台路由器，无法再绑定
		RouterbindedByYouself(416), // 路由器已被自己绑定无需再次绑定
		SidError(417), // 服务查询错误
		AppNotInstalled(418), // 插件未安装
		AlreadyDone(419), // 操作已经完成
		NotSupportSwitch(420), // 不支持开关
		PathNotAllowed(421), // path不允许或者当前路由器的os不支持
		RouterNameLong(422), // 名字超长
		RouterNameInvalid(423), // 非法的名字
		VerError(424), // 版本错误
		TimeOut(501), // 系统超时
		UnknowError(502), // 未知错误
		RebootError(503), // 发送重启失败
		SetDeviceBlockError(504), // 断开wifi设备失败
		CloseDeviceBlockError(505), // 关闭mac地址过滤失败
		CheckRouterUpgradeError(507), // 路由器升级检查更新错误
		DoRouterUpgradeError(508), // 路由器升级错误
		GetLedStatusError(509), // 获取面板灯状态错误
		SetLedStatusError(510), // 设置面板灯状态错误
		SetAppSwitchError(511), // 开关设置错误
		GetMacError(512), // MAC地址获取错误
		GetTxpwrStatusError(513), // 获取穿墙模式状态错误
		SetTxpwrStatusError(514), // 设置穿墙模式出错
		needUpgrade(547), // 路由器不支持此功能，需要更新路由器固件
		TwxApiError(548), // 路由器接口错误
		UserExist(594), // 手机号已经被使用

		PlugNeedUpgrade(100004), // openapi 需升级
		PlugUnValid(100005), // openapi 需升级（未安装或未授权）
		ALREADYBIND(100007), // openapi 需升级（未安装或未授权）
		NameOrPasswordError(10112), // 用户名或密码错误
		CLIENTUNBINID(100012),
		UNONLINE(2003002);

		private int value = 0;

		private ServerCode(int value) {
			this.value = value;
		}

		public static ServerCode valueOf(int value) {
			switch (value) {
			case 0:
				return ServerCode.OK;
			case 301:
				return ServerCode.NeedEmailInput;
			case 302:
				return ServerCode.NeedPasswordInput;
			case 303:
				return ServerCode.TokenEmpty;
			case 304:
				return ServerCode.RidEmpty;
			case 305:
				return ServerCode.MacEmpty;
			case 306:
				return ServerCode.StatusEmpty;
			case 307:
				return ServerCode.SwitchEmpty;
			case 308:
				return ServerCode.SidEmpty;
			case 309:
				return ServerCode.VerEmpty;
			case 310:
				return ServerCode.TwxPathEmpty;
			case 311:
				return ServerCode.RouterNameEmpty;
			case 312:
				return ServerCode.PushTokenEmpty;
			case 313:
				return ServerCode.TokenAuthRequestFailed;
			case 314:
				return ServerCode.TokenAuthFailed;
			case 315:
				return ServerCode.ParameterMissed;
			case 316:
				return ServerCode.DataUpdateFailed;
			case 317:
				return ServerCode.ParameterInValid;

			case 401:
				return ServerCode.EmailError;
			case 402:
				return ServerCode.UserNonexistence;
			case 403:
				return ServerCode.PasswordError;
			case 404:
				return ServerCode.UserDisabled;
			case 405:
				return ServerCode.UserNeedVerify;
			case 406:
				return ServerCode.TokenExpire;
			case 407:
				return ServerCode.TokenError;
			case 408:
				return ServerCode.RidError;
			case 409:
				return ServerCode.RouterNoAccess;
			case 410:
				return ServerCode.NotReady;
			case 411:
				return ServerCode.RouterNotOnline;
			case 412:
				return ServerCode.DevicesListExpire;
			case 413:
				return ServerCode.DevicesMacError;
			case 414:
				return ServerCode.BindTooMore;
			case 416:
				return ServerCode.RouterbindedByYouself;
			case 417:
				return ServerCode.SidError;
			case 418:
				return ServerCode.AppNotInstalled;
			case 419:
				return ServerCode.AlreadyDone;
			case 420:
				return ServerCode.NotSupportSwitch;
			case 421:
				return ServerCode.PathNotAllowed;
			case 422:
				return ServerCode.RouterNameLong;
			case 423:
				return ServerCode.RouterNameInvalid;
			case 424:
				return ServerCode.VerError;
			case 501:
				return ServerCode.TimeOut;
			case 502:
				return ServerCode.UnknowError;
			case 503:
				return ServerCode.RebootError;
			case 504:
				return ServerCode.SetDeviceBlockError;
			case 505:
				return ServerCode.CloseDeviceBlockError;
			case 507:
				return ServerCode.CheckRouterUpgradeError;
			case 508:
				return ServerCode.DoRouterUpgradeError;
			case 509:
				return ServerCode.GetLedStatusError;
			case 510:
				return ServerCode.SetLedStatusError;
			case 511:
				return ServerCode.SetAppSwitchError;
			case 512:
				return ServerCode.GetMacError;
			case 513:
				return ServerCode.GetTxpwrStatusError;
			case 514:
				return ServerCode.SetTxpwrStatusError;
			case 547:
				return ServerCode.needUpgrade;
			case 548:
				return ServerCode.TwxApiError;
			case 594:
				return ServerCode.UserExist;
			case 10112:
				return NameOrPasswordError;
			default:
				return Undefined;
			}
		}

		public int value() {
			return this.value;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return originResponse == null ? "uninited" : originResponse.toString();
	}

}
